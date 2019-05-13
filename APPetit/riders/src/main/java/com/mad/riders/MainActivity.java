package com.mad.riders;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            }
        });


        TextView email = findViewById(R.id.email);
        TextView password = findViewById(R.id.password);

        findViewById(R.id.sign_up).setOnClickListener(e -> {
            Intent i = new Intent(this, EditProfile.class);
            startActivity(i);
        });

        if(auth.getCurrentUser()!= null){
            Intent i = new Intent(MainActivity.this,NavApp.class);
            i.putExtra("UID",auth.getUid().toString());
            startActivityForResult(i,10);
            finish();
        }

        findViewById(R.id.sign_in).setOnClickListener(e -> {
            auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("success", "signInWithEmail:success");


                                Intent i = new Intent(MainActivity.this,NavApp.class);
                                i.putExtra("UID",auth.getUid().toString());
                                startActivityForResult(i,10);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this,"Wrong Username or Password",Toast.LENGTH_LONG);
                            }
                        }
                    });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(auth.getCurrentUser()!= null){
            Intent i = new Intent(MainActivity.this,NavApp.class);
            i.putExtra("UID",auth.getUid().toString());
            startActivityForResult(i,10);
            finish();
        }
    }
}
