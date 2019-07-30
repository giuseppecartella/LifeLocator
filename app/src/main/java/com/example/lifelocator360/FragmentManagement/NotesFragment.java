package com.example.lifelocator360.FragmentManagement;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelocator360.DataBaseManagement.Note;
import com.example.lifelocator360.R;
import com.example.lifelocator360.SplashScreenManagement.SplashActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class NotesFragment extends Fragment implements View.OnClickListener {
    private List<Note> notes;
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



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes,container,false);

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

    public void updateMissingNoteBackground() {
        if (notes.isEmpty()) {
            imageMissingNote.setVisibility(View.VISIBLE);
            textMissingNote.setVisibility(View.VISIBLE);
        } else {
            imageMissingNote.setVisibility(View.INVISIBLE);
            textMissingNote.setVisibility(View.INVISIBLE);
        }
    }

    public void showNotes() {
        //prendo le note dal database e le metto nella lista
        notes = SplashActivity.appDataBase.daoManager().getNote();

        updateMissingNoteBackground();

        //setto il recycler view
        recyclerView = view.findViewById(R.id.recyclerViewNotes);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);

        notesAdapter = new NotesAdapter(notes);

        recyclerView.setLayoutManager(mLayoutManager);

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

    private void onSaveClicked() {

        name = editTextName.getText().toString();
        position = editTextPosition.getText().toString();
        textNote = editTextNoteText.getText().toString();



        if (name.isEmpty() && position.isEmpty() && textNote.isEmpty()) {
            Toast.makeText(getActivity(), "Nota non salvata!", Toast.LENGTH_SHORT).show();
        } else {
            Note note = new Note(name, position, textNote);

            Log.d("prova", "arrivato 1");

            int index = notes.size();

            Log.d("prova", "arrivato 2" + index);

            notes.add(index,note);

            Log.d("prova", "arrivato 3");

            SplashActivity.appDataBase.daoManager().addNote(note);
            Log.d("prova", "arrivato 4");
            notesAdapter.notifyItemInserted(index);
            Log.d("prova", "arrivato 5");
            updateMissingNoteBackground();

            Log.d("prova", "arrivato 6");

            Toast.makeText(getActivity(), "Nota salvata!", Toast.LENGTH_SHORT).show();

            Log.d("prova", "arrivato 7");
        }
    }

    public void updateNote(int position, EditText noteName, EditText notePosition, EditText noteText) {
        Note note = new Note();
        note.setId(notes.get(position).getId());
        note.setName(noteName.getText().toString());
        note.setPosition(notePosition.getText().toString());
        note.setText(noteText.getText().toString());

        notes.remove(position);
        notesAdapter.notifyItemRemoved(position);

        notes.add(position, note);
        notesAdapter.notifyItemInserted(position);

        SplashActivity.appDataBase.daoManager().updateNote(note);
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

    private void deleteNote(int position) {
        Integer id = notes.get(position).getId();
        Note note = new Note();
        note.setId(id);
        notes.remove(position);
        SplashActivity.appDataBase.daoManager().deleteNote(note);
        notesAdapter.notifyItemRemoved(position);
        updateMissingNoteBackground();
    }



    public void updateNoteInfoDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.update_note_dialog_layout, null);

        updateTextName = view.findViewById(R.id.update_note_name);
        updateTextPosition = view.findViewById(R.id.update_note_position);
        updateTextNoteText = view.findViewById(R.id.update_note_text);

        updateTextName.setText(notes.get(position).getName());
        updateTextPosition.setText(notes.get(position).getPosition());
        updateTextNoteText.setText(notes.get(position).getText());

        builder.setView(view)
                .setTitle("Modifica nota")
                .setPositiveButton("SALVA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateNote(position, updateTextName, updateTextPosition, updateTextNoteText);
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
                .setCancelable(false)
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


    public void actionBarDeleteAllItems() {
        notes.clear();
        notesAdapter.notifyDataSetChanged();
        updateMissingNoteBackground();
        SplashActivity.appDataBase.daoManager().deleteAllNotes();
    }

}