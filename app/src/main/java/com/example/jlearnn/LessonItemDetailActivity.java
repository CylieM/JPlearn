package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LessonItemDetailActivity extends AppCompatActivity {
    TextView detailDesc, detailRomaji, detailExample, detailJapaneseChar, detailLesson;
    FloatingActionButton deleteButton, editButton;
    String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailDesc = findViewById(R.id.detailDesc);
        detailRomaji = findViewById(R.id.detailRomaji);
        detailExample = findViewById(R.id.detailExample);
        detailJapaneseChar = findViewById(R.id.JapaneseChar);
        detailLesson = findViewById(R.id.LessonNumber); // Add TextView for lesson number
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            detailDesc.setText(bundle.getString("Description"));
            detailRomaji.setText(bundle.getString("Romaji"));
            detailExample.setText(bundle.getString("Example"));
            detailJapaneseChar.setText(bundle.getString("JapaneseChar"));
            key = bundle.getString("Key");

            // Extract lesson number from the key and display it
            String[] keyParts = key.split("_");
            if (keyParts.length > 0) {
                String lessonNumber = keyParts[0]; // Extract lesson number from the key
                detailLesson.setText("Lesson " + lessonNumber);
            }
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("Lessons");
                reference.child(key).removeValue();
                Toast.makeText(LessonItemDetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LessonItemCRUD.class));
                finish();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LessonItemDetailActivity.this, LessonItemUpdateActivity.class)
                        .putExtra("Romaji", detailRomaji.getText().toString())
                        .putExtra("Description", detailDesc.getText().toString())
                        .putExtra("Example", detailExample.getText().toString())
                        .putExtra("JapaneseChar", detailJapaneseChar.getText().toString())
                        .putExtra("Key", key);
                startActivity(intent);
            }
        });
    }
}

