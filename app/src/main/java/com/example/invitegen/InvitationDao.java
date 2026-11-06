package com.example.invitegen;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface InvitationDao {
    @Insert
    void insert(Invitation invitation);

    @Query("SELECT * FROM invitations ORDER BY timestamp DESC")
    List<Invitation> getAllInvitations();

    @Query("DELETE FROM invitations")
    void deleteAll();
}
