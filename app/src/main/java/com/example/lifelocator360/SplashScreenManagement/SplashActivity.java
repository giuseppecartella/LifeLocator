package com.example.lifelocator360.SplashScreenManagement;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.example.lifelocator360.DataBaseManagement.AppDataBase;
import com.example.lifelocator360.DataBaseManagement.Contact;
import com.example.lifelocator360.DataBaseManagement.Note;
import com.example.lifelocator360.DataBaseManagement.Photo;
import com.example.lifelocator360.FragmentManagement.NotesFragment;
import com.example.lifelocator360.MapManagement.HttpDataHandler;
import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

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
    private ArrayList<Contact> contacts;
    private ArrayList<Note> notes;
    private ArrayList<Photo> photos;
    //Salvo in una lista tutti i nuovi pathfile
    private ArrayList<File> photosNewPaths;
    private String allInformationO1;
    private String allInformationO2;
    private Timer timer;
    private TimerTask timerTask;
    private static int numNoInternet = 0;  //variabili che all inizio vale il num di nointernet
    //e ad ogni richiesta fallita/eseguita decrementa di uno
    //quando vale zero il timer si ferma e parte la  nav drawer.

    private boolean isConnectionAvailable() {
        return connectionAvailable;
    }

    public void setConnectionAvailable(boolean connectionAvailable) {

        this.connectionAvailable = checkNetworkConnectionStatus();
    }


    /**
     *
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


    public void getPhotoPaths(File path,FilenameFilter filenameFilter){
        File[] tmp;

        //Ottengo effettivamente la lista dei path
        tmp = path.listFiles(filenameFilter);

        Log.d("FATTO", "trovati " + tmp.length + " elementi dir "+path);
        for(int i = 0; i < tmp.length; i++) {
            if(tmp[i].isDirectory() && !tmp[i].isHidden()){
                getPhotoPaths(tmp[i],filenameFilter);
            }else{
                photosNewPaths.add(tmp[i]);
            }
        }
    }

    private void setUpDatabaseAndLaunchMainActivity() {
        //A questo punto posso creare il db (spostare in una funzione!)
        appDataBase = Room.databaseBuilder(getApplicationContext(), AppDataBase.class, DBName).allowMainThreadQueries().build();
        //prendo dal database i dati necessari
        contacts = (ArrayList<Contact>) SplashActivity.appDataBase.daoManager().getContacts();
        notes = (ArrayList<Note>) SplashActivity.appDataBase.daoManager().getNote();


        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {

                allInformationO1 = o1.getAllInformation(o1);
                allInformationO2 = o2.getAllInformation(o2);

                if (allInformationO1.compareToIgnoreCase(allInformationO2) < 0) {
                    return -1;
                } else if (allInformationO1.compareToIgnoreCase(allInformationO2) == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        /////////////////////////////////////////
        for (Note n : notes) {
            if (n.getLatitude().equals("NO_INTERNET")) {
                numNoInternet++;
            }
        }

        for (Contact n : contacts) {
            if (n.getLatitude().equals("NO_INTERNET")) {
                numNoInternet++;
            }
        }

        //ATTENZIONE PASSIAMO L ID E NON INDEX COSI SONO SICURO CHE SIA PER LE NOTE CHE EPR I CONTATTI CE CORRISPONDENZA CON IL DATABASE
        for (int i = 0; i < notes.size(); ++i) {
            if (notes.get(i).getLatitude().equals("NO_INTERNET")) {
                new GetCoordinates().execute(notes.get(i).getPosition().replace(" ", "+"), Integer.toString(notes.get(i).getId()), Integer.toString(i), "NOTE");
            }
        }

        for (int i = 0; i < contacts.size(); ++i) {
            if (contacts.get(i).getLatitude().equals("NO_INTERNET")) {
                new GetCoordinates().execute(contacts.get(i).getAddress().replace(" ", "+"), Integer.toString(contacts.get(i).getId()), Integer.toString(i), "CONTACT");
            }
        }


        /////////////////////////////////////////
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (numNoInternet == 0) {
                    timer.cancel();
                    timer.purge();
                    launchMainActivity();

                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 20, 20);

        //Ora accedo all'archivio dell'utente, e aggiorno il DB delle foto se ce ne sono di nuove/cancellate
        //Solo se l'utente mi ha dato il permesso
        if(storagePermissionGranted()) {

            //Ottengo il path delle foto scattate con la fotocamera
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            photosNewPaths = new ArrayList<File>();

            //Impongo un filtro solo per formati jpg, jpeg e png
            FilenameFilter photoFilter = new FilenameFilter() {
                File f;
                public boolean accept(File dir, String name) {
                    if((name.endsWith(".jpg") || name.endsWith(".JPG")|| name.endsWith(".png")|| name.endsWith(".PNG")|| name.endsWith(".jpeg")|| name.endsWith(".JPEG")) && !name.startsWith(".") && !dir.isHidden()) {
                       // Log.d("PROVA", "elemtno nome:  " + name + " " + " nome dir " + dir);
                        return true;
                    }

                    if(!name.startsWith(".")) {
                        f = new File(dir.getAbsolutePath() + "/" + name);
                        return f.isDirectory();
                    }
                    return false;
                }
            };

            getPhotoPaths(path,photoFilter);
            Log.d("FINITO", "finito, trovati " + photosNewPaths.size() + " elementi");

            //Ora ho in photos tutti i path AGGIORNATI dalla libreria del telefono

        } else {
            //L'utente mi ha negato i permessi, il vettore è vuoto
            photosNewPaths = new ArrayList<File>();
        }

    }

    private void launchMainActivity() {
        final Intent intentNavigationDrawer = new Intent(this, NavigationDrawerActivity.class);

        intentNavigationDrawer.putExtra("lista_contatti", contacts); //passo i contatti al nav drawer
        intentNavigationDrawer.putExtra("lista_note", notes);
        intentNavigationDrawer.putExtra("lista_photo_path", photosNewPaths);
        startActivity(intentNavigationDrawer);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {


        if (requestCode == ALL_PERMISSION_CODE) {
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

    public class Wrapper {
        public String dataType;
        public String response;
        public Integer id;
        public Integer index;
    }

    private class GetCoordinates extends AsyncTask<String, Void, SplashActivity.Wrapper> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("prova", "sono nel preexecute");

        }

        @Override
        protected SplashActivity.Wrapper doInBackground(String... strings) {
            try {
                String address = strings[0];
                Integer id = Integer.parseInt(strings[1]);
                Integer index = Integer.parseInt(strings[2]);
                String dataType = strings[3];

                Wrapper wrapper = new Wrapper();
                HttpDataHandler httpDataHandler = new HttpDataHandler();
                String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=AIzaSyAV3-Tn-8X4CjWVDTrVhSGDQrbAdEsdjuc";
                Log.d("tag", "url vale " + url);
                Log.d("prova", "sto per fare il gethttpdata");
                wrapper.response = httpDataHandler.getHTTPData(url);
                wrapper.id = id;
                wrapper.index = index;
                wrapper.dataType = dataType;
                return wrapper;
            } catch (Exception e) {
                if (strings[3].equals("NOTE")) {
                    SplashActivity.appDataBase.daoManager().updateLatLngNotes("NO_RESULT", "NO_RESULT", strings[1]);
                    notes.get(Integer.parseInt(strings[2])).setLatitude("NO_RESULT");
                    notes.get(Integer.parseInt(strings[2])).setLongitude("NO_RESULT");
                } else if (strings[3].equals("CONTACT")) {
                    SplashActivity.appDataBase.daoManager().updateLatLngContacts("NO_RESULT", "NO_RESULT", strings[1]);
                    contacts.get(Integer.parseInt(strings[2])).setLatitude("NO_RESULT");
                    contacts.get(Integer.parseInt(strings[2])).setLongitude("NO_RESULT");
                }

                numNoInternet--;
                Log.d("richiesta", "Salvataggio con risultato assente");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Wrapper wrapper) {
            try {
                Log.d("prova", "dati arrivati: gestisco il json");
                JSONObject jsonObject = new JSONObject(wrapper.response);
                String lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();

                String lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();
                Log.d("prova", "latlng: " + lat + lng);
                Log.d("richiesta", "Salvataggio con coordinate");

                if (wrapper.dataType.equals("NOTE")) {
                    SplashActivity.appDataBase.daoManager().updateLatLngNotes(lat, lng, Integer.toString(wrapper.id));
                    notes.get(wrapper.index).setLatitude(lat);
                    notes.get(wrapper.index).setLongitude(lng);
                } else if (wrapper.dataType.equals("CONTACT")) {
                    SplashActivity.appDataBase.daoManager().updateLatLngContacts(lat, lng, Integer.toString(wrapper.id));
                    contacts.get(wrapper.index).setLatitude(lat);
                    contacts.get(wrapper.index).setLongitude(lng);
                }
                numNoInternet--;

            } catch (JSONException e) {

                if (HttpDataHandler.timeOutException) { //ATTENZIONE: QUESTO RAGIONAMENTO CON UNA SOLA VARIABILE STATICA FUNZIONA SOLO SE LE RICHIESTE SONO SEQUENZIALI
                    HttpDataHandler.timeOutException = false;

                    Log.d("GESTISCO", "trovato uno con connessione lenta");

                    if (wrapper.dataType.equals("NOTE")) {
                        SplashActivity.appDataBase.daoManager().updateLatLngNotes("NO_INTERNET", "NO_INTERNET", Integer.toString(wrapper.id));
                        notes.get(wrapper.index).setLatitude("NO_INTERNET");
                        notes.get(wrapper.index).setLongitude("NO_INTERNET");
                    } else if (wrapper.dataType.equals("CONTACT")) {
                        SplashActivity.appDataBase.daoManager().updateLatLngContacts("NO_INTERNET", "NO_INTERNET", Integer.toString(wrapper.id));
                        contacts.get(wrapper.index).setLatitude("NO_INTERNET");
                        contacts.get(wrapper.index).setLongitude("NO_INTERNET");
                    }

                } else {

                    Log.d("GESTISCO", "trovato uno con no resutl");

                    if (wrapper.dataType.equals("NOTE")) {
                        SplashActivity.appDataBase.daoManager().updateLatLngNotes("NO_RESULT", "NO_RESULT", Integer.toString(wrapper.id));
                        notes.get(wrapper.index).setLatitude("NO_RESULT");
                        notes.get(wrapper.index).setLongitude("NO_RESULT");
                    } else if (wrapper.dataType.equals("CONTACT")) {
                        SplashActivity.appDataBase.daoManager().updateLatLngContacts("NO_RESULT", "NO_RESULT", Integer.toString(wrapper.id));
                        contacts.get(wrapper.index).setLatitude("NO_RESULT");
                        contacts.get(wrapper.index).setLongitude("NO_RESULT");
                    }

                    Log.d("richiesta", "Salvataggio con risultato assente");
                }
                numNoInternet--;
                e.printStackTrace();
            }
        }
    }

}