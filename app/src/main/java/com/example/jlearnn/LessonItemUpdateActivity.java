package com.example.jlearnn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class LessonItemUpdateActivity extends AppCompatActivity {
    EditText updateDesc, updateRomaji, updateExample, updateJapaneseChar;
    Button updateButton, updateAudioButton;
    String romaji, desc, example, japaneseChar;
    String key, lesson;
    DatabaseReference databaseReference;
    private Uri audioUri;
    private static final int PICK_AUDIO_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        updateButton = findViewById(R.id.updateButton);
        updateDesc = findViewById(R.id.updateDesc);
        updateRomaji = findViewById(R.id.updateRomaji);
        updateExample = findViewById(R.id.updateExample);
        updateJapaneseChar = findViewById(R.id.updateJapaneseChar);
        updateAudioButton = findViewById(R.id.updateAudioButton);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            updateRomaji.setText(bundle.getString("Romaji"));
            updateDesc.setText(bundle.getString("Description"));
            updateExample.setText(bundle.getString("Example"));
            updateJapaneseChar.setText(bundle.getString("JapaneseChar"));
            key = bundle.getString("Key");
            // Extract the lesson number from the key
            lesson = key.split("_")[0]; // Assuming key format is lessonNumber_itemCount
        } else {
            // Handle the case where the lesson is not passed properly
            Toast.makeText(this, "Lesson information not provided", Toast.LENGTH_SHORT).show();
            finish(); // Finish the activity
            return;
        }

        // Construct the correct database reference using the lesson variable
        databaseReference = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Lessons")
                .child("Lesson " + lesson) // Assuming the lesson number is passed as a string, adjust this accordingly if it's an integer
                .child(key);

        updateButton.setOnClickListener(view -> updateData());

        updateAudioButton.setOnClickListener(v -> selectAudioFile());
    }

    private void selectAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            audioUri = data.getData();
            Toast.makeText(LessonItemUpdateActivity.this, "Audio file selected: " + audioUri.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateData() {
        romaji = updateRomaji.getText().toString().trim();
        desc = updateDesc.getText().toString().trim();
        example = updateExample.getText().toString().trim();
        japaneseChar = updateJapaneseChar.getText().toString().trim();

        LessonItemDataClass lessonItemDataClass = new LessonItemDataClass(romaji, desc, example, japaneseChar);
        databaseReference.setValue(lessonItemDataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LessonItemUpdateActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(LessonItemUpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

        // Upload audio file if audioUri is not null
        if (audioUri != null) {
            uploadAudio();
        } else {
            Toast.makeText(this, "Please select an audio file", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadAudio() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("audios/");

        // Generate a random UUID as the audio file name
        String audioFileName = UUID.randomUUID().toString();

        StorageReference audioRef = storageRef.child(audioFileName);

        // Upload file to Firebase Storage
        UploadTask uploadTask = audioRef.putFile(audioUri);

        // Register observers to listen for upload progress or failures
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Continue with the task to get the download URL
            return audioRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                // Update the lesson item data with the new audio download URL
                databaseReference.child("audioUrl").setValue(downloadUri.toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LessonItemUpdateActivity.this, "Audio updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LessonItemUpdateActivity.this, "Failed to update audio", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                // Handle failures
                Toast.makeText(LessonItemUpdateActivity.this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Handle exceptions
            Toast.makeText(LessonItemUpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
