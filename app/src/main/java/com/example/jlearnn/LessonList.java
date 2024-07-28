package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LessonList extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lesson_list);

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up click listeners for each lesson
        findViewById(R.id.lessonKatakanaHiragana).setOnClickListener(v -> startLessonDiscussionActivity(1, "Katakana and Hiragana"));
        findViewById(R.id.lessonVocabulary).setOnClickListener(v -> startLessonDiscussionActivity(2, "Vocabulary"));

        // Modify the click listener for the grammar button to use your existing activity
        LinearLayout lessonGrammar = findViewById(R.id.lessonGrammar);
        lessonGrammar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LessonList.this, LessonGrammarIntroActivity.class);
                startActivity(intent);
                // Save the selected lesson in SharedPreferences
                sharedPreferences.edit().putString("currentLesson", "Grammar").apply();
            }
        });
    }

    private void startLessonDiscussionActivity(int lessonNumber, String lessonName) {
        Intent intent = new Intent(LessonList.this, LessonDiscussionActivity.class);
        intent.putExtra("lessonNumber", lessonNumber);
        startActivity(intent);
        // Save the selected lesson in SharedPreferences
        sharedPreferences.edit().putString("currentLesson", lessonName).apply();
    }
}
