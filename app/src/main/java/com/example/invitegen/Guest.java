package com.example.invitegen;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "guests")
public class Guest {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String guestName;
    public String rsvpStatus; // "Yes", "No", "Maybe"
    public String eventName;

    public Guest() {
    }

    public Guest(String guestName, String rsvpStatus, String eventName) {
        this.guestName = guestName;
        this.rsvpStatus = rsvpStatus;
        this.eventName = eventName;
    }
}
