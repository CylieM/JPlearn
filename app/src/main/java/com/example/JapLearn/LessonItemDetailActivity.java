package com.example.JapLearn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LessonItemDetailActivity extends AppCompatActivity {
    TextView detailDesc, detailRomaji, detailExampleEn, detailExampleJp, detailPronun, detailJapaneseChar, detailLesson;
    FloatingActionButton deleteButton, editButton;
    Button playAudio;
    String key = "";
    MediaPlayer mediaPlayer;
    FirebaseStorage storage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_item_detail);

        // Initialize views
        detailDesc = findViewById(R.id.detailDesc);
        detailRomaji = findViewById(R.id.detailRomaji);
        detailExampleEn = findViewById(R.id.detailExampleEn);
        detailExampleJp = findViewById(R.id.detailExampleJp);
        detailPronun = findViewById(R.id.detailPronun);
        detailJapaneseChar = findViewById(R.id.JapaneseChar);
        detailLesson = findViewById(R.id.LessonNumber);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);
        playAudio = findViewById(R.id.playAudio);
        storage = FirebaseStorage.getInstance();

        // Retrieve data passed from the previous activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            detailDesc.setText(bundle.getString("Description"));
            detailRomaji.setText(bundle.getString("Romaji"));
            detailExampleEn.setText(bundle.getString("ExampleEn"));
            detailExampleJp.setText(bundle.getString("ExampleJp"));
            detailPronun.setText(bundle.getString("Pronun"));
            detailJapaneseChar.setText(bundle.getString("JapaneseChar"));
            key = bundle.getString("Key");

            // Extract lesson number from the key and display it
            String[] keyParts = key.split("_");
            if (keyParts.length > 0) {
                String lessonNumber = keyParts[0];
                detailLesson.setText("Lesson " + lessonNumber);
            }
        }

        // Handle delete button click
        deleteButton.setOnClickListener(view -> {
            if (key == null || key.isEmpty()) {
                Toast.makeText(LessonItemDetailActivity.this, "Key is invalid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Split the key to get the lesson number and item identifier
            String[] keyParts = key.split("_");
            if (keyParts.length > 0) {
                String lessonNumber = keyParts[0]; // Gets the lesson number part (e.g., "4" from "4_1")

                // Reference to the database
                DatabaseReference reference = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("Lessons")
                        .child(lessonNumber) // Navigate to the specific lesson number (e.g., "4")
                        .child(key); // Append the specific key to delete

                // Remove the lesson item
                reference.removeValue()
                        .addOnCompleteListener(task -> {
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

        // Handle edit button click
        editButton.setOnClickListener(view -> {
            // Create an intent to open the LessonItemUpdateActivity
            Intent intent = new Intent(LessonItemDetailActivity.this, LessonItemUpdateActivity.class)
                    .putExtra("Romaji", detailRomaji.getText().toString())
                    .putExtra("Description", detailDesc.getText().toString())
                    .putExtra("ExampleEn", detailExampleEn.getText().toString())  // Corrected here
                    .putExtra("ExampleJp", detailExampleJp.getText().toString())  // Added ExampleJp
                    .putExtra("Pronun", detailPronun.getText().toString())        // Added Pronun
                    .putExtra("JapaneseChar", detailJapaneseChar.getText().toString())
                    .putExtra("Key", key);  // Key remains the same
            startActivity(intent);
        });

        // Handle play audio button click
        playAudio.setOnClickListener(view -> playAudioFile(detailJapaneseChar.getText().toString()));
    }

    // Updated method to play audio for the Japanese character
    private void playAudioFile(String japaneseChar) {
        String sanitizedJapaneseChar = japaneseChar.replaceAll("[^a-zA-Z0-9_\\-]", "" + japaneseChar);  // Sanitize input

        // Reference to Firebase Storage for the audio file
        StorageReference audioRef = storage.getReference().child("audios/" + sanitizedJapaneseChar);

        audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            try {
                mediaPlayer.reset();  // Reset the MediaPlayer to ensure it's in a ready state
                mediaPlayer.setDataSource(uri.toString());  // Set the audio data source to the file's URL
                mediaPlayer.prepare();  // Prepare the audio file for playback
                mediaPlayer.start();  // Start playback
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
