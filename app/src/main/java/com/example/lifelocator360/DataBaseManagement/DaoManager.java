package com.example.lifelocator360.DataBaseManagement;

import androidx.room.Insert;

public interface DaoManager {

    @Insert
    public void addContact(Contact contact);


}
