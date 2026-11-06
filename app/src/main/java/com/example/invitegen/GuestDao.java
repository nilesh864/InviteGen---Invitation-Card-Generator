package com.example.invitegen;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface GuestDao {
    @Insert
    void insert(Guest guest);

    @Query("SELECT COUNT(*) FROM guests WHERE LOWER(rsvpStatus) LIKE :pattern")
    int getCountByPattern(String pattern);

    @Query("SELECT * FROM guests")
    List<Guest> getAllGuests();

    @Query("DELETE FROM guests")
    void deleteAllGuests();
}
