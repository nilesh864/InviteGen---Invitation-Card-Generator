package com.example.invitegen;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;
import java.util.Locale;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class EventDetailsActivity extends AppCompatActivity {

    EditText etName, etDate, etTime, etVenue, etMessage, etGroom, etBride;
    TextInputLayout tilGroom, tilBride;
    Button btnNext, btnPickImage;
    ImageButton btnRemoveImage, btnBack;
    ImageView ivCustomPreview;
    View flImagePreview;
    LinearLayout llBlurControl;
    SeekBar sbBlur;
    TextView tvBlurLabel;
    
    String eventType;
    Uri selectedImageUri;
    int blurIntensity = 0;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    showImagePreview();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventType = getIntent().getStringExtra("eventType");

        // Initialization
        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etVenue = findViewById(R.id.etVenue);
        etMessage = findViewById(R.id.etMessage);
        etGroom = findViewById(R.id.etGroom);
        etBride = findViewById(R.id.etBride);
        tilGroom = findViewById(R.id.tilGroom);
        tilBride = findViewById(R.id.tilBride);
        btnNext = findViewById(R.id.btnNext);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        ivCustomPreview = findViewById(R.id.ivCustomPreview);
        flImagePreview = findViewById(R.id.flImagePreview);
        llBlurControl = findViewById(R.id.llBlurControl);
        sbBlur = findViewById(R.id.sbBlur);
        tvBlurLabel = findViewById(R.id.tvBlurLabel);

        btnBack.setOnClickListener(v -> finish());

        if ("Wedding".equalsIgnoreCase(eventType) || "Engagement".equalsIgnoreCase(eventType)) {
            tilGroom.setVisibility(View.VISIBLE);
            tilBride.setVisibility(View.VISIBLE);
        }

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        btnPickImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        
        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            blurIntensity = 0;
            sbBlur.setProgress(0);
            flImagePreview.setVisibility(View.GONE);
            llBlurControl.setVisibility(View.GONE);
        });

        sbBlur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blurIntensity = progress;
                tvBlurLabel.setText("Background Blur: " + progress);
                updateImageBlur();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnNext.setOnClickListener(v -> {
            Intent intent;
            if (selectedImageUri != null) {
                intent = new Intent(this, PreviewActivity.class);
                intent.putExtra("customImageUri", selectedImageUri.toString());
                intent.putExtra("blurIntensity", blurIntensity);
            } else {
                intent = new Intent(this, TemplateActivity.class);
            }
            
            intent.putExtra("eventType", eventType);
            intent.putExtra("name", etName.getText().toString());
            intent.putExtra("groom", etGroom.getText().toString());
            intent.putExtra("bride", etBride.getText().toString());
            intent.putExtra("date", etDate.getText().toString());
            intent.putExtra("time", etTime.getText().toString());
            intent.putExtra("venue", etVenue.getText().toString());
            intent.putExtra("message", etMessage.getText().toString());
            startActivity(intent);
        });
    }

    private void showImagePreview() {
        flImagePreview.setVisibility(View.VISIBLE);
        llBlurControl.setVisibility(View.VISIBLE);
        updateImageBlur();
    }

    private void updateImageBlur() {
        if (selectedImageUri == null) return;
        
        if (blurIntensity > 0) {
            Glide.with(this)
                .load(selectedImageUri)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(blurIntensity)))
                .into(ivCustomPreview);
        } else {
            Glide.with(this).load(selectedImageUri).into(ivCustomPreview);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    etDate.setText(selectedDate);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    etTime.setText(selectedTime);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }
}
