package com.example.jlearnn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class LessonItemUploadActivity extends AppCompatActivity {
    EditText uploadRomaji, uploadDesc, uploadExample, uploadJapaneseChar;
    Spinner lessonSpinner;
    Button saveButton;
    Button uploadAudioButton;
    String[] lessons = new String[15];

    private Uri audioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadDesc = findViewById(R.id.uploadDesc);
        uploadRomaji = findViewById(R.id.uploadRomaji);
        uploadExample = findViewById(R.id.uploadExample);
        uploadJapaneseChar = findViewById(R.id.JapaneseChar);
        uploadAudioButton = findViewById(R.id.uploadAudioButton);
        lessonSpinner = findViewById(R.id.lessonSpinner);
        saveButton = findViewById(R.id.saveButton);

        for (int i = 0; i < lessons.length; i++) {
            lessons[i] = String.valueOf(i + 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lessons);
        lessonSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
            }
        });

        uploadAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement audio file selection logic here
                // Example: Launch an intent to choose an audio file
                // Replace with your actual implementation
                // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // intent.setType("audio/*");
                // startActivityForResult(Intent.createChooser(intent, "Select Audio"), 1);

                // For demonstration purposes, setting a dummy URI
                audioUri = Uri.parse("content://media/external/audio/media/12345");
                Toast.makeText(LessonItemUploadActivity.this, "Audio file selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void uploadData() {
        String romaji = uploadRomaji.getText().toString().trim();
        String desc = uploadDesc.getText().toString().trim();
        String example = uploadExample.getText().toString().trim();
        String japaneseChar = uploadJapaneseChar.getText().toString().trim();
        String selectedLesson = lessonSpinner.getSelectedItem().toString();

        if (romaji.isEmpty() || desc.isEmpty() || example.isEmpty() || japaneseChar.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (audioUri != null) {
            uploadAudio();
        } else {
            Toast.makeText(this, "Please select an audio file", Toast.LENGTH_SHORT).show();
        }


        DatabaseReference lessonRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Lessons")
                .child(selectedLesson);

        lessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the number of items in the lesson
                long itemCount = snapshot.getChildrenCount();

                // Get the lesson number from the selectedLesson string
                String lessonNumber = selectedLesson.substring(selectedLesson.lastIndexOf(" ") + 1);

                // Construct the key with the format "lessonNumber_itemCount"
                String key = lessonNumber + "_" + (itemCount + 1);

                LessonItemDataClass lessonItemDataClass = new LessonItemDataClass(romaji, desc, example, japaneseChar);

                lessonRef.child(key).setValue(lessonItemDataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LessonItemUploadActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LessonItemUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LessonItemUploadActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAudio() {
        // Replace "your_storage_path" with your actual Firebase Storage path
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("your_storage_path");

        // Generate a random UUID as the audio file name
        String audioFileName = UUID.randomUUID().toString();

        StorageReference audioRef = storageRef.child(audioFileName);

        // Upload file to Firebase Storage
        UploadTask uploadTask = audioRef.putFile(audioUri);

        // Register observers to listen for upload progress or failures
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return audioRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    // Handle successful audio upload
                    Toast.makeText(LessonItemUploadActivity.this, "Audio uploaded successfully: " + downloadUri.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    // Handle failures
                    Toast.makeText(LessonItemUploadActivity.this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle exceptions
                Toast.makeText(LessonItemUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

