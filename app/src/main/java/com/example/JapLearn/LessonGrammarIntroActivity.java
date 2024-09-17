package com.example.JapLearn;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LessonGrammarIntroActivity extends AppCompatActivity {

    private static final String TAG = "LessonGrammarIntroActivity";
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_grammar_intro);

        // Find your button by ID
        Button btnGrammar = findViewById(R.id.btnGrammar);

        // Set a click listener for the button
        btnGrammar.setOnClickListener(view -> {
            // Redirect to LessonGrammarActivity
            Intent intent = new Intent(LessonGrammarIntroActivity.this, LessonGrammarActivity.class);
            startActivity(intent);
        });

        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer();

        // Fetch the audio file from Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("grammarintro.wav");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Play the audio
                playAudio(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e(TAG, "Failed to fetch audio URL", exception);
            }
        });
    }

    private void playAudio(Uri uri) {
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the audio if the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the audio if the activity is paused
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the audio if the activity is resumed
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
}
