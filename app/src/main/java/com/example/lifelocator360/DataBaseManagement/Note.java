package com.example.lifelocator360.DataBaseManagement;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "note")

public class Note implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private Integer id ;

    @ColumnInfo(name = "note_name")
    private String name;

    @ColumnInfo(name = "note_position")
    private String position;

    @ColumnInfo(name = "note_text")
    private String text;

    @ColumnInfo(name = "latitude")
    private String latitude;

    @ColumnInfo(name = "longitude")
    private String longitude;

    public Note(){

    }

    public Note(String name, String position, String text) {
        this.name = name;
        this.position = position;
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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