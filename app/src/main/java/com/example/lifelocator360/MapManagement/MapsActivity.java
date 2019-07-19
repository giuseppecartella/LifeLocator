package com.example.lifelocator360.MapManagement;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.lifelocator360.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    String TAG = "MapsActivity";
    private FusedLocationProviderClient fusedLocationProviderClient;
    boolean storagePermissionGranted;
    boolean locationPermissionGranted;
    final float DEF_ZOOM = 15f;

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the device current location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (locationPermissionGranted) {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {

                            Location currentLocation = (Location) task.getResult();
                            Log.d(TAG, "onComplete: found location!" + "  " + currentLocation.getLatitude() + "  " + currentLocation.getLongitude());

                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                            moveCamera(latLng, DEF_ZOOM);
                            Log.d(TAG, "Camera Moved Succesfully");


                            mMap.addMarker(new MarkerOptions().position(latLng).title("current position"));
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //get data from Splash Activity
        Intent intentMaps = getIntent();
        storagePermissionGranted = intentMaps.getBooleanExtra("storagePermissionGranted", false);
        locationPermissionGranted = intentMaps.getBooleanExtra("locationPermissionGranted", false);
    }


    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (locationPermissionGranted) {
            getDeviceLocation();
        }
    }
}