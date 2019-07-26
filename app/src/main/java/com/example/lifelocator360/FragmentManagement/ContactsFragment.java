package com.example.lifelocator360.FragmentManagement;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifelocator360.DataBaseManagement.Contact;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;
import java.util.List;

public class ContactsFragment extends Fragment implements View.OnClickListener {
    List<Contact> contacts;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public static int[] colors;
    View view;


    public ContactsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        colors = getResources().getIntArray(R.array.materialColors);
        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        showContacts();

        return view;
    }


    public void showContacts(){
        contacts =  SplashActivity.appDataBase.daoManager().getContacts();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerAdapter = new ContactsAdapter(contacts);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);

    }

    @Override
    public void onClick(View view) {
        /*switch (view.getId()) {
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
        }*/
    }

    public void addContactDialog() {
        AddContactDialogFragment addContactDialogFragment = new AddContactDialogFragment();
        addContactDialogFragment.show(getFragmentManager(), "addContactDialog");
    }


}