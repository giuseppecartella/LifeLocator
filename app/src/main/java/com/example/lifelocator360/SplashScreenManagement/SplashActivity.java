package com.example.lifelocator360.SplashScreenManagement;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifelocator360.MapManagement.MapsActivity;
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


    /**
     * @param savedInstanceState
     * The method calls the MapsActivity if connection is available,
     * else shows an alert dialog.
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intentMaps = new Intent(this, MapsActivity.class);
        final Intent intentSplash = new Intent(this, SplashActivity.class);


        setConnectionAvailable(connectionAvailable);
        if (isConnectionAvailable() && isServicesOK()) {
            startActivity(intentMaps);
            finish();

        } else if (!isConnectionAvailable()) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("No internet Connection");
            builder.setMessage("Please turn on internet connection to continue");
            builder.setCancelable(false);

            builder.setPositiveButton("TRY AGAIN", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(intentSplash);
                    overridePendingTransition(0,0);
                    finish();
                    overridePendingTransition(0,0);
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
}