package com.example.lifelocator360.MapManagement;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;
import static com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity.DEF_ZOOM;
import static com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity.contacts;
import static com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity.photos;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static GoogleMap mMap;
    private String TAG = "MapsActivity";
    private FusedLocationProviderClient fusedLocationProviderClient;
    public static boolean GPSActive;
    public static ArrayList<Marker> noteMarkers;
    public static ArrayList<Marker> contactMarkers;
    public static Marker newMarker;

    public static int tmp = 0;

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

    private void setContactMarkers() {
        for(int i = 0; i < NavigationDrawerActivity.contacts.size(); ++i) {
            String validPosition = NavigationDrawerActivity.contacts.get(i).getLongitude();
            String contactTitle;
            if(!validPosition.equals("NO_INTERNET") && !validPosition.equals("NO_ADDRESS") && !validPosition.equals("NO_RESULT")) {
                Double lat = Double.parseDouble(NavigationDrawerActivity.contacts.get(i).getLatitude());
                Double lng =  Double.parseDouble(NavigationDrawerActivity.contacts.get(i).getLongitude());

                if(NavigationDrawerActivity.contacts.get(i).getName().isEmpty() && NavigationDrawerActivity.contacts.get(i).getSurname().isEmpty())
                    contactTitle = "NESSUN NOME";
                else
                    contactTitle = NavigationDrawerActivity.contacts.get(i).getName()+" "+NavigationDrawerActivity.contacts.get(i).getSurname();

                MapsFragment.newMarker = MapsFragment.mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                        .title(contactTitle));

                MapsFragment.newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.contact_icon_map));
                MapsFragment.newMarker.setTag(NavigationDrawerActivity.contacts.get(i).getId());

                MapsFragment.contactMarkers.add(MapsFragment.newMarker);
            }
        }
    }


    private Bitmap addMarkerBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }



    private void loadMarkerIcon(final Marker marker, File file) {

        Uri imageUri = Uri.fromFile(file);

        Glide.with(getActivity()).asBitmap().load(imageUri).apply(new RequestOptions().override(70, 70)).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                resource =  addMarkerBorder(resource, 6);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resource);
                marker.setIcon(icon);
                tmp++;
                Log.d("TMP", "Vale: " + tmp);
            }
        });
    }


    private void setPhotoMarkers() {
        for(File f : NavigationDrawerActivity.photos) {
            String filePath = f.getAbsolutePath();

            //Leggo i metadata del file
            try {
                ExifInterface exifInterface = new ExifInterface(filePath);

                float[] latlng = new float[2];

                exifInterface.getLatLong(latlng);

                if(latlng[0] != 0 || latlng[1] != 0) {

                    Log.e("LATLNG", "Latitudine: " + latlng[0] + " Longitudine: " + latlng[1]);

                    MapsFragment.newMarker = MapsFragment.mMap.addMarker(new MarkerOptions().position(new LatLng(latlng[0], latlng[1]))
                            .title("FOTO"));

                    loadMarkerIcon(MapsFragment.newMarker, f);
               }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "PARTITO L'ON MAP READY");
        mMap = googleMap;


        //Setto tutti i markers
        setPhotoMarkers();
        setNoteMarkers();
        setContactMarkers();



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
        contactMarkers = new ArrayList<Marker>();

        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.remove();
        return true;
    }
}


