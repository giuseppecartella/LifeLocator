package com.example.lifelocator360.FragmentManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifelocator360.DataBaseManagement.Note;
import com.example.lifelocator360.R;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
    private List<Note> notes;
    private static OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }


    public static class NotesViewHolder extends RecyclerView.ViewHolder {

        public ImageView locationIcon;
        public TextView noteName;
        public TextView notePosition;
        public TextView noteText;


        public NotesViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            locationIcon =  itemView.findViewById(R.id.placeNoteIcon);
            noteName = itemView.findViewById(R.id.noteName);
            notePosition = itemView.findViewById(R.id.notePosition);
            noteText = itemView.findViewById(R.id.noteText);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }

                    }
                }
            });
        }
    }



    public NotesAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_card_layout, parent, false);
        NotesViewHolder notesViewHolder = new NotesViewHolder(view, onItemClickListener);
        return notesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        Note currentNote = notes.get(position);


        if(!currentNote.getName().isEmpty())
            holder.noteName.setText(currentNote.getName());
        else
            holder.noteName.setText("NESSUN TITOLO");

        if(!currentNote.getPosition().isEmpty())
            holder.notePosition.setText(currentNote.getPosition());
        else
            holder.notePosition.setText("Nessuna posizione");

        if(!currentNote.getText().isEmpty())
            holder.noteText.setText(currentNote.getText());
        else
            holder.noteText.setText("Nessun testo");


    }


    @Override
    public int getItemCount() {
        return notes.size();
    }
}