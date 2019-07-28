package com.example.lifelocator360.NavigationDrawerManagement;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import java.util.Timer;
import java.util.TimerTask;


public class NavigationDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private String currentFragment = "maps";
    private int navigationDrawerSize;
    private static float DEF_ZOOM = 15.0f;
    private Timer timer;
    private TimerTask timerTask;
    private FusedLocationProviderClient fusedLocationProviderClient;



    public int getNavigationDrawerSize() {
        return navigationDrawerSize;
    }

    public void setNavigationDrawerSize() {
        this.navigationDrawerSize = navigationView.getMenu().size();
    }

    public void uncheckAllNavigationItems() {
        setNavigationDrawerSize();

        for (int i = 0; i < getNavigationDrawerSize(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    private void returnToMap() {
        currentFragment = "maps";
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
        if(currentFragment.equals("maps"))
            returnToMap.setVisible(false);
        else
            returnToMap.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home: {
                if (!currentFragment.equals("maps")) {
                    returnToMap();
                }
                return true;
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
                if (!currentFragment.equals("maps")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new PhotoFragment()).addToBackStack("stack1").commit();
                currentFragment = "photo";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_calendar:
                if (!currentFragment.equals("maps")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new CalendarFragment()).addToBackStack("stack1").commit();
                currentFragment = "calendar";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_contacts:
                if (!currentFragment.equals("maps")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new ContactsFragment()).addToBackStack("stack1").commit();
                currentFragment = "contacts";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_notes:
                if (!currentFragment.equals("maps")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new NotesFragment()).addToBackStack("stack1").commit();
                currentFragment = "notes";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_instagram:
                if (!currentFragment.equals("maps")) {
                    getSupportFragmentManager().popBackStack();
                }

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new InstagramFragment()).addToBackStack("stack1").commit();
                currentFragment = "instagram";
                //Show return to map button
                this.invalidateOptionsMenu();

                break;

            case R.id.nav_settings:
                if (!currentFragment.equals("maps")) {
                    getSupportFragmentManager().popBackStack();
                }

                navigationView.getMenu().getItem(5).setChecked(true);

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new SettingsFragment()).addToBackStack("stack1").commit();
                currentFragment = "settings";
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
            if (!currentFragment.equals("maps"))
                returnToMap();
            else
                super.onBackPressed();
        }
    }

    /*
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        final LocationManager manager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        outState.putBoolean("gps_state",manager.isProviderEnabled(manager.GPS_PROVIDER));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        MapsFragment.GPSActive = savedInstanceState.getBoolean("gps_state");
    }*/
}
