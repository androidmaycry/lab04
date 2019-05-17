package com.mad.appetit;

import static com.mad.mylibrary.SharedClass.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.mylibrary.Restaurateur;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EditProfile extends AppCompatActivity {
    private String name;
    private String addr;
    private String desc;
    private String mail;
    private String phone;
    private String currentPhotoPath;
    private String error_msg = " ";

    private boolean photoChanged = false;
    private boolean camera_open = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getData();

        findViewById(R.id.button).setOnClickListener(e -> {
            if(checkFields()){
                storeDatabase();
            }
            else{
                Toast.makeText(getApplicationContext(), error_msg, Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.plus).setOnClickListener(p -> editPhoto());
        findViewById(R.id.img_profile).setOnClickListener(e -> editPhoto());
    }

    private boolean checkFields(){
        name = ((EditText)findViewById(R.id.name)).getText().toString();
        addr = ((EditText)findViewById(R.id.address)).getText().toString();
        desc = ((EditText)findViewById(R.id.description)).getText().toString();
        mail = ((EditText)findViewById(R.id.mail)).getText().toString();
        phone = ((EditText)findViewById(R.id.time_text)).getText().toString();

        if(name.trim().length() == 0){
            error_msg = "Fill name";
            return false;
        }

        if(addr.trim().length() == 0){
            error_msg = "Fill address";
            return false;
        }

        if(mail.trim().length() == 0 || !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            error_msg = "Invalid mail";
            return false;
        }

        if(phone.trim().length() != 10){
            error_msg = "Invalid phone number";
            return false;
        }

        return true;
    }

    private void getData(){
        Intent i = getIntent();

        name = i.getStringExtra(Name);
        addr = i.getStringExtra(Address);
        desc = i.getStringExtra(Description);
        mail = i.getStringExtra(Mail);
        phone = i.getStringExtra(Phone);
        currentPhotoPath = i.getStringExtra(Photo);

        ((EditText)findViewById(R.id.name)).setText(name);
        ((EditText)findViewById(R.id.address)).setText(addr);
        ((EditText)findViewById(R.id.description)).setText(desc);
        ((EditText)findViewById(R.id.mail)).setText(mail);
        ((EditText)findViewById(R.id.time_text)).setText(phone);

        InputStream inputStream = null;

        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            inputStream = new URL(currentPhotoPath).openStream();
            if(inputStream != null)
                Glide.with(getApplicationContext()).load(currentPhotoPath).into((ImageView)findViewById(R.id.img_profile));
            else
                ((ImageView)findViewById(R.id.img_profile)).setImageResource(R.drawable.person);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void editPhoto(){
        AlertDialog alertDialog = new AlertDialog.Builder(EditProfile.this, R.style.AlertDialogStyle).create();
        LayoutInflater factory = LayoutInflater.from(EditProfile.this);
        final View view = factory.inflate(R.layout.custom_dialog, null);

        camera_open = true;

        alertDialog.setOnCancelListener(dialog -> {
            camera_open = false;
            alertDialog.dismiss();
        });

        view.findViewById(R.id.camera).setOnClickListener( c -> {
            cameraIntent();
            camera_open = false;
            alertDialog.dismiss();
        });
        view.findViewById(R.id.gallery).setOnClickListener( g -> {
            galleryIntent();
            camera_open = false;
            alertDialog.dismiss();
        });

        alertDialog.setView(view);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Camera", (dialog, which) -> {
            cameraIntent();
            camera_open = false;
            dialog.dismiss();
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Gallery", (dialog, which) -> {
            galleryIntent();
            camera_open = false;
            dialog.dismiss();
        });
        alertDialog.show();
    }

    private void cameraIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);

                photoChanged = true;

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 2);
            }
        }
    }

    private void galleryIntent(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    PERMISSION_GALLERY_REQUEST);
        }
        else{
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        }
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File( storageDir + File.separator +
                imageFileName + /* prefix */
                ".jpg"
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_GALLERY_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission Run Time: ", "Obtained");

                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                } else {
                    Log.d("Permission Run Time: ", "Denied");

                    Toast.makeText(getApplicationContext(), "Access to media files denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if((requestCode == 1) && resultCode == RESULT_OK && null != data){
            Uri selectedImage = data.getData();
            photoChanged = true;

            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            currentPhotoPath = picturePath;
        }

        if((requestCode == 1 || requestCode == 2) && resultCode == RESULT_OK){
            Glide.with(getApplicationContext()).load(currentPhotoPath).into((ImageView)findViewById(R.id.img_profile));
        }
    }

    private void storeDatabase(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(RESTAURATEUR_INFO + "/" + ROOT_UID);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        Map<String, Object> profileMap = new HashMap<>();

        if(photoChanged && currentPhotoPath != null) {
            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());

            ref.putFile(photoUri).continueWithTask(task -> {
                if (!task.isSuccessful()){
                    throw Objects.requireNonNull(task.getException());
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Uri downUri = task.getResult();

                    profileMap.put("info", new Restaurateur(mail, name, addr, desc, "", phone, downUri.toString()));
                    myRef.updateChildren(profileMap);

                    finish();
                }
            });
        }
        else{
            if(currentPhotoPath != null)
                profileMap.put("info", new Restaurateur(mail, name, addr, desc, "", phone, currentPhotoPath));
            else
                profileMap.put("info", new Restaurateur(mail, name, addr, desc, "", phone,  null));

            myRef.updateChildren(profileMap);

            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(Name, ((EditText)findViewById(R.id.name)).getText().toString());
        savedInstanceState.putString(Address, ((EditText)findViewById(R.id.address)).getText().toString());
        savedInstanceState.putString(Description, ((EditText)findViewById(R.id.description)).getText().toString());
        savedInstanceState.putString(Mail, ((EditText)findViewById(R.id.mail)).getText().toString());
        savedInstanceState.putString(Phone, ((EditText)findViewById(R.id.time_text)).getText().toString());
        savedInstanceState.putString(Photo, currentPhotoPath);
        savedInstanceState.putBoolean(CameraOpen, camera_open);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ((EditText)findViewById(R.id.name)).setText(savedInstanceState.getString(Name));
        ((EditText)findViewById(R.id.address)).setText(savedInstanceState.getString(Address));
        ((EditText)findViewById(R.id.description)).setText(savedInstanceState.getString(Description));
        ((EditText)findViewById(R.id.mail)).setText(savedInstanceState.getString(Mail));
        ((EditText)findViewById(R.id.time_text)).setText(savedInstanceState.getString(Phone));

        currentPhotoPath = savedInstanceState.getString(Photo);
        if(currentPhotoPath != null)
            Glide.with(getApplicationContext()).load(currentPhotoPath).into((ImageView)findViewById(R.id.img_profile));

        if(savedInstanceState.getBoolean(CameraOpen))
            editPhoto();
    }
}