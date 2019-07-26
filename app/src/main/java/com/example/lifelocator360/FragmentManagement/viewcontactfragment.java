package com.example.lifelocator360.FragmentManagement;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lifelocator360.DataBaseManagement.Contact;
import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;

import java.util.List;

public class viewcontactfragment extends Fragment {
    private TextView textView;

    public viewcontactfragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_viewcontactfragment, container, false);
        textView = view.findViewById(R.id.txt_display_info);

        List<Contact> contacts = SplashActivity.appDataBase.daoManager().getContacts();
        String info = "";
        for(Contact contact : contacts){
            int id = contact.getId();
            String name = contact.getName();
            String surname = contact.getSurname();
            String phone = contact.getPhone();
            String address = contact.getAddress();

            info = info + "\n\n" + "Id: " + id + "\nName: "+name+"\nSurname: "+surname+"\nPhone: "+phone+"\nAddress: "+address+"\n\n";
        }

        textView.setText(info);
        return view;
    }

}
