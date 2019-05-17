package com.mad.appetit;

import static  com.mad.mylibrary.SharedClass.ROOT_UID;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private String email;
    private String password;
    private String errMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() == null){
            findViewById(R.id.sign_up).setOnClickListener(e -> {
                Intent login = new Intent(this, SignUp.class);
                startActivityForResult(login, 1);
            });

            findViewById(R.id.login).setOnClickListener(h -> {
                if(checkFields()){
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    ROOT_UID = auth.getUid();
                                    Intent fragment = new Intent(this, FragmentManager.class);
                                    startActivity(fragment);
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this,"Wrong Username or Password", Toast.LENGTH_LONG).show();
                                }
                            });
                }
                else{
                    Toast.makeText(MainActivity.this, errMsg, Toast.LENGTH_LONG).show();
                }
            });
        }
        else{
            ROOT_UID = auth.getCurrentUser().getUid();

            Intent fragment = new Intent(this, FragmentManager.class);
            startActivity(fragment);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null && resultCode == 1){
            Intent fragment = new Intent(this, FragmentManager.class);
            startActivity(fragment);
            finish();
        }
    }

    public boolean checkFields(){
        email = ((EditText)findViewById(R.id.email)).getText().toString();
        password = ((EditText)findViewById(R.id.password)).getText().toString();

        if(email.trim().length() == 0 || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            errMsg = "Invalid Mail";
            return false;
        }

        if(password.trim().length() == 0){
            errMsg = "Fill password";
            return false;
        }

        return true;
    }
}