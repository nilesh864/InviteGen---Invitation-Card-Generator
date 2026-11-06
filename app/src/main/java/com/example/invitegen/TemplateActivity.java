package com.example.invitegen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class TemplateActivity extends AppCompatActivity {

    ImageButton[] templates = new ImageButton[20];
    String eventType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);

        eventType = getIntent().getStringExtra("eventType");
        TextView title = findViewById(R.id.tvTemplateTitle);
        if (title != null && eventType != null) {
            title.setText(eventType + " Templates");
        }

        String prefix = (eventType != null) ? eventType.toLowerCase() : "birthday";

        for (int i = 1; i <= 20; i++) {
            int buttonId = getResources().getIdentifier("template" + i, "id", getPackageName());
            templates[i-1] = findViewById(buttonId);

            if (templates[i-1] == null) continue;

            final String templateFileName = prefix + i;
            int imageResId = getResources().getIdentifier(templateFileName, "drawable", getPackageName());

            if (imageResId != 0) {
                templates[i-1].setVisibility(View.VISIBLE);
                
                // Load using Glide from drawable resource
                Glide.with(this)
                    .load(imageResId)
                    .centerCrop()
                    .into(templates[i-1]);

                templates[i-1].setOnClickListener(v -> openPreview(templateFileName));
            } else {
                templates[i-1].setVisibility(View.GONE);
            }
        }
    }

    private void openPreview(String templateName) {
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("template", templateName);
        intent.putExtra("eventType", eventType);

        // Forward ALL event details including Groom and Bride names
        intent.putExtra("name", getIntent().getStringExtra("name"));
        intent.putExtra("groom", getIntent().getStringExtra("groom"));
        intent.putExtra("bride", getIntent().getStringExtra("bride"));
        intent.putExtra("date", getIntent().getStringExtra("date"));
        intent.putExtra("time", getIntent().getStringExtra("time"));
        intent.putExtra("venue", getIntent().getStringExtra("venue"));
        intent.putExtra("message", getIntent().getStringExtra("message"));

        startActivity(intent);
    }
}