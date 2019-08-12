package com.example.lifelocator360.DataBaseManagement;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "photo")

public class Photo {

    //Per ora inutile e non usato

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "photo_path")
    private String path;

    @ColumnInfo(name = "latitude")
    private String latitude;

    @ColumnInfo(name = "longitude")
    private String longitude;

    public Photo(){

    }

    public Photo(String path, String latitude, String longitude) {
        this.path = path;
        this.latitude = latitude;
        this. longitude = longitude;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}