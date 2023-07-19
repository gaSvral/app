package com.app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button btnRegister;
    EditText name,lastName, email, password;
    FirebaseFirestore db;
    ImageView imageViewHideShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Initialize veriables
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        btnRegister = findViewById(R.id.btnRegister);
        name = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        imageViewHideShow = findViewById(R.id.hidePassIcon);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get email and password from edit text
                String emailR = email.getText().toString().trim();
                String passwordR = password.getText().toString().trim();
                String nameR = name.getText().toString().trim();
                String lastNameR = lastName.getText().toString().trim();

                register(emailR, passwordR, nameR, lastNameR);
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


    }

    private void register(String emailR, String passwordR, String nameR, String lastNameR) {

        mAuth.createUserWithEmailAndPassword(emailR, passwordR)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "createUserWithEmail:success");

                            /********************* SEND VARIABLES TO FIRESTORE **********************/
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", nameR);
                            user.put("LastName", lastNameR);
                            user.put("Email", emailR);
                            user.put("Gender", "-");

                            db.collection("users").document(emailR)
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                            /********************* SEND VARIABLES TO FIRESTORE **********************/

                            Toast.makeText(getApplicationContext(), "User has been registered succesfuly",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Something went wrong.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}