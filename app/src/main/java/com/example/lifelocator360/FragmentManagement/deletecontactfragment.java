package com.example.lifelocator360.FragmentManagement;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lifelocator360.DataBaseManagement.Contact;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class deletecontactfragment extends Fragment {
    private EditText editText;
    private Button removeButton;

    public deletecontactfragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_deletecontactfragment, container, false);
        editText = view.findViewById(R.id.edit_delete);
        removeButton = view.findViewById(R.id.btn_remove_contact);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = Integer.parseInt(editText.getText().toString());
                Contact contact = new Contact();
                contact.setId(id);

                SplashActivity.appDataBase.daoManager().deleteContact(contact);
                Toast.makeText(getActivity(),"Contatto eliminato",Toast.LENGTH_SHORT);
                editText.setText("");
            }
        });

        return view;
    }

}
