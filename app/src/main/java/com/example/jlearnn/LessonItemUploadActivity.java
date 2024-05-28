package com.example.jlearnn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
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

public class LessonItemUploadActivity extends AppCompatActivity {
    EditText uploadRomaji, uploadDesc, uploadExample, uploadJapaneseChar;
    Spinner lessonSpinner;
    Button saveButton;
    String[] lessons = new String[15];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadDesc = findViewById(R.id.uploadDesc);
        uploadRomaji = findViewById(R.id.uploadRomaji);
        uploadExample = findViewById(R.id.uploadExample);
        uploadJapaneseChar = findViewById(R.id.JapaneseChar);
        lessonSpinner = findViewById(R.id.lessonSpinner);
        saveButton = findViewById(R.id.saveButton);

        for (int i = 0; i < lessons.length; i++) {
            lessons[i] = "Lesson " + (i + 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lessons);
        lessonSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
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
}

