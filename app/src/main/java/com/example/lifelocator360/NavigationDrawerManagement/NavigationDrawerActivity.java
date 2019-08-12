package com.example.lifelocator360.NavigationDrawerManagement;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.lifelocator360.DataBaseManagement.Contact;
import com.example.lifelocator360.DataBaseManagement.Note;
import com.example.lifelocator360.FragmentManagement.CalendarFragment;
import com.example.lifelocator360.FragmentManagement.ContactsFragment;
import com.example.lifelocator360.FragmentManagement.InstagramFragment;
import com.example.lifelocator360.FragmentManagement.NotesFragment;
import com.example.lifelocator360.FragmentManagement.PhotoFragment;
import com.example.lifelocator360.FragmentManagement.SettingsFragment;
import com.example.lifelocator360.MapManagement.MapsFragment;
import com.example.lifelocator360.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;



public class NavigationDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NotesFragment.NoteFragmentListener,ContactsFragment.ContactFragmentListener {
    private DrawerLayout drawerLayout;
    public static NavigationView navigationView;
    public static String currentFragment = "Mappa";
    public static int navigationDrawerSize;
    public static final float DEF_ZOOM = 11.0f;
    public static final float ZOOM_TO_MARKER = 17.0f;
    private Timer timer;
    private TimerTask timerTask;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public static ArrayList<Contact> contacts;
    public static ArrayList<Note> notes;
    public static ArrayList<File> photos;


    public static int getNavigationDrawerSize() {
        return navigationDrawerSize;
    }

    public static void setNavigationDrawerSize() {
        navigationDrawerSize = navigationView.getMenu().size();
    }

    public static void uncheckAllNavigationItems() {
        setNavigationDrawerSize();

        for (int i = 0; i < getNavigationDrawerSize(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    /////////////IN CASO DI MODIFICA,CAMBIARE IL RETURN TO MAP NEGLI ALTRI FRAGMENT
    public void returnToMap() {
        currentFragment = "Mappa";
        uncheckAllNavigationItems();
        getSupportFragmentManager().popBackStack();

        //Hide return to map button
        this.invalidateOptionsMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MapsFragment mapsFragment = new MapsFragment();
        mapsFragment.setGPSActive(true); // flag maintain before get location

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        MapsFragment.mMap.setMyLocationEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater;
        inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);


        //Show/Hide return to map button
        MenuItem returnToMap = menu.findItem(R.id.home);
        if(currentFragment.equals("Mappa")) {
            setTitle(R.string.app_name);
            returnToMap.setVisible(false);
        }
        else {
            setTitle(currentFragment);
            returnToMap.setVisible(true);
        }

        MenuItem deleteAll = menu.findItem(R.id.deleteAll);
        if (currentFragment.equals("Contatti") || currentFragment.equals("Note") || currentFragment.equals("Calendario"))
            deleteAll.setVisible(true);
        else
            deleteAll.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home: {
                if (!currentFragment.equals("Mappa")) {
                    returnToMap();
                }
                return true;
            }
            case R.id.deleteAll: {
                super.onOptionsItemSelected(item);
                return false;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getDeviceLocationScheduled() {
        Log.d("Timer", "il timer sta andando...");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(NavigationDrawerActivity.this);

        try {
            Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        if (currentLocation != null) {
                            Log.d("timer", "location vale " + currentLocation);
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            MapsFragment.moveCamera(latLng, DEF_ZOOM);

                            timer.cancel();
                            timer.purge();
                        }

                    } else {
                        Log.d("timer", "current location is null!");
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("timer", "Security Exception " + e.getMessage());
        }
    }

    private void waitForAvailableLocation() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getDeviceLocationScheduled();
            }
        };
        timer.scheduleAtFixedRate(timerTask, 20, 20);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        contacts = (ArrayList<Contact>) getIntent().getSerializableExtra("lista_contatti");
        notes = (ArrayList<Note>) getIntent().getSerializableExtra("lista_note");
        photos = (ArrayList<File>) getIntent().getSerializableExtra("lista_photo_path");


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapsFragment()).commit();
        }

        waitForAvailableLocation();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_photo:
                if (!currentFragment.equals("Mappa")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new PhotoFragment()).addToBackStack("stack1").commit();
                currentFragment = "Foto";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_calendar:
                if (!currentFragment.equals("Mappa")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new CalendarFragment()).addToBackStack("stack1").commit();
                currentFragment = "Calendario";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_contacts:
                if (!currentFragment.equals("Mappa")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new ContactsFragment()).addToBackStack("stack1").commit();
                currentFragment = "Contatti";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_notes:
                if (!currentFragment.equals("Mappa")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new NotesFragment()).addToBackStack("stack1").commit();
                currentFragment = "Note";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_instagram:
                if (!currentFragment.equals("Mappa")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new InstagramFragment()).addToBackStack("stack1").commit();
                currentFragment = "Instagram";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_settings:
                if (!currentFragment.equals("Mappa")) {
                    getSupportFragmentManager().popBackStack();
                }

                navigationView.getMenu().getItem(5).setChecked(true);

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new SettingsFragment()).addToBackStack("stack1").commit();
                currentFragment = "Impostazioni";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (!currentFragment.equals("Mappa"))
                returnToMap();
            else
                super.onBackPressed();
        }
    }

    private void addNoteMarker(String inputLatitude, String inputLongitude, String noteTitle, int index){
        Double lat = Double.parseDouble(inputLatitude);
        Double lng = Double.parseDouble(inputLongitude);
        if(noteTitle.isEmpty())
            noteTitle = "NESSUN TITOLO";

        MapsFragment.newMarker = MapsFragment.mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                .title(noteTitle));

        Log.d("QUAAAA", "arrivato qua");
        MapsFragment.newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.note_icon_map));
        MapsFragment.newMarker.setTag(index);
        Toast.makeText(this, "Aggiunto index " + index, Toast.LENGTH_SHORT).show();

        MapsFragment.noteMarkers.add(MapsFragment.newMarker);
    }

    private void updateNoteMarker(String inputLatitude, String inputLongitude, String noteTitle, Integer oldIndex) {
        Double lat = Double.parseDouble(inputLatitude);
        Double lng = Double.parseDouble(inputLongitude);
        boolean found = false;
        if(noteTitle.isEmpty())
            noteTitle = "NESSUN TITOLO";

        for(int i = 0; i < MapsFragment.noteMarkers.size(); ++i) {
            if(MapsFragment.noteMarkers.get(i).getTag() == oldIndex) {
                MapsFragment.noteMarkers.get(i).setPosition(new LatLng(lat, lng));
                MapsFragment.noteMarkers.get(i).setTitle(noteTitle);
                Toast.makeText(this, "Aggiornato index " + i, Toast.LENGTH_SHORT).show();
                found = true;
            }
        }

        if(!found) {
            addNoteMarker(inputLatitude, inputLongitude, noteTitle, oldIndex);
        }
    }

    private void deleteNoteMarker(String inputLatitude, String inputLongitude, Integer index) {
        for(int i = 0; i < MapsFragment.noteMarkers.size(); ++i) {
            if(MapsFragment.noteMarkers.get(i).getTag() == index) {
                MapsFragment.noteMarkers.get(i).remove(); //rimuovo dalla mappa
                MapsFragment.noteMarkers.remove(i);       //rimuovo da vettore di marker
            }
        }

        //Se devo eliminare anche la nota oltre al marker, aggiorno tutti i tag dei marker successivi, altrimenti se devo
        //eliminare solo il marker non entro nell'if (caso di update)
        if(!inputLatitude.equals("REMOVE_MARKER")) {
            for (int i = 0; i < MapsFragment.noteMarkers.size(); ++i) {
                if ((Integer) MapsFragment.noteMarkers.get(i).getTag() > index) {
                    Log.d("FATTO", "sono entrato");
                    MapsFragment.noteMarkers.get(i).setTag((Integer) MapsFragment.noteMarkers.get(i).getTag() - 1);
                }
            }
        }
    }

    private void deleteAllNoteMarkers(){
        for(Marker m : MapsFragment.noteMarkers){
            m.remove();
        }
        MapsFragment.noteMarkers.clear();
    }

    @Override
    public void onInputNoteSent(String inputLatitude, String inputLongitude, String editType, String noteTitle, int index) {
        if(editType.equals("ADD")) {
            addNoteMarker(inputLatitude, inputLongitude, noteTitle, index);
        } else if(editType.equals("UPDATE")) {
            Log.d("sbagliato", "non ok");
            updateNoteMarker(inputLatitude, inputLongitude, noteTitle, index);
        } else if(editType.equals("DELETE")) {
            deleteNoteMarker(inputLatitude, inputLongitude, index);
        } else if(editType.equals("DELETE_ALL")){
            deleteAllNoteMarkers();
        }
    }




    private void addContactMarker(String inputLatitude, String inputLongitude, String contactTitle, int index){
        Double lat = Double.parseDouble(inputLatitude);
        Double lng = Double.parseDouble(inputLongitude);
        if(contactTitle.equals(" "))
            contactTitle = "NESSUN NOME";

        MapsFragment.newMarker = MapsFragment.mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                .title(contactTitle));

        MapsFragment.newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.contact_icon_map));
        MapsFragment.newMarker.setTag(index);
        Toast.makeText(this, "Aggiunto index " + index, Toast.LENGTH_SHORT).show();

        MapsFragment.contactMarkers.add(MapsFragment.newMarker);
    }

    private void updateContactMarker(String inputLatitude, String inputLongitude, String contactTitle, Integer oldIndex) {
        Double lat = Double.parseDouble(inputLatitude);
        Double lng = Double.parseDouble(inputLongitude);
        boolean found = false;
        if(contactTitle.equals(" "))
            contactTitle = "NESSUN NOME";

        for(int i = 0; i < MapsFragment.contactMarkers.size(); ++i) {
            if(MapsFragment.contactMarkers.get(i).getTag() == oldIndex) {
                MapsFragment.contactMarkers.get(i).setPosition(new LatLng(lat, lng));
                MapsFragment.contactMarkers.get(i).setTitle(contactTitle);
                Toast.makeText(this, "Aggiornato index " + i, Toast.LENGTH_SHORT).show();
                found = true;
            }
        }

        if(!found) {
            addContactMarker(inputLatitude, inputLongitude, contactTitle, oldIndex);
        }
    }

    private void deleteContactMarker(String inputLatitude, String inputLongitude, Integer index) {
        Log.d("test100", "index: " + index);
        Toast.makeText(this, "index e' " + index, Toast.LENGTH_LONG).show();
        for(int i = 0; i < MapsFragment.contactMarkers.size(); ++i) {

            if((MapsFragment.contactMarkers.get(i).getTag()).equals(index)) {
                Log.d("test100", "sono entratooo, il tag e' " + MapsFragment.contactMarkers.get(i).getTag());
                //Toast.makeText(this, "il tag e' " + MapsFragment.contactMarkers.get(i).getTag(), Toast.LENGTH_LONG).show();
                MapsFragment.contactMarkers.get(i).remove(); //rimuovo dalla mappa
                MapsFragment.contactMarkers.remove(i);       //rimuovo da vettore di marker
                Log.d("PROVAF", "rimosso il marker con index " + index);
            }
        }
        /*
        //Se devo eliminare anche la nota oltre al marker, aggiorno tutti i tag dei marker successivi, altrimenti se devo
        //eliminare solo il marker non entro nell'if (caso di update)
        if(!inputLatitude.equals("REMOVE_MARKER")) {
            for (int i = 0; i < MapsFragment.contactMarkers.size(); ++i) {
                if ((Integer) MapsFragment.contactMarkers.get(i).getTag() > index) {
                    Log.d("FATTO", "sono entrato");
                    MapsFragment.contactMarkers.get(i).setTag((Integer) MapsFragment.contactMarkers.get(i).getTag() - 1);
                }
            }
        }*/
    }

    private void deleteAllContactMarkers(){
        for(Marker m : MapsFragment.contactMarkers){
            m.remove();
        }
        MapsFragment.contactMarkers.clear();
    }


    @Override
    public void onInputContactSent(String inputLatitude, String inputLongitude, String editType, String contactTitle, int index) {
        if(editType.equals("ADD")) {
            addContactMarker(inputLatitude, inputLongitude, contactTitle, index);
        } else if(editType.equals("UPDATE")) {
            Log.d("giusto", "ok");
            updateContactMarker(inputLatitude, inputLongitude, contactTitle, index);
        } else if(editType.equals("DELETE")) {
            deleteContactMarker(inputLatitude, inputLongitude, index);
        } else if(editType.equals("DELETE_ALL")){
            deleteAllContactMarkers();
        }
    }
}
