package com.example.jlearnn;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
public class UpdateActivity extends AppCompatActivity {
    EditText updateDesc, updateRomaji, updateExample, updateJapaneseChar;
    Button updateButton;
    String romaji, desc, example, japaneseChar;
    String key, lesson;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        updateButton = findViewById(R.id.updateButton);
        updateDesc = findViewById(R.id.updateDesc);
        updateRomaji = findViewById(R.id.updateRomaji);
        updateExample = findViewById(R.id.updateExample);
        updateJapaneseChar = findViewById(R.id.updateJapaneseChar);

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

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateData();
            }
        });
    }

    public void updateData() {
        romaji = updateRomaji.getText().toString().trim();
        desc = updateDesc.getText().toString().trim();
        example = updateExample.getText().toString().trim();
        japaneseChar = updateJapaneseChar.getText().toString().trim();

        DataClass dataClass = new DataClass(romaji, desc, example, japaneseChar);
        databaseReference.setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(UpdateActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

