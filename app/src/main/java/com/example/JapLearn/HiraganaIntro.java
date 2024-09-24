package com.example.JapLearn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HiraganaIntro extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        setContentView(R.layout.activity_hiragana_intro);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btnProceed).setOnClickListener(v -> startLessonDiscussionActivity(1, "Hiragana"));
    }

    private void startLessonDiscussionActivity(int lessonNumber, String lessonName) {
        Intent intent = new Intent(HiraganaIntro.this, LessonDiscussionActivity.class);
        intent.putExtra("lessonNumber", lessonNumber);
        startActivity(intent);
        // Save the selected lesson in SharedPreferences
        sharedPreferences.edit().putString("currentLesson", lessonName).apply();
    }
}