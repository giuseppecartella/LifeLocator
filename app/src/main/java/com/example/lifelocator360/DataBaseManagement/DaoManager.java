package com.example.lifelocator360.DataBaseManagement;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface DaoManager {

    @Insert
    public void addContact(Contact contact);

    @Query("select * from contact")
    public List<Contact> getContacts();

    @Delete
    public void deleteContact(Contact contact);

    @Update
    public void updateContact(Contact contact);

}
