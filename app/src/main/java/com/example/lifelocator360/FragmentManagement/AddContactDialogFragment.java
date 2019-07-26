package com.example.lifelocator360.FragmentManagement;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.lifelocator360.DataBaseManagement.Contact;
import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;

public class AddContactDialogFragment extends AppCompatDialogFragment {

    private EditText editTextName;
    private EditText editTextSurname;
    private EditText editTextPhone;
    private EditText editTextAddress;

    private String name;
    private String surname;
    private String phone;
    private String address;


    private void onSaveClicked() {
        name = editTextName.getText().toString();
        surname = editTextSurname.getText().toString();
        phone = editTextPhone.getText().toString();
        address = editTextAddress.getText().toString();

        Contact contact = new Contact(name, surname, phone, address);
        SplashActivity.appDataBase.daoManager().addContact(contact);
        Toast.makeText(getActivity(), "Contatto salvato!", Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_contact_dialog_layout, null);

        builder.setView(view)
                .setCancelable(false)
                .setTitle("Nuovo contatto")
                .setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("SALVA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSaveClicked();
                    }
                });

        editTextName = view.findViewById(R.id.edit_name);
        editTextSurname = view.findViewById(R.id.edit_surname);
        editTextPhone = view.findViewById(R.id.edit_phone);
        editTextAddress = view.findViewById(R.id.edit_address);

        return builder.create();
    }

    public interface AddContactDialogFragmentListener {
        void applyTexts(String name, String surname, String phone, String address);
    }
}
