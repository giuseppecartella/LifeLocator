package com.example.lifelocator360.FragmentManagement;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifelocator360.DataBaseManagement.Contact;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ContactsFragment extends Fragment implements View.OnClickListener {
    private List<Contact> contacts;
    private RecyclerView recyclerView;
    private ContactsAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public static int[] colors;
    private FloatingActionButton floatingActionButton;
    private View view;
    private EditText editTextName;
    private EditText editTextSurname;
    private EditText editTextPhone;
    private EditText editTextAddress;
    private String name;
    private String surname;
    private String phone;
    private String address;


    public ContactsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        colors = getResources().getIntArray(R.array.materialColors);
        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        editTextName = view.findViewById(R.id.edit_name);
        editTextSurname = view.findViewById(R.id.edit_surname);
        editTextPhone = view.findViewById(R.id.edit_phone);
        editTextAddress = view.findViewById(R.id.edit_address);
        floatingActionButton = view.findViewById(R.id.addContact);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
            }
        });

        showContacts();
        return view;
    }

    /*
    public void onDeleteClick(int position){
        int id = contacts.get(position).getId();
        Contact contact = new Contact();
        contact.setId(id);
        SplashActivity.appDataBase.daoManager().deleteContact(contact);
        contacts.remove(position);
        recyclerAdapter.notifyDataSetChanged();
    }*/



    public void showContacts() {
        //prendo i contatti dal database e li metto nella lista
        contacts = SplashActivity.appDataBase.daoManager().getContacts();

        //setto il recycler view
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerAdapter = new ContactsAdapter(contacts);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                int id = contacts.get(position).getId();
                Contact contact = new Contact();
                contact.setId(id);
                SplashActivity.appDataBase.daoManager().deleteContact(contact);
                contacts.remove(position);

                //recyclerAdapter.notifyDataSetChanged();
                recyclerAdapter.notifyItemRemoved(position);

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addContact: {
                addContact();
                break;
            }
        }
    }

    private void onSaveClicked() {

        name = editTextName.getText().toString();
        surname = editTextSurname.getText().toString();
        phone = editTextPhone.getText().toString();
        address = editTextAddress.getText().toString();

        if (name.isEmpty() && surname.isEmpty() && phone.isEmpty() && address.isEmpty()) {
            Toast.makeText(getActivity(), "Contatto non salvato!", Toast.LENGTH_SHORT).show();
        } else {
            Contact contact = new Contact(name, surname, phone, address);
            SplashActivity.appDataBase.daoManager().addContact(contact);
            contacts.add(contact);

            //recyclerAdapter.notifyDataSetChanged();
            recyclerAdapter.notifyItemInserted(contacts.size() - 1);

            Log.d("prova", "PROVA: " + contacts.size());


            Toast.makeText(getActivity(), "Contatto salvato!", Toast.LENGTH_SHORT).show();
        }
    }


    public void addContact() {
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
        builder.create().show();

        editTextName = view.findViewById(R.id.edit_name);
        editTextSurname = view.findViewById(R.id.edit_surname);
        editTextPhone = view.findViewById(R.id.edit_phone);
        editTextAddress = view.findViewById(R.id.edit_address);
        floatingActionButton = view.findViewById(R.id.addContact);
    }


    /*
    public void addContactDialog() {
        ContactDialogFragment contactDialogFragment = new ContactDialogFragment();
        contactDialogFragment.show(getFragmentManager(), "addContactDialog");
    }*/

}