package com.example.lifelocator360.DataBaseManagement;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Contact.class}, version = 1)
public abstract class DataBaseManager extends RoomDatabase {

    public abstract DaoManager daoManager();

}
