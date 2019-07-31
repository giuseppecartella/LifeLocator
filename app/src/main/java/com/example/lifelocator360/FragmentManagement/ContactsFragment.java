package com.example.lifelocator360.FragmentManagement;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifelocator360.DataBaseManagement.Contact;
import com.example.lifelocator360.MapManagement.HttpDataHandler;
import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactsFragment extends Fragment implements View.OnClickListener {
    private RecyclerView recyclerView;
    private ContactsAdapter contactsAdapter;
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
    private ImageView imageMissingContacts;
    private TextView textMissingContacts;
    private String allInformationO1;
    private String allInformationO2;
    private EditText updateTextName;
    private EditText updateTextSurname;
    private EditText updateTextPhone;
    private EditText updateTextAddress;
    private TextView textName;
    private TextView textSurname;
    private TextView textPhone;
    private TextView textAddress;



    public ContactsFragment() {

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        colors = getResources().getIntArray(R.array.materialColors);
        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        editTextName = view.findViewById(R.id.edit_name);
        editTextSurname = view.findViewById(R.id.edit_surname);
        editTextPhone = view.findViewById(R.id.edit_phone);
        editTextAddress = view.findViewById(R.id.edit_address);
        floatingActionButton = view.findViewById(R.id.addContact);
        imageMissingContacts = view.findViewById(R.id.image_missing_contacts);
        textMissingContacts = view.findViewById(R.id.text_missing_contacts);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddContactDialog();
            }
        });

        showContacts();
        return view;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAll: {
                safeDeleteAllDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public int contactsCompare(Contact contact1, Contact contact2) {
        allInformationO1 = contact1.getAllInformation(contact1);
        allInformationO2 = contact2.getAllInformation(contact2);

        if (allInformationO1.compareToIgnoreCase(allInformationO2) < 0) {
            return -1;
        } else if (allInformationO1.compareToIgnoreCase(allInformationO2) == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public int getNewContactIndex(ArrayList<Contact> contacts, Contact contact) {
        if (contacts.size() == 0)
            return 0;


        for (int i = 0; i < contacts.size(); ++i) {
            if (contactsCompare(contact, contacts.get(i)) <= 0) {
                return i;
            }
        }
        return contacts.size();
    }


    public void updateMissingContactsBackground() {
        if (NavigationDrawerActivity.contacts.isEmpty()) {
            imageMissingContacts.setVisibility(View.VISIBLE);
            textMissingContacts.setVisibility(View.VISIBLE);
        } else {
            imageMissingContacts.setVisibility(View.INVISIBLE);
            textMissingContacts.setVisibility(View.INVISIBLE);
        }
    }

    public void showContacts() {
        updateMissingContactsBackground();

        //setto il recycler view
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        contactsAdapter = new ContactsAdapter(NavigationDrawerActivity.contacts);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(contactsAdapter);

        contactsAdapter.setOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showContactInfoDialog(position);
            }
        });
    }


    //vedere bene come toglierla
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addContact: {
                showAddContactDialog();
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
            int index = getNewContactIndex(NavigationDrawerActivity.contacts, contact);
            NavigationDrawerActivity.contacts.add(index, contact);
            SplashActivity.appDataBase.daoManager().addContact(contact);

            contactsAdapter.notifyItemInserted(index);
            updateMissingContactsBackground();

            Toast.makeText(getActivity(), "Contatto salvato!", Toast.LENGTH_SHORT).show();
        }
    }

    public void showContactInfoDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.show_contact_info_dialog_layout, null);

        textName = view.findViewById(R.id.text_name);
        textSurname = view.findViewById(R.id.text_surname);
        textPhone = view.findViewById(R.id.text_phone);
        textAddress = view.findViewById(R.id.text_address);

        textName.setText(NavigationDrawerActivity.contacts.get(position).getName());
        textSurname.setText(NavigationDrawerActivity.contacts.get(position).getSurname());
        textPhone.setText(NavigationDrawerActivity.contacts.get(position).getPhone());
        textAddress.setText(NavigationDrawerActivity.contacts.get(position).getAddress());


        builder.setView(view)
                .setTitle("Scheda contatto")
                .setNeutralButton("ELIMINA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        safeDeleteDialog(position);
                    }
                })
                .setNegativeButton("MAPPA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("prova ", "cerco la posizione " + NavigationDrawerActivity.contacts.get(position).getAddress().replace(" ", "+"));
                        new GetCoordinates().execute(NavigationDrawerActivity.contacts.get(position).getAddress().replace(" ", "+"));
                    }
                })
                .setPositiveButton("MODIFICA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateContactDialog(position);
                    }
                });
        builder.create().show();
    }


    private void deleteContact(int position) {
        Integer idS[] = SplashActivity.appDataBase.daoManager().reciveContactsIds();
        Contact contact = new Contact();
        contact.setId(idS[position]);
        SplashActivity.appDataBase.daoManager().deleteContact(contact);
        NavigationDrawerActivity.contacts.remove(position);

        contactsAdapter.notifyItemRemoved(position);
        updateMissingContactsBackground();
    }

    private void deleteAllContacts() {
        NavigationDrawerActivity.contacts.clear();
        SplashActivity.appDataBase.daoManager().deleteAllContacts();
        contactsAdapter.notifyDataSetChanged();
        updateMissingContactsBackground();
    }

    public void safeDeleteAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Tutti i contatti verranno eliminati")
                .setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("ELIMINA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllContacts();
                    }
                });
        builder.create().show();
    }

    private void updateContact(int position, EditText name, EditText surname, EditText phone, EditText address) {
        Contact contact = new Contact();
        contact.setId(NavigationDrawerActivity.contacts.get(position).getId());
        contact.setName(name.getText().toString());
        contact.setSurname(surname.getText().toString());
        contact.setPhone(phone.getText().toString());
        contact.setAddress(address.getText().toString());

        NavigationDrawerActivity.contacts.remove(position);
        contactsAdapter.notifyItemRemoved(position);

        position = getNewContactIndex(NavigationDrawerActivity.contacts, contact);
        NavigationDrawerActivity.contacts.add(position, contact);
        contactsAdapter.notifyItemInserted(position);

        SplashActivity.appDataBase.daoManager().updateContact(contact);
    }

    private void showContactInMap(int position) {

    }

    public void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_contact_dialog_layout, null);

        builder.setView(view)
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

    public void safeDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Questo contatto verrà eliminato")
                .setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("ELIMINA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteContact(position);
                    }
                });
        builder.create().show();
    }

    public void updateContactDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.update_contact_dialog_layout, null);

        updateTextName = view.findViewById(R.id.update_name);
        updateTextSurname = view.findViewById(R.id.update_surname);
        updateTextPhone = view.findViewById(R.id.update_phone);
        updateTextAddress = view.findViewById(R.id.update_address);

        updateTextName.setText(NavigationDrawerActivity.contacts.get(position).getName());
        updateTextSurname.setText(NavigationDrawerActivity.contacts.get(position).getSurname());
        updateTextPhone.setText(NavigationDrawerActivity.contacts.get(position).getPhone());
        updateTextAddress.setText(NavigationDrawerActivity.contacts.get(position).getAddress());

        builder.setView(view)
                .setTitle("Modifica contatto")
                .setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("SALVA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateContact(position, updateTextName, updateTextSurname, updateTextPhone, updateTextAddress);
                    }
                });
        builder.create().show();
    }

    private class GetCoordinates extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("prova","sono nel preexecute");

        }

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try{
                String address = strings[0];
                HttpDataHandler httpDataHandler =new HttpDataHandler();
                String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=AIzaSyAV3-Tn-8X4CjWVDTrVhSGDQrbAdEsdjuc";
                Log.d("tag", "url vale " + url);
                Log.d("prova","sto per fare il gethttpdata");
                response = httpDataHandler.getHTTPData(url);
                return response;
            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                Log.d("prova","dati arrivati: gestisco il json");
                JSONObject jsonObject = new JSONObject(s);
                String lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();

                String lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();
                Log.d("prova","latlng: "+lat+lng);

                Toast.makeText(getActivity(),lat + lng,Toast.LENGTH_SHORT).show();



            } catch(JSONException e){
                e.printStackTrace();
            }
        }
    }
}