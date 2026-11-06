package com.example.invitegen;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PreviewActivity extends AppCompatActivity {

    // -------------------------------------------------------------------------------------
    // CONFIGURATION: UPDATE YOUR LINKS HERE
    private static final String FORM_URL = "https://docs.google.com/forms/d/e/1FAIpQLSdY3NVKZ_C6tnF_zwUnZWdk72vqou_SVmgEyToJfxpdqFvW5w/viewform";
    private static final String CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQPH_hb7Ld535qqXnWOAmRhp6czXiYku2cn_MtgvXhkBcgnRD8ytUXYPI0JuSkz58WE4aW3UFBKrO4U/pub?output=csv";
    private static final String EVENT_FIELD_ID = "entry.123456789"; 
    // -------------------------------------------------------------------------------------

    ImageView templateImage;
    TextView tvEventName, tvDateTime, tvVenue, tvMessage;
    ProgressBar progressBar;
    View invitationCard;
    Button btnExport, btnShare;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        invitationCard = findViewById(R.id.invitationCard);
        templateImage = findViewById(R.id.templateImage);
        tvEventName = findViewById(R.id.tvEventName);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvVenue = findViewById(R.id.tvVenue);
        tvMessage = findViewById(R.id.tvMessage);
        progressBar = findViewById(R.id.progressBar);
        btnExport = findViewById(R.id.btnPDF); 
        btnShare = findViewById(R.id.btnShare);

        // Load Background
        String customImageUriString = getIntent().getStringExtra("customImageUri");
        int blurIntensity = getIntent().getIntExtra("blurIntensity", 0);
        if (customImageUriString != null) {
            Uri uri = Uri.parse(customImageUriString);
            Glide.with(this).load(uri).apply(blurIntensity > 0 ? RequestOptions.bitmapTransform(new BlurTransformation(blurIntensity)) : new RequestOptions()).into(templateImage);
        } else {
            String templateName = getIntent().getStringExtra("template");
            if (templateName != null) {
                int resId = getResources().getIdentifier(templateName, "drawable", getPackageName());
                if (resId != 0) Glide.with(this).load(resId).into(templateImage);
            }
        }

        // Set Details: FIX FOR GROOM & BRIDE NAMES
        String groom = getIntent().getStringExtra("groom");
        String bride = getIntent().getStringExtra("bride");
        String name = getIntent().getStringExtra("name");

        if (groom != null && !groom.isEmpty() && bride != null && !bride.isEmpty()) {
            tvEventName.setText(groom + " & " + bride);
        } else {
            tvEventName.setText(name != null ? name : "Invitation");
        }

        tvDateTime.setText(getIntent().getStringExtra("date") + " | " + getIntent().getStringExtra("time"));
        tvVenue.setText(getIntent().getStringExtra("venue"));
        tvMessage.setText(getIntent().getStringExtra("message"));

        btnExport.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            executorService.execute(() -> {
                saveToGallery();
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            });
        });

        btnShare.setOnClickListener(v -> shareInvitation());

        btnShare.setOnLongClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("EXTRA_CSV_URL", CSV_URL);
            intent.putExtra("EXTRA_EVENT_NAME", tvEventName.getText().toString());
            startActivity(intent);
            return true;
        });
    }

    private void shareInvitation() {
        Bitmap bitmap = Bitmap.createBitmap(invitationCard.getWidth(), invitationCard.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        invitationCard.draw(canvas);

        File cachePath = new File(getExternalCacheDir(), "images");
        cachePath.mkdirs();
        File file = new File(cachePath, "invite.png");
        try (FileOutputStream stream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (IOException e) { return; }

        String rsvpLink = FORM_URL + "?usp=pp_url&" + EVENT_FIELD_ID + "=" + Uri.encode(tvEventName.getText().toString());
        
        Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.putExtra(Intent.EXTRA_TEXT, "You're invited! Click to RSVP: " + rsvpLink);
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, "Share Invitation"));
    }

    private void saveToGallery() {
        Bitmap bitmap = Bitmap.createBitmap(invitationCard.getWidth(), invitationCard.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        invitationCard.draw(canvas);
        try {
            OutputStream fos;
            String filename = "Invite_" + System.currentTimeMillis() + ".jpg";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                v.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/InviteGen");
                fos = getContentResolver().openOutputStream(getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v));
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "InviteGen");
                if (!dir.exists()) dir.mkdirs();
                fos = new FileOutputStream(new File(dir, filename));
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            runOnUiThread(() -> Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show());
        } catch (Exception ignored) {}
    }

    @Override protected void onDestroy() { super.onDestroy(); executorService.shutdown(); }
}
