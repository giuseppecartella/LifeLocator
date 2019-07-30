package com.example.lifelocator360.DataBaseManagement;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Contact.class, Note.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {

    public abstract DaoManager daoManager();

}