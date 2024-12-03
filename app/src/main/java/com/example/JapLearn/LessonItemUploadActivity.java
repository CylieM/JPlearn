
package com.example.JapLearn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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

public class LessonItemUploadActivity extends AppCompatActivity {
    EditText  uploadDesc,  uploadRomaji,  uploadExampleEn,  uploadExampleJp,  uploadPronun,  uploadJapaneseChar;
    Spinner lessonSpinner;
    Button saveButton;
    Button uploadAudioButton;
    String[] lessons = new String[15];

    private static final int PICK_AUDIO_REQUEST = 1;
    private Uri audioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadDesc = findViewById(R.id. uploadDesc);
        uploadRomaji = findViewById(R.id. uploadRomaji);
        uploadExampleEn = findViewById(R.id.uploadExampleEn);
        uploadExampleJp = findViewById(R.id.uploadExampleJp);
        uploadPronun = findViewById(R.id.uploadPronun);
        uploadJapaneseChar = findViewById(R.id.uploadJapaneseChar);
        uploadAudioButton = findViewById(R.id.uploadAudioButton);
        lessonSpinner = findViewById(R.id.lessonSpinner);
        saveButton = findViewById(R.id.saveButton);

        for (int i = 0; i < lessons.length; i++) {
            lessons[i] = String.valueOf(i + 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lessons);
        lessonSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(view -> uploadData());

        uploadAudioButton.setOnClickListener(v -> selectAudioFile());
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
            Toast.makeText(LessonItemUploadActivity.this, "Audio file selected: " + audioUri.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadData() {
        String romaji = uploadRomaji.getText().toString().trim();
        String desc = uploadDesc.getText().toString().trim();
        String exampleEn = uploadExampleEn.getText().toString().trim();
        String exampleJp = uploadExampleJp.getText().toString().trim();
        String pronun = uploadPronun.getText().toString().trim();
        String japaneseChar = uploadJapaneseChar.getText().toString().trim();
        String selectedLesson = lessonSpinner.getSelectedItem().toString();

        if (romaji.isEmpty() || desc.isEmpty() || exampleEn.isEmpty() || exampleJp.isEmpty() || pronun.isEmpty() ||japaneseChar.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (audioUri != null) {
            uploadAudio(japaneseChar);
        } else {
            Toast.makeText(this, "Please select an audio file", Toast.LENGTH_SHORT).show();
        }

        DatabaseReference lessonRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Lessons")
                .child(selectedLesson);

        lessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long itemCount = snapshot.getChildrenCount();
                String lessonNumber = selectedLesson.substring(selectedLesson.lastIndexOf(" ") + 1);
                String key = lessonNumber + "_" + (itemCount + 1);

                LessonItemDataClass lessonItemDataClass = new LessonItemDataClass(romaji, desc, exampleEn, exampleJp, pronun, japaneseChar);

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

    private void uploadAudio(String japaneseChar) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("audios/");

        // Sanitize japaneseChar to ensure it can be used as a file name
        String sanitizedJapaneseChar = japaneseChar.replaceAll("[^a-zA-Z0-9_\\-]", "" + japaneseChar);

        StorageReference audioRef = storageRef.child(sanitizedJapaneseChar);

        UploadTask uploadTask = audioRef.putFile(audioUri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return audioRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                Toast.makeText(LessonItemUploadActivity.this, "Audio uploaded successfully: " + downloadUri.toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LessonItemUploadActivity.this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(LessonItemUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String getRealPathFromURI(Uri uri) {
        String result;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
