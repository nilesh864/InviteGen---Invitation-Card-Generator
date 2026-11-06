package com.example.invitegen;


import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class RSVPStorage {

    public static void saveResponse(Context context, String response) {
        SharedPreferences prefs =
                context.getSharedPreferences("RSVPData", Context.MODE_PRIVATE);
        prefs.edit().putString("response", response).apply();

        Toast.makeText(context,
                "Response Saved: " + response,
                Toast.LENGTH_SHORT).show();
    }
}
