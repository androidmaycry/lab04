package com.mad.appetit;

import static com.mad.mylibrary.SharedClass.*;
import android.Manifest;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EditOffer extends AppCompatActivity {
    private String error_msg = "";
    private boolean editing = false;
    private boolean photoChanged = false;
    private String keyChild;

    private String name;
    private String desc;
    private float priceValue = -1;
    private int quantValue = -1;
    private String currentPhotoPath = null;
    private ImageView imageview;

    private boolean camera_open = false;
    private boolean price_open = false;
    private boolean quant_open = false;

    private Button priceButton;
    private Button quantButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_offer);

        priceButton = findViewById(R.id.price);
        quantButton = findViewById(R.id.quantity);

        priceButton.setOnClickListener(e->setPrice());
        quantButton.setOnClickListener(f->setQuantity());

        findViewById(R.id.plus).setOnClickListener(p -> editPhoto());
        findViewById(R.id.img_profile).setOnClickListener(e -> editPhoto());

        imageview = findViewById(R.id.img_profile);

        String dishName = getIntent().getStringExtra(EDIT_EXISTING_DISH);
        if(dishName != null)
            getData(dishName);
        else
            imageview.setImageResource(R.drawable.hamburger);

        findViewById(R.id.button).setOnClickListener(e -> {
            if(checkFields()){
                storeDatabase();

                finish();
            }
            else{
                Toast.makeText(getApplicationContext(), error_msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getData(String dishName){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        Query query = myRef.child(RESTAURATEUR_INFO + "/" + ROOT_UID + "/" + DISHES_PATH)
                .orderByChild("name").equalTo(dishName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    DailyOfferItem dish = new DailyOfferItem();

                    for(DataSnapshot d : dataSnapshot.getChildren()) {
                        dish = d.getValue(DailyOfferItem.class);
                        keyChild = d.getKey();
                        break;
                    }

                    editing = true;

                    name = dish.getName();
                    desc = dish.getDesc();
                    priceValue = dish.getPrice();
                    quantValue = dish.getQuantity();
                    currentPhotoPath = dish.getPhotoUri();

                    InputStream inputStream = null;

                    try{
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);

                        inputStream = new URL(currentPhotoPath).openStream();
                        if(inputStream != null)
                            Glide.with(getApplicationContext()).load(currentPhotoPath).into((ImageView)findViewById(R.id.img_profile));
                        else
                            imageview.setImageResource(R.drawable.hamburger);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }

                    ((EditText)findViewById(R.id.name)).setText(name);
                    ((EditText)findViewById(R.id.description)).setText(desc);

                    priceButton.setText(Float.toString(priceValue));
                    quantButton.setText(Integer.toString(quantValue));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("EDIT OFFER", "Failed to read value.", error.toException());
            }
        });
    }

    private boolean checkFields(){
        name = ((EditText)findViewById(R.id.name)).getText().toString();
        desc = ((EditText)findViewById(R.id.description)).getText().toString();

        if(name.trim().length() == 0){
            error_msg = "Insert name";
            return false;
        }

        if(desc.trim().length() == 0){
            error_msg = "Insert description";
            return false;
        }

        if(priceValue == -1){
            error_msg = "Insert price";
            return false;
        }

        if(quantValue == -1){
            error_msg = "Insert quantity";
            return false;
        }

        return true;
    }

    private String[] setCentsValue(){
        String[] cent = new String[100];
        for(int i=0; i<100; i++){
            if(i<10) {
                cent[i] = "0" +i;
            }
            else{
                cent[i] = ""+i;
            }
        }
        return cent;
    }

    private void setPrice(){
        AlertDialog priceDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(EditOffer.this);
        final View view = inflater.inflate(R.layout.price_dialog, null);

        price_open = true;

        NumberPicker euro = view.findViewById(R.id.euro_picker);
        NumberPicker cent = view.findViewById(R.id.cent_picker);

        priceDialog.setView(view);

        priceDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", (dialog, which) -> {
            float centValue = cent.getValue();
            price_open = false;
            priceValue = euro.getValue() + (centValue/100);
            priceButton.setText(Float.toString(priceValue));
        });
        priceDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"CANCEL", (dialog, which) -> {
            price_open = false;
            dialog.dismiss();
        });

        euro.setMinValue(0);
        euro.setMaxValue(9999);
        euro.setValue(0);

        String[] cents = setCentsValue();

        cent.setDisplayedValues(cents);
        cent.setMinValue(0);
        cent.setMaxValue(99);
        cent.setValue(0);

        priceDialog.show();
    }

    private void setQuantity(){
        AlertDialog quantDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(EditOffer.this);
        final View view = inflater.inflate(R.layout.quantity_dialog, null);

        quant_open = true;

        NumberPicker quantity = view.findViewById(R.id.quant_picker);

        quantDialog.setView(view);

        quantDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", (dialog, which) -> {
            quant_open = false;
            quantValue = quantity.getValue();
            quantButton.setText(Integer.toString(quantValue));
        });
        quantDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"CANCEL", (dialog, which) -> {
            quant_open = false;
            dialog.dismiss();
        });

        quantity.setMinValue(1);
        quantity.setMaxValue(999);
        quantity.setValue(1);

        quantDialog.show();
    }

    private void editPhoto(){
        AlertDialog alertDialog = new AlertDialog.Builder(EditOffer.this, R.style.AlertDialogStyle).create();
        LayoutInflater factory = LayoutInflater.from(EditOffer.this);
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
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(
                RESTAURATEUR_INFO + "/" + ROOT_UID + "/" + DISHES_PATH);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        Map<String, Object> dishMap = new HashMap<>();

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

                    if(editing)
                        dishMap.put(keyChild, new DailyOfferItem(name, desc, priceValue, quantValue, downUri.toString()));
                    else
                        dishMap.put(Objects.requireNonNull(myRef.push().getKey()), new DailyOfferItem(name, desc, priceValue, quantValue, downUri.toString()));

                    myRef.updateChildren(dishMap);
                }
            });
        }
        else{
            if(editing && currentPhotoPath != null)
                dishMap.put(keyChild, new DailyOfferItem(name, desc, priceValue, quantValue, currentPhotoPath));
            else
                dishMap.put(Objects.requireNonNull(myRef.push().getKey()), new DailyOfferItem(name, desc, priceValue, quantValue, null));

            myRef.updateChildren(dishMap);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(Name, ((EditText)findViewById(R.id.name)).getText().toString());
        savedInstanceState.putString(Description, ((EditText)findViewById(R.id.description)).getText().toString());
        savedInstanceState.putFloat(Price, priceValue);
        savedInstanceState.putInt(Quantity, quantValue);
        savedInstanceState.putString(Photo, currentPhotoPath);
        savedInstanceState.putBoolean(CameraOpen, camera_open);
        savedInstanceState.putBoolean(PriceOpen, price_open);
        savedInstanceState.putBoolean(QuantOpen, quant_open);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ((EditText)findViewById(R.id.name)).setText(savedInstanceState.getString(Name));
        ((EditText)findViewById(R.id.description)).setText(savedInstanceState.getString(Description));

        priceValue = savedInstanceState.getFloat(Price);
        if(priceValue != -1)
            priceButton.setText(Float.toString(priceValue));

        quantValue = savedInstanceState.getInt(Quantity);
        if(quantValue != -1)
            quantButton.setText(Integer.toString(quantValue));

        currentPhotoPath = savedInstanceState.getString(Photo);
        if(currentPhotoPath != null)
            Glide.with(getApplicationContext()).load(currentPhotoPath).into((ImageView)findViewById(R.id.img_profile));

        if(savedInstanceState.getBoolean(CameraOpen))
            editPhoto();

        if(savedInstanceState.getBoolean(PriceOpen))
            setPrice();

        if(savedInstanceState.getBoolean(QuantOpen))
            setQuantity();
    }
}
