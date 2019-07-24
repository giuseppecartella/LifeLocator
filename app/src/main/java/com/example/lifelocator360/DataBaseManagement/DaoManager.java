package com.example.lifelocator360.DataBaseManagement;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface DaoManager {

    @Insert
    public void addContact(Contact contact);


}
