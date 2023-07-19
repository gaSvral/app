package com.app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextView nameM, emailM, cardMail, forgotPassword, gender;
    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseStorage storage;
    ImageView pp, btnPp, logOut;
    ProgressBar progressBar;

    public String Email;
    public StorageReference storageRef;
    public UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        pp = findViewById(R.id.pp);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);


        //initialize text views
        nameM = findViewById(R.id.name);
        emailM = findViewById(R.id.email);
        cardMail = findViewById(R.id.cardMail);
        gender = findViewById(R.id.gender);
        forgotPassword = findViewById(R.id.forgotPassword);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        //get current user's email
        String email = user.getEmail();

        //get current users info with emailId
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        String lastM = document.getData().get("LastName").toString();
                        nameM.setText(document.getData().get("name").toString() + " " + lastM);
                        emailM.setText(document.getData().get("Email").toString());
                        cardMail.setText(document.getData().get("Email").toString());
                        gender.setText(document.getData().get("Gender").toString());

                        // we will use when we need it
                        Email = document.getData().get("Email").toString();
                        updatePp();

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        logOut = findViewById(R.id.btnLogOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this,IntroActivity.class));
                finish();
            }
        });

        btnPp = findViewById(R.id.change_pp);
        btnPp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,3);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword(Email);
            }
        });

        Button btnEdit = (Button) findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fragment'ı açma işlemi
                openFragment();
            }
        });
    }

    private void openFragment() {
        // Yeni bir fragment örneği oluşturun
        editProfileFragment myFragment = new editProfileFragment();

        // Fragment'ı açmak için fragment yöneticisi alın
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Fragment işlemini başlatın
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in,0,0,R.anim.fade_out);

        // Fragment'ı ekleyin ve geri düğmesine basıldığında geri alma işlemi tanımlayın
        fragmentTransaction.replace(R.id.fragment_container, myFragment);
        fragmentTransaction.addToBackStack(null);

        // Fragment işlemini onaylayın
        fragmentTransaction.commit();
    }

    private void resetPassword(String email) {
        if (email.length() == 0){
            Toast.makeText(getApplicationContext(),"Please enter Email and send reset link",Toast.LENGTH_SHORT).show();
        }else {
            mAuth = FirebaseAuth.getInstance();
            mAuth.sendPasswordResetEmail(email)
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

    private void updatePp() {

        // Reference to an image file in Cloud Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();


        storageRef.child("ProfilePhotos/"+Email).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'ProfilePhotos/Email'
                Picasso.get().load(uri).into(pp);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                if(exception instanceof StorageException){
                    pp.setImageResource(R.drawable.default_pp);
                    progressBar.setVisibility(View.INVISIBLE);
                }else {
                    Toast.makeText(getApplicationContext(),"fail",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });


        /*
        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        Glide.with(getApplicationContext())
                .load(storageReference)
                .into(pp);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null){
            progressBar.setVisibility(View.VISIBLE);
            Uri selectedImage = data.getData();

            // Create a storage reference from our app
            //storageRef = storage.getReference();

            // Child references can also take paths
            // spaceRef now points to "images/space.jpg
            // imagesRef still points to "images"
            StorageReference spaceRef = storageRef.child("ProfilePhotos/"+ Email );

            uploadTask = spaceRef.putFile(selectedImage);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(),"fail",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    updatePp();
                }
            });

        }



    }
}