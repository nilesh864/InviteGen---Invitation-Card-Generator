package com.example.invitegen;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "invitations")
public class Invitation {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String eventType;
    public String eventName;
    public String groomName;
    public String brideName;
    public String date;
    public String time;
    public String venue;
    public String message;
    public String templateName;
    public String customImageUri;
    public long timestamp;

    public Invitation() {
        this.timestamp = System.currentTimeMillis();
    }
}
