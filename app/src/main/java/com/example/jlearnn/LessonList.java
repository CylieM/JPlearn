package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LessonList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lesson_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up click listeners for each lesson
        findViewById(R.id.lessonHiragana).setOnClickListener(v -> startLessonDiscussionActivity(1));
        findViewById(R.id.lessonKatakana).setOnClickListener(v -> startLessonDiscussionActivity(2));
        findViewById(R.id.lessonVocabulary).setOnClickListener(v -> startLessonDiscussionActivity(3));
        findViewById(R.id.lessonParticles).setOnClickListener(v -> startLessonDiscussionActivity(4));
    }

    private void startLessonDiscussionActivity(int lessonNumber) {
        Intent intent = new Intent(LessonList.this, LessonDiscussionActivity.class);
        intent.putExtra("lessonNumber", lessonNumber);
        startActivity(intent);
    }
}
