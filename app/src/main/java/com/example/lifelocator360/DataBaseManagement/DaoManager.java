package com.example.lifelocator360.DataBaseManagement;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoManager {

    //CONTATTI
    @Insert
    public void addContact(Contact contact);

    @Query("select * from contact")
    public List<Contact> getContacts();

    @Delete
    public void deleteContact(Contact contact);

    @Query("delete from contact")
    public void deleteAllContacts();

    @Update
    public void updateContact(Contact contact);

    //NB: nel database i contatti sono ordinati per id e quindi per ordine di inserimento
    @Query("select contact_id from contact")
    public Integer[] getContactIds();

    @Query("update Contact set latitude = :lat,longitude = :lng where contact_id = :id")
    public void updateLatLngContacts(String lat,String lng,String id);

    //NOTE
    @Insert
    public void addNote(Note note);

    @Query("select * from note")
    public List<Note> getNote();

    @Delete
    public void deleteNote(Note note);

    @Query("delete from note")
    public void deleteAllNotes();

    @Update
    public void updateNote(Note note);


    @Query("select note_id from note")
    public Integer[] getNoteIds();

    @Query("update Note set latitude = :lat,longitude = :lng where note_id = :id")
    public void updateLatLngNotes(String lat,String lng,String id);


    //FOTO
    @Insert
    public void addPhoto(Photo photo);

    @Query("select * from photo")
    public List<Photo> getPhoto();

    @Delete
    public void deletePhoto(Photo photo);

    @Query("delete from photo")
    public void deleteAllPhotos();
}