package com.example.jlearnn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VocabForeignIntro extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vocab_foreign_intro);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btnVocabProceed).setOnClickListener(v -> startLessonVocab(3, "Vocabulary"));

    }

    private void startLessonVocab(int lessonNumber, String lessonType) {
        Intent intent = new Intent(VocabForeignIntro.this, LessonVocab.class);
        intent.putExtra("lessonNumber", lessonNumber);
        intent.putExtra("lessonItemIndex", 17); // Set to 10 to start from item 3_11
        startActivity(intent);
        finish();
    }
}