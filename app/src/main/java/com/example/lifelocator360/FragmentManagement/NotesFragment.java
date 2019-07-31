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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelocator360.DataBaseManagement.Note;
import com.example.lifelocator360.MapManagement.HttpDataHandler;
import com.example.lifelocator360.NavigationDrawerManagement.NavigationDrawerActivity;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment implements View.OnClickListener {
    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton floatingActionButton;
    private View view;
    private EditText editTextName;
    private EditText editTextPosition;
    private EditText editTextNoteText;
    private String name;
    private String position;
    private String textNote;
    private ImageView imageMissingNote;
    private TextView textMissingNote;
    private EditText updateTextName;
    private EditText updateTextPosition;
    private EditText updateTextNoteText;
    private static boolean isNewNote; //Serve per on post execute, per sapere se chiamare nuova nota o aggiornarla
    private static int oldIndex;


    public NotesFragment() {

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
        view = inflater.inflate(R.layout.fragment_notes, container, false);

        editTextName = view.findViewById(R.id.edit_note_name);
        editTextPosition = view.findViewById(R.id.edit_note_position);
        editTextNoteText = view.findViewById(R.id.edit_note_text);
        floatingActionButton = view.findViewById(R.id.addNote);
        imageMissingNote = view.findViewById(R.id.image_missing_notes);
        textMissingNote = view.findViewById(R.id.text_missing_notes);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNoteDialog();
            }
        });

        showNotes();
        return view;
    }

    public void updateMissingNotesBackground() {
        if (NavigationDrawerActivity.notes.isEmpty()) {
            imageMissingNote.setVisibility(View.VISIBLE);
            textMissingNote.setVisibility(View.VISIBLE);
        } else {
            imageMissingNote.setVisibility(View.INVISIBLE);
            textMissingNote.setVisibility(View.INVISIBLE);
        }
    }

    public void showNotes() {
        updateMissingNotesBackground();

        //setto il recycler view
        recyclerView = view.findViewById(R.id.recyclerViewNotes);

        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(getContext(), 2);
        notesAdapter = new NotesAdapter(NavigationDrawerActivity.notes);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(notesAdapter);
        notesAdapter.setOnItemClickListener(new NotesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                updateNoteInfoDialog(position);
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addNote: {
                showAddNoteDialog();
                break;
            }
        }
    }

    private void saveNote(String name, String position, String textNote, String latitude, String longitude) {
        Note note = new Note(name, position, textNote, latitude, longitude);
        int index = NavigationDrawerActivity.notes.size();
        NavigationDrawerActivity.notes.add(index, note);
        SplashActivity.appDataBase.daoManager().addNote(note);
        notesAdapter.notifyItemInserted(index);
        updateMissingNotesBackground();

        Log.d("richiesta", "Salvataggio con coordinate dal save note");

        //Imposto il marker
        if (latitude.equals("NO_INTERNET") || latitude.equals("NO_ADDRESS") || latitude.equals("NO_RESULT")) {
            Log.d("richiesta", "salvata nota con errore: " + note.getLatitude());
        } else {
            Log.d("richiesta", "salvata nota con coordinate: " + note.getLatitude() + " " + note.getLongitude());

            //Invio i dati alla mappa tramite il navigation drawer
            String inputLatitude = note.getLatitude();
            String inputLongitude = note.getLongitude();
            noteFramentListener.onInputNoteSent(inputLatitude, inputLongitude, "ADD", name, index);
        }


       // Toast.makeText(getActivity(), "Nota salvata!", Toast.LENGTH_SHORT).show();
    }


    //////////////////////////////////////////////////////GESTORE COMUNICAZIONE CON MAPPA///////////////////////////
    private NoteFramentListener noteFramentListener;

    public interface NoteFramentListener {
        void onInputNoteSent(String inputLatitude, String inputLongitude, String editType, String noteTitle, int index);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NoteFramentListener) {
            noteFramentListener = (NoteFramentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement NoteFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        noteFramentListener = null;
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

    private void onSaveClicked() {
        name = editTextName.getText().toString();
        position = editTextPosition.getText().toString();
        textNote = editTextNoteText.getText().toString();


        if (name.isEmpty() && position.isEmpty() && textNote.isEmpty()) {
            Toast.makeText(getActivity(), "Nota non salvata!", Toast.LENGTH_SHORT).show();
        } else {

            if (!checkNetworkConnectionStatus()) {
                saveNote(name, position, textNote, "NO_INTERNET", "NO_INTERNET");
                Log.d("richiesta", "Salvataggio con connessione assente");
            } else if (position.isEmpty()) {
                saveNote(name, position, textNote, "NO_ADDRESS", "NO_ADDRESS");
                Log.d("richiesta", "Salvataggio con indirizzo assente");
            } else {
                Log.d("richiesta", "Provo il salvataggio con indirizzo");
                isNewNote = true;
                new GetCoordinates().execute(position.replace(" ", "+"), "ADD");
            }


        }
    }


    public void onUpdateClicked(int index, EditText noteName, EditText notePosition, EditText noteText) {

        String oldAddress = NavigationDrawerActivity.notes.get(index).getPosition();
        String newAddress = notePosition.getText().toString();

        name = noteName.getText().toString();
        position = notePosition.getText().toString();
        textNote = noteText.getText().toString();

        if (oldAddress.equals(newAddress)) {
            updateNote(name, position, textNote, NavigationDrawerActivity.notes.get(oldIndex).getLatitude(), NavigationDrawerActivity.notes.get(oldIndex).getLongitude());
        } else {
            if (!checkNetworkConnectionStatus()) {
                oldIndex = index;
                updateNote(name, position, textNote, "NO_INTERNET", "NO_INTERNET");
                Log.d("richiesta", "Modifica con connessione assente");
            } else if (notePosition.getText().toString().isEmpty()) {
                oldIndex = index;
                updateNote(name, position, textNote, "NO_ADDRESS", "NO_ADDRESS");
                Log.d("richiesta", "Salvataggio con indirizzo assente");
            } else {
                isNewNote = false;
                oldIndex = index;
                Log.d("richiesta", "Provo il salvataggio con indirizzo");
                new GetCoordinates().execute(newAddress.replace(" ", "+"));
            }
        }

    }

    private void updateNote(String name, String position, String textNote, String lat, String lng) {
        Note note = new Note();
        note.setId(NavigationDrawerActivity.notes.get(oldIndex).getId());
        note.setName(name);
        note.setPosition(position);
        note.setText(textNote);
        note.setLatitude(lat);
        note.setLongitude(lng);

        Log.d("updatenote: ", NavigationDrawerActivity.notes.get(oldIndex).getId() + name + position + textNote + lat + lng);

        NavigationDrawerActivity.notes.remove(oldIndex);
        notesAdapter.notifyItemRemoved(oldIndex);

        NavigationDrawerActivity.notes.add(oldIndex, note);
        notesAdapter.notifyItemInserted(oldIndex);
        SplashActivity.appDataBase.daoManager().updateNote(note);

        //Eventualmente sposto il marker
        if (note.getLatitude().equals("NO_INTERNET") || note.getLatitude().equals("NO_ADDRESS") || note.getLatitude().equals("NO_RESULT")) {
           noteFramentListener.onInputNoteSent("NO_LATITUDE", "NO_LONGITUDE", "DELETE", name, oldIndex);
            Log.d("richiesta", "aggiornata nota con errore: " + note.getLatitude());
        } else {
            Log.d("richiesta", "aggiornata nota con coordinate: " + note.getLatitude() + " " + note.getLongitude());

            //Invio i dati alla mappa tramite il navigation drawer
            String inputLatitude = note.getLatitude();
            String inputLongitude = note.getLongitude();
            noteFramentListener.onInputNoteSent(inputLatitude, inputLongitude, "UPDATE", name, oldIndex);
        }

    }

    public void safeDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Questa nota verrà eliminata")
                .setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("ELIMINA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteNote(position);
                    }
                });
        builder.create().show();
    }

    private void deleteAllNotes() {
        NavigationDrawerActivity.notes.clear();
        SplashActivity.appDataBase.daoManager().deleteAllNotes();
        notesAdapter.notifyDataSetChanged();
        updateMissingNotesBackground();
    }

    public void safeDeleteAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Tutte le note verranno eliminate")
                .setNegativeButton("ANNULLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("ELIMINA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllNotes();
                    }
                });
        builder.create().show();
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

    private void deleteNote(int index) {
        Integer id = NavigationDrawerActivity.notes.get(index).getId();
        Note note = new Note();
        note.setId(id);
        NavigationDrawerActivity.notes.remove(index);
        SplashActivity.appDataBase.daoManager().deleteNote(note);
        notesAdapter.notifyItemRemoved(index);
        updateMissingNotesBackground();

        noteFramentListener.onInputNoteSent("DELETING", "DELETING", "DELETE", name, index);
    }

    public void updateNoteInfoDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.update_note_dialog_layout, null);

        updateTextName = view.findViewById(R.id.update_note_name);
        updateTextPosition = view.findViewById(R.id.update_note_position);
        updateTextNoteText = view.findViewById(R.id.update_note_text);

        updateTextName.setText(NavigationDrawerActivity.notes.get(position).getName());
        updateTextPosition.setText(NavigationDrawerActivity.notes.get(position).getPosition());
        updateTextNoteText.setText(NavigationDrawerActivity.notes.get(position).getText());

        builder.setView(view)
                .setTitle("Modifica nota")
                .setPositiveButton("SALVA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onUpdateClicked(position, updateTextName, updateTextPosition, updateTextNoteText);
                    }
                })
                .setNegativeButton("MAPPA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("ELIMINA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        safeDeleteDialog(position);
                    }
                });
        builder.create().show();
    }

    public void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_note_dialog_layout, null);

        builder.setView(view)
                .setTitle("Nuova nota")
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

        editTextName = view.findViewById(R.id.edit_note_name);
        editTextPosition = view.findViewById(R.id.edit_note_position);
        editTextNoteText = view.findViewById(R.id.edit_note_text);
    }

    private class GetCoordinates extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("prova", "sono nel preexecute");

        }

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try {
                String address = strings[0];
                Log.d("prova"," vale "+name);
                HttpDataHandler httpDataHandler = new HttpDataHandler();
                String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=AIzaSyAV3-Tn-8X4CjWVDTrVhSGDQrbAdEsdjuc";
                Log.d("tag", "url vale " + url);
                Log.d("prova", "sto per fare il gethttpdata");
                response = httpDataHandler.getHTTPData(url);
                Log.d("valori",name+position+textNote);
                return response;
            } catch (Exception e) {
                if (isNewNote) {
                    saveNote(name, position, textNote, "NO_RESULT", "NO_RESULT");}
                else {
                    updateNote(name, position, textNote, "NO_RESULT", "NO_RESULT");
                }
                Log.d("richiesta", "Salvataggio con risultato assente");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                Log.d("prova", "dati arrivati: gestisco il json");
                JSONObject jsonObject = new JSONObject(s);
                String lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();

                String lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();
                Log.d("prova", "latlng: " + lat + lng);
                Log.d("richiesta", "Salvataggio con coordinate");

                if (isNewNote)
                    saveNote(name, position, textNote, lat, lng);
                else {
                    Log.d("prima valgono: ", name + position + textNote);
                    updateNote(name, position, textNote, lat, lng);
                }


            } catch (JSONException e) {

                if (isNewNote)
                    saveNote(name, position, textNote, "NO_RESULT", "NO_RESULT");
                else
                    updateNote(name, position, textNote, "NO_RESULT", "NO_RESULT");
                Log.d("richiesta", "Salvataggio con risultato assente");
                e.printStackTrace();
            }
        }
    }

}