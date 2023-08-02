package com.app;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class editProfileFragment extends Fragment {

    private EditText editText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        editText = view.findViewById(R.id.email);
        editText.setText(ProfileActivity.Email);
        Button button = view.findViewById(R.id.btnSubmit);

        String oldDocumentId = ProfileActivity.Email;

        Spinner spinner = view.findViewById(R.id.gender);
        List<String> options = new ArrayList<>();
        options.add("Male");
        options.add("Female");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_style, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText ve Spinner'dan değeri al
                String value = editText.getText().toString();
                String selectedValue = (String) spinner.getSelectedItem();

                // Firebase Authentication instance'ını alın
                FirebaseAuth auth = FirebaseAuth.getInstance();

                // Mevcut oturum açmış kullanıcıyı alın
                FirebaseUser user = auth.getCurrentUser();

                user.updateEmail(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getActivity(),"succes",Toast.LENGTH_SHORT).show();
                            Log.d(TAG,"Email değiştirildi");

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            String collectionPath = "users";


                            db.collection(collectionPath).document(oldDocumentId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            // Mevcut dokümanın verilerini alın
                                            Map<String, Object> data = documentSnapshot.getData();
                                            data.put("Email", value);

                                            // Yeni dokümanı oluşturmak için eski dokümanı silin
                                            String newDocumentId = value;
                                            db.collection(collectionPath).document(newDocumentId).set(data)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Yeni doküman başarıyla oluşturuldu, şimdi eski dokümanı silin
                                                        db.collection(collectionPath).document(oldDocumentId).delete()
                                                                .addOnSuccessListener(aVoid1 -> {
                                                                    // Doküman başarıyla silindi
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(getActivity(),"Doküman silinirken bir hata oluştu", Toast.LENGTH_SHORT).show();
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getActivity(),"Yeni doküman oluşturulurken bir hata oluştu", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            // Belirtilen doküman mevcut değil
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Doküman alınırken bir hata oluştu
                                    });

                           /*
                            // Firebase Storage referansını alın
                            FirebaseStorage storage = FirebaseStorage.getInstance();

                            // Mevcut ve yeni dosya yollarını belirleyin
                            String currentPath = "ProfilePhotos/"+ ProfileActivity.Email;
                            String newPath = "ProfilePhotos/"+ value;;

                            // Storage referanslarını alın
                            StorageReference currentPhotoRef = storage.getReference().child(currentPath);
                            StorageReference newPhotoRef = storage.getReference().child(newPath);

                            // Mevcut fotoğrafı yeni adıyla taşıyın (kopyalayın)
                            currentPhotoRef
                                    .getDownloadUrl() // Mevcut fotoğrafın indirme URL'sini alın
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            // Fotoğrafın indirme URL'sini aldığınızda, yeni adıyla fotoğrafı taşıyın (kopyalayın)
                                            Log.d(TAG,"İndirme URI alındı");
                                            newPhotoRef.putFile(uri)
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            // Fotoğraf başarıyla kopyalandı, şimdi eski fotoğrafı silin
                                                            Log.d(TAG, "Foto Kopyalanıp Firebase'e yüklendi");
                                                            currentPhotoRef.delete()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            // Eski fotoğraf başarıyla silindi
                                                                            Log.d(TAG, "Eski foto silindi");
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception exception) {
                                                                            // Eski fotoğraf silinirken bir hata oluştu
                                                                            Log.e(TAG,"Eski Foto Silinemedi"+ exception);
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception exception) {
                                                            // Fotoğraf kopyalanırken bir hata oluştu
                                                            Log.e(TAG, "foto kopyalanamadı"+exception);
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Mevcut fotoğrafın indirme URL'sini alırken bir hata oluştu
                                        }
                                    });*/


                            getActivity().recreate();
                        }else {
                            Toast.makeText(getActivity(),"fail",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });

        return view;
    }
}