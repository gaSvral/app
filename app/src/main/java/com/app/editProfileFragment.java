package com.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class editProfileFragment extends Fragment {

    private EditText editText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        editText = view.findViewById(R.id.email);
        Button button = view.findViewById(R.id.btnSubmit);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText'ten değeri al
                String value = editText.getText().toString();

                // Alınan değeri kullanabilirsiniz
                // Örneğin, bir Toast mesajıyla gösterelim:
                Toast.makeText(getActivity(), "Girilen değer: " + value, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}