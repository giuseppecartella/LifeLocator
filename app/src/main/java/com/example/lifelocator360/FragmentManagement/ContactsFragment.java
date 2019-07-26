package com.example.lifelocator360.FragmentManagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;

public class ContactsFragment extends Fragment implements View.OnClickListener {

    private Button buttonAddContact, buttonViewContact, buttonDeleteContact;


    public ContactsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        buttonAddContact = view.findViewById(R.id.addContact);
        buttonAddContact.setOnClickListener(this);

        buttonViewContact = view.findViewById(R.id.ShowContacts);
        buttonViewContact.setOnClickListener(this);

        buttonDeleteContact = view.findViewById(R.id.removeContact);
        buttonDeleteContact.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addContact: {
                addContactDialog();
                break;
            }

            case R.id.ShowContacts: {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new viewcontactfragment()).addToBackStack("stack").commit();
                break;
            }

            case R.id.removeContact: {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new deletecontactfragment()).addToBackStack("stack2").commit();
                break;
            }
        }
    }

    public void addContactDialog() {
        AddContactDialogFragment addContactDialogFragment = new AddContactDialogFragment();
        addContactDialogFragment.show(getFragmentManager(), "addContactDialog");
    }


}