package com.example.lifelocator360.FragmentManagement;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifelocator360.DataBaseManagement.Contact;
import com.example.lifelocator360.MapManagement.HttpDataHandler;
import com.example.lifelocator360.MapManagement.MapsFragment;
import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import static com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity.ZOOM_TO_MARKER;
import static com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity.contacts;

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
    private int oldIndex;
    private boolean refreshGoToMapButton = false;
    private AlertDialog contactInfoDialog;
    private boolean isNewContact; //Serve per on post execute, per sapere se chiamare un nuovo contatto o aggiornarlo


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


    public void showContacts() {
        UISupport.updateBackground(contacts,textMissingContacts,imageMissingContacts);

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


    private void onSaveClicked() {

        name = editTextName.getText().toString();
        surname = editTextSurname.getText().toString();
        phone = editTextPhone.getText().toString();
        address = editTextAddress.getText().toString();

        if (name.isEmpty() && surname.isEmpty() && phone.isEmpty() && address.isEmpty()) {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.nav_drawer_layout), "Contatto non salvato.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        } else {
            if (address.isEmpty()) {
                saveContact(name, surname, phone, address, "NO_ADDRESS", "NO_ADDRESS");
                Log.d("richiesta", "Salvataggio con indirizzo assente");
            } else if (!checkNetworkConnectionStatus()) {
                saveContact(name, surname, phone, address, "NO_INTERNET", "NO_INTERNET");
                Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.nav_drawer_layout), "Nessuna connessione, la posizione sulla mappa verrà aggiunta più tardi.", Snackbar.LENGTH_LONG);
                snackbar.show();
                Log.d("richiesta", "Salvataggio con connessione assente");
            } else {
                Log.d("richiesta", "Provo il salvataggio con indirizzo");
                isNewContact = true;
                new GetCoordinates().execute(address.replace(" ", "+"), "ADD");
            }

        }
    }

    private void saveContact(String name, String surname, String phone,String address,String latitude,String longitude){

            Contact contact = new Contact(name, surname, phone, address,latitude,longitude);
            int index = getNewContactIndex(NavigationDrawerActivity.contacts, contact);

            SplashActivity.appDataBase.daoManager().addContact(contact);
            Integer contactIds[] = SplashActivity.appDataBase.daoManager().getContactIds();
            NavigationDrawerActivity.contacts.add(index, contact);
            NavigationDrawerActivity.contacts.get(index).setId(contactIds[contactIds.length - 1]);

            contactsAdapter.notifyItemInserted(index);
            UISupport.updateBackground(contacts,textMissingContacts,imageMissingContacts);

        Log.d("richiesta", "Salvataggio con coordinate dal save contact");

        //Imposto il marker
        if (latitude.equals("NO_INTERNET") || latitude.equals("NO_ADDRESS") || latitude.equals("NO_RESULT")) {
            Log.d("richiesta", "salvato contatto con errore: " + contact.getLatitude());
        } else {
            Log.d("richiesta", "salvato contatto con coordinate: " + contact.getLatitude() + " " + contact.getLongitude());

            //Invio i dati alla mappa tramite il navigation drawer
            String inputLatitude = contact.getLatitude();
            String inputLongitude = contact.getLongitude();
            contactFragmentListener.onInputContactSent(inputLatitude, inputLongitude, "ADD",  name + " " + surname, NavigationDrawerActivity.contacts.get(index).getId());
        }
    }

    //////////////////////////////////////////////////////GESTORE COMUNICAZIONE CON MAPPA///////////////////////////
    private ContactFragmentListener contactFragmentListener;

    public interface ContactFragmentListener {
        void onInputContactSent(String inputLatitude, String inputLongitude, String editType, String contactTitle, int index);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ContactFragmentListener) {
            contactFragmentListener = (ContactFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ContactFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        contactFragmentListener = null;
    }

    //////////////////////////////////////////////////////FINE GESTORE COMUNICAZIONE CON MAPPA///////////////////////////

    public boolean checkNetworkConnectionStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected())
            return true;
        else
            return false;
    }

    public void onUpdateClicked(int index, EditText contactName, EditText contactSurname, EditText contactPhone,EditText contactAddress) {
        String oldAddress = NavigationDrawerActivity.contacts.get(index).getAddress();
        String newAddress = contactAddress.getText().toString();


        name = contactName.getText().toString();
        surname = contactSurname.getText().toString();
        phone = contactPhone.getText().toString();
        address = contactAddress.getText().toString();


        if (oldAddress.equals(newAddress)) {
            oldIndex = index;
            updateContact(name,surname,phone,address, NavigationDrawerActivity.contacts.get(oldIndex).getLatitude(), NavigationDrawerActivity.contacts.get(oldIndex).getLongitude());
        } else {
            oldIndex = index;
            if (contactAddress.getText().toString().isEmpty()) {
                updateContact(name,surname,phone,address, "NO_ADDRESS", "NO_ADDRESS");
                Log.d("richiesta", "Salvataggio con indirizzo assente");
            } else if (!checkNetworkConnectionStatus()) {
                updateContact(name,surname,phone,address, "NO_INTERNET", "NO_INTERNET");
                Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.nav_drawer_layout), "Nessuna connessione, la posizione sulla mappa verrà aggiunta più tardi.", Snackbar.LENGTH_LONG);
                snackbar.show();
                Log.d("richiesta", "Modifica con connessione assente");
            } else {
                isNewContact = false;
                Log.d("richiesta", "Provo il salvataggio con indirizzo");
                new GetCoordinates().execute(newAddress.replace(" ", "+"));
            }
        }

    }

    private void updateContact(String name,String surname,String phone,String address,String lat,String lng) {
        Contact contact = new Contact();
        contact.setId(NavigationDrawerActivity.contacts.get(oldIndex).getId());
        contact.setName(name);
        contact.setSurname(surname);
        contact.setPhone(phone);
        contact.setAddress(address);
        contact.setLatitude(lat);
        contact.setLongitude(lng);

        NavigationDrawerActivity.contacts.remove(oldIndex);
        contactsAdapter.notifyItemRemoved(oldIndex);

        Integer newIndex = getNewContactIndex(NavigationDrawerActivity.contacts, contact);
        NavigationDrawerActivity.contacts.add(newIndex, contact);

        SplashActivity.appDataBase.daoManager().updateContact(contact);
        contactsAdapter.notifyItemInserted(newIndex);

        //Eventualmente sposto il marker
        if (contact.getLatitude().equals("NO_INTERNET") || contact.getLatitude().equals("NO_ADDRESS") || contact.getLatitude().equals("NO_RESULT")) {
            contactFragmentListener.onInputContactSent("REMOVE_MARKER", "REMOVE_MARKER", "DELETE",  name + " " + surname,contact.getId());
            Log.d("richiesta", "aggiornato contatto con errore: " + contact.getLatitude());
        } else {
            Log.d("richiesta", "aggiornato contatto con coordinate: " + contact.getLatitude() + " " + contact.getLongitude());

            //Invio i dati alla mappa tramite il navigation drawer
            String inputLatitude = contact.getLatitude();
            String inputLongitude = contact.getLongitude();
            contactFragmentListener.onInputContactSent(inputLatitude, inputLongitude, "UPDATE", name + " " + surname, contact.getId());
        }



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

    private void deleteAllContacts() {
        NavigationDrawerActivity.contacts.clear();
        SplashActivity.appDataBase.daoManager().deleteAllContacts();
        contactsAdapter.notifyDataSetChanged();
        UISupport.updateBackground(contacts,textMissingContacts,imageMissingContacts);

        contactFragmentListener.onInputContactSent("DELETE_ALL","DELETE_ALL","DELETE_ALL","DELETE_ALL",-1);
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

    private void deleteContact(int index) {
        Integer id = NavigationDrawerActivity.contacts.get(index).getId();
        Log.d("PROVA3", "l'id vale "+ id);
        Contact contact = new Contact();
        contact.setId(id);
        NavigationDrawerActivity.contacts.remove(index);
        SplashActivity.appDataBase.daoManager().deleteContact(contact);
        contactsAdapter.notifyItemRemoved(index);
        UISupport.updateBackground(contacts,textMissingContacts,imageMissingContacts);

        contactFragmentListener.onInputContactSent("DELETING", "DELETING", "DELETE", name + " " + surname, id);
    }

    private void returnToMap(){
        NavigationDrawerActivity.currentFragment = "Mappa";
        NavigationDrawerActivity.uncheckAllNavigationItems();
        getActivity().getSupportFragmentManager().popBackStack();

        //Hide return to map button
        getActivity().invalidateOptionsMenu();
    }

    private void showContactInMap(String latitude,String longitude) {
        Double lat = Double.parseDouble(latitude);
        Double lng = Double.parseDouble(longitude);

        MapsFragment.moveCamera(new LatLng(lat,lng),ZOOM_TO_MARKER);
        returnToMap();
    }

    public void updateContactDialog(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.update_contact_dialog_layout, null);

        updateTextName = view.findViewById(R.id.update_name);
        updateTextSurname = view.findViewById(R.id.update_surname);
        updateTextPhone = view.findViewById(R.id.update_phone);
        updateTextAddress = view.findViewById(R.id.update_address);

        updateTextName.setText(NavigationDrawerActivity.contacts.get(index).getName());
        updateTextSurname.setText(NavigationDrawerActivity.contacts.get(index).getSurname());
        updateTextPhone.setText(NavigationDrawerActivity.contacts.get(index).getPhone());
        updateTextAddress.setText(NavigationDrawerActivity.contacts.get(index).getAddress());

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
                        onUpdateClicked(index, updateTextName,updateTextSurname,updateTextPhone,updateTextAddress);
                    }
                });


        builder.create().show();
    }

    public void showContactInfoDialog(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.show_contact_info_dialog_layout, null);

        textName = view.findViewById(R.id.text_name);
        textSurname = view.findViewById(R.id.text_surname);
        textPhone = view.findViewById(R.id.text_phone);
        textAddress = view.findViewById(R.id.text_address);

        textName.setText(NavigationDrawerActivity.contacts.get(index).getName());
        textSurname.setText(NavigationDrawerActivity.contacts.get(index).getSurname());
        textPhone.setText(NavigationDrawerActivity.contacts.get(index).getPhone());
        textAddress.setText(NavigationDrawerActivity.contacts.get(index).getAddress());


        builder.setView(view)
                .setTitle("Scheda contatto");

        contactInfoDialog = builder.create();

        contactInfoDialog.setButton(AlertDialog.BUTTON_POSITIVE, "MODIFICA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateContactDialog(index);
                dialog.dismiss();
            }
        });

        contactInfoDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "MAPPA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showContactInMap(NavigationDrawerActivity.contacts.get(index).getLatitude(),NavigationDrawerActivity.contacts.get(index).getLongitude());
                dialog.dismiss();
            }
        });

        contactInfoDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ELIMINA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                safeDeleteDialog(index);
                dialog.dismiss();
            }
        });


        contactInfoDialog.show();

        Button buttonPositive = contactInfoDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button buttonNegative = contactInfoDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button buttonNeutral = contactInfoDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) buttonPositive.getLayoutParams();
        layoutParams.weight = 10;
        buttonPositive.setLayoutParams(layoutParams);
        buttonNegative.setLayoutParams(layoutParams);
        buttonNeutral.setLayoutParams(layoutParams);

        oldIndex = index;
        isNewContact = false;

        name = NavigationDrawerActivity.contacts.get(index).getName();
        surname = NavigationDrawerActivity.contacts.get(index).getSurname();
        phone = NavigationDrawerActivity.contacts.get(index).getPhone();
        address = NavigationDrawerActivity.contacts.get(index).getAddress();


        String latStatus = NavigationDrawerActivity.contacts.get(index).getLatitude();

        if (latStatus.equals("NO_ADDRESS") || latStatus.equals("NO_RESULT")) {
            contactInfoDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        } else if (latStatus.equals("NO_INTERNET")) {
            contactInfoDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            if (checkNetworkConnectionStatus()) {
                refreshGoToMapButton = true;
                new GetCoordinates().execute(NavigationDrawerActivity.contacts.get(index).getAddress().replace(" ", "+"), "RETRY");
            }
        }
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
                Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.nav_drawer_layout), "Errore nell'ottenimento della posizione.", Snackbar.LENGTH_LONG);
                snackbar.show();

                if (isNewContact) {
                    saveContact(name,surname, phone,address, "NO_RESULT", "NO_RESULT");
                } else {
                    updateContact(name, surname,phone,address, "NO_RESULT", "NO_RESULT");
                }
                Log.d("richiesta", "Salvataggio con risultato assente");
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

                if (isNewContact)
                    saveContact(name, surname,phone,address, lat, lng);
                else {
                    updateContact(name, surname,phone,address, lat, lng);

                    if(refreshGoToMapButton) {
                        refreshGoToMapButton = false;
                        contactInfoDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                    }
                }

            } catch(JSONException e){
                if(HttpDataHandler.timeOutException) {
                    HttpDataHandler.timeOutException = false;

                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.nav_drawer_layout), "Connessione troppo lenta, la posizione sulla mappa verrà aggiunta più tardi.", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    if (isNewContact)
                        saveContact(name, surname, phone, address, "NO_INTERNET", "NO_INTERNET");
                    else
                        updateContact(name, surname, phone,address, "NO_INTERNET", "NO_INTERNET");
                    Log.d("richiesta", "Salvataggio con risultato assente");

                } else {
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.nav_drawer_layout), "L'indirizzo non è valido, sii più preciso.", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    if (isNewContact)
                        saveContact(name, surname, phone,address, "NO_RESULT", "NO_RESULT");
                    else
                        updateContact(name, surname, phone,address, "NO_RESULT", "NO_RESULT");
                    Log.d("richiesta", "Salvataggio con risultato assente");
                }

                e.printStackTrace();
            }
        }
    }
}