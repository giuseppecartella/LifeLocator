package com.example.lifelocator360.SplashScreenManagement;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.example.lifelocator360.DataBaseManagement.AppDataBase;
import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * The class shows a SplashScreen during the app loading.
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Variabile to check if connection is available or not
     */
    private boolean connectionAvailable;

    //Servono per controllare google play services
    private static final String TAG = "SplashActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    //Servono per gestire i permessi
    final private int ALL_PERMISSION_CODE = 1;

    //Variabili per la gestione del Data Base
    public static AppDataBase appDataBase;
    public static String DBName = "APP_DB";

    private boolean isConnectionAvailable() {
        return connectionAvailable;
    }

    public void setConnectionAvailable(boolean connectionAvailable) {
        this.connectionAvailable = checkNetworkConnectionStatus();
    }

    /**
     * @return true if connection is available,false if not
     */
    private boolean checkNetworkConnectionStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected())
            return true;
        else
            return false;
    }

    //Funzione per controllare google play services
    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(SplashActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(SplashActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
            dialog.setCancelable(false);
        } else {
            AlertDialog.Builder builderGooglePlayServicesFatalError;
            builderGooglePlayServicesFatalError = new AlertDialog.Builder(this);
            builderGooglePlayServicesFatalError.setTitle("Google Play Services error");
            builderGooglePlayServicesFatalError.setMessage(R.string.app_name + "can't start!");

            builderGooglePlayServicesFatalError.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    System.exit(-1);

                }
            });
            AlertDialog alertDialog = builderGooglePlayServicesFatalError.create();
            alertDialog.show();
            builderGooglePlayServicesFatalError.setCancelable(false);
        }
        return false;
    }

    public boolean storagePermissionGranted() {
        if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    public boolean locationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    public void checkPermissionsSetUpDatabaseAndLauchMainActivity() {
        if (!storagePermissionGranted() || !locationPermissionGranted()) {
            Log.d(TAG, "Requesting permissions");
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, ALL_PERMISSION_CODE);
        } else {
            Log.d(TAG, "Permission already granted");
            setUpDatabaseAndLaunchMainActivity();
        }
    }

    private void createAlertDialogNoConnection() {
        final Intent intentSplash = new Intent(this, SplashActivity.class);

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Rete non disponibile");
        builder.setMessage("Verifica la tua connessione Internet e riprova");
        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(intentSplash);
                overridePendingTransition(0, 0);
                finish();
                overridePendingTransition(0, 0);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setUpDatabaseAndLaunchMainActivity() {
        final Intent intentNavigationDrawer = new Intent(this, NavigationDrawerActivity.class);

        //A questo punto posso creare il db (spostare in una funzione!)
        appDataBase = Room.databaseBuilder(getApplicationContext(), AppDataBase.class, DBName).allowMainThreadQueries().build();

        startActivity(intentNavigationDrawer);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {


        if (requestCode == ALL_PERMISSION_CODE) {
            /*
            //Probabilmente questi controlli non servono e possono essere fatti successivamente
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setUpDatabaseAndLaunchMainActivity();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setUpDatabaseAndLaunchMainActivity();

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                setUpDatabaseAndLaunchMainActivity();

            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                setUpDatabaseAndLaunchMainActivity();
            }
            */

            //Avvio il caricamento del database, e successivamente avvio la Main Activity
            setUpDatabaseAndLaunchMainActivity();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * @param savedInstanceState The method calls the MapsActivity if connection is available, else shows an alert dialog.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setConnectionAvailable(connectionAvailable);

        //Il primo if controlla che ci siano la connessione e i google play services (NECESSARI), se questi ci sono,
        // avvio il check delle permissions e il set up del database. Infine vado alla main activity
        if (isConnectionAvailable() && isServicesOK()) {
            checkPermissionsSetUpDatabaseAndLauchMainActivity();
        } else if (!isConnectionAvailable()) { //Controllo solo questo caso alternativo, perche !isServicesOK è controllato direttamente dalla funzione isServicesOK
            createAlertDialogNoConnection();
        }
    }
}