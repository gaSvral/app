package com.app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button btnLogin;
    TextView forgotPassword, register, showPass;
    ImageView imageViewHideShow;
    private FirebaseAuth mAuth;
    public static String emailLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.Password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        imageViewHideShow = (ImageView) findViewById(R.id.hidePassIcon);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //take Email from edittext
                    emailLog = email.getText().toString();
                    String passwordLog = password.getText().toString();

                    logIn(emailLog, passwordLog);
                }catch (Throwable t){
                    Toast.makeText(getApplicationContext(),"Please enter your informations",Toast.LENGTH_SHORT).show();
                }
            }
        });
        imageViewHideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //Hide Password
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //change Icon
                    imageViewHideShow.setImageResource(R.drawable.eye_on);
                }else {
                    //Show Password
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    //Change Icon
                    imageViewHideShow.setImageResource(R.drawable.eye_off);
                }
            }
        });


        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = email.getText().toString();
                resetPassword(emailAddress);
            }
        });

        register = findViewById(R.id.registerText);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        }

    private void resetPassword(String emailAddress) {
        if (emailAddress.length() == 0){
            Toast.makeText(getApplicationContext(),"Please enter Email and send reset link",Toast.LENGTH_SHORT).show();
        }else {
            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                            }
                        }
                    });
            Toast.makeText(getApplicationContext(),"Please check your email for reset link",Toast.LENGTH_SHORT).show();
        }
    }

    private void logIn(String emailLog, String passwordLog) {

        mAuth.signInWithEmailAndPassword(emailLog, passwordLog)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            email.setText("");
                            password.setText("");
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }
}