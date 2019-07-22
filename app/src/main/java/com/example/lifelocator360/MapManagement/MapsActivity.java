package com.example.lifelocator360.MapManagement;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;
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
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String TAG = "MapsActivity";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final float DEF_ZOOM = 15f;
    boolean GPSActive;


    public boolean isGPSActive() {
        return GPSActive;
    }

    public void setGPSActive(boolean GPS) {
        GPSActive = GPS;
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the device current location");
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (locationPermissionGranted() && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                setGPSActive(true); // flag maintain before get location
            }
        }
    }


    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


    public boolean storagePermissionGranted() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    public boolean locationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "PARTITO L'ON CREATE");


        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (locationPermissionGranted() && !manager.isProviderEnabled(manager.GPS_PROVIDER)) {
            new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {
                    setGPSActive(isGPSEnable);
                }
            });
        }


        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "PARTITO L'ON MAP READY");
        mMap = googleMap;

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        Timer timerObj = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
               if (locationPermissionGranted() && manager.isProviderEnabled(manager.GPS_PROVIDER))
                   getDeviceLocation();
            }
        };

        timerObj.scheduleAtFixedRate(timerTask, 0, 100);

    }
}