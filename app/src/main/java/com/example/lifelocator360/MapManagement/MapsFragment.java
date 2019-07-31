package com.example.lifelocator360.MapManagement;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    public static GoogleMap mMap;
    private String TAG = "MapsActivity";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final float DEF_ZOOM = 15f;
    public static boolean GPSActive;
    public static ArrayList<Marker> noteMarkers;
    public static Marker newMarker;

    public boolean isGPSActive() {
        return GPSActive;
    }

    public void setGPSActive(boolean GPS) {
        GPSActive = GPS;
    }

    public void getDeviceLocation() {
        Log.d(TAG, "getting the device current location!");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
            if (locationPermissionGranted()) {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "location found!");

                            Location currentLocation = (Location) task.getResult();

                            if (currentLocation != null) {
                                Log.d(TAG, "location vale " + currentLocation);
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEF_ZOOM);
                            }

                        } else {
                            Log.d(TAG, "current location is null!");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception " + e.getMessage());
        }
    }

    public static void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public boolean storagePermissionGranted() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    public boolean locationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    private void setNoteMarkers() {
        for(int i = 0; i < NavigationDrawerActivity.notes.size(); ++i) {
            String validPosition = NavigationDrawerActivity.notes.get(i).getLongitude();
            String noteTitle;
            if(!validPosition.equals("NO_INTERNET") && !validPosition.equals("NO_ADDRESS") && !validPosition.equals("NO_RESULT")) {
                Double lat = Double.parseDouble(NavigationDrawerActivity.notes.get(i).getLatitude());
                Double lng =  Double.parseDouble(NavigationDrawerActivity.notes.get(i).getLongitude());

                if(NavigationDrawerActivity.notes.get(i).getName().isEmpty())
                    noteTitle = "NESSUN TITOLO";
                else
                    noteTitle = NavigationDrawerActivity.notes.get(i).getName();

                MapsFragment.newMarker = MapsFragment.mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                        .title(noteTitle));

                MapsFragment.newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.note_icon_map));
                MapsFragment.newMarker.setTag(i);

                MapsFragment.noteMarkers.add(MapsFragment.newMarker);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "PARTITO L'ON MAP READY");
        mMap = googleMap;


        //Setto tutti i markers
        setNoteMarkers();


        final LocationManager manager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        if (locationPermissionGranted() && manager.isProviderEnabled(manager.GPS_PROVIDER)) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            setGPSActive(true); // flag maintain before get location
            mMap.setMyLocationEnabled(true);
        } else if (locationPermissionGranted() && !manager.isProviderEnabled(manager.GPS_PROVIDER)) {
            new GpsUtils(getContext()).turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {
                    setGPSActive(isGPSEnable);
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inizializzo il vettore per i markers
        noteMarkers = new ArrayList<Marker>();

        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }
}


