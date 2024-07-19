package com.example.jlearnn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LessonItemDetailActivity extends AppCompatActivity {
    TextView detailDesc, detailRomaji, detailExample, detailJapaneseChar, detailLesson;
    FloatingActionButton deleteButton, editButton;
    Button playAudio;
    String key = "";
    MediaPlayer mediaPlayer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_item_detail);

        detailDesc = findViewById(R.id.detailDesc);
        detailRomaji = findViewById(R.id.detailRomaji);
        detailExample = findViewById(R.id.detailExample);
        detailJapaneseChar = findViewById(R.id.JapaneseChar);
        detailLesson = findViewById(R.id.LessonNumber);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);
        playAudio = findViewById(R.id.playAudio);

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

        deleteButton.setOnClickListener(view -> {
            if (key == null || key.isEmpty()) {
                Toast.makeText(LessonItemDetailActivity.this, "Key is invalid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Split the key to get the lesson number
            String[] keyParts = key.split("_");
            if (keyParts.length == 2) {
                String lessonNumber = keyParts[0];
                DatabaseReference reference = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("Lessons")
                        .child("Lesson " + lessonNumber)
                        .child(key);

                reference.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LessonItemDetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LessonItemList.class));
                        finish();
                    } else {
                        Toast.makeText(LessonItemDetailActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(LessonItemDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(LessonItemDetailActivity.this, "Invalid key format", Toast.LENGTH_SHORT).show();
            }
        });

        editButton.setOnClickListener(view -> {
            Intent intent = new Intent(LessonItemDetailActivity.this, LessonItemUpdateActivity.class)
                    .putExtra("Romaji", detailRomaji.getText().toString())
                    .putExtra("Description", detailDesc.getText().toString())
                    .putExtra("Example", detailExample.getText().toString())
                    .putExtra("JapaneseChar", detailJapaneseChar.getText().toString())
                    .putExtra("Key", key);
            startActivity(intent);
        });

        playAudio.setOnClickListener(view -> playAudioFile(detailJapaneseChar.getText().toString()));
    }

    private void playAudioFile(String japaneseChar) {
        String sanitizedJapaneseChar = japaneseChar.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("audios/" + sanitizedJapaneseChar);

        audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(uri.toString());
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(LessonItemDetailActivity.this, "Playing Audio", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(LessonItemDetailActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(LessonItemDetailActivity.this, "Failed to get audio URL: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
