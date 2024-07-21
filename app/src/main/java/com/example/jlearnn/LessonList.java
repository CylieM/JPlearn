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
        findViewById(R.id.lessonKatakanaHiragana).setOnClickListener(v -> startLessonDiscussionActivity(1));
        findViewById(R.id.lessonVocabulary).setOnClickListener(v -> startLessonDiscussionActivity(2));

        // Modify the click listener for the grammar button to use your existing activity
        LinearLayout lessonGrammar = findViewById(R.id.lessonGrammar);
        lessonGrammar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LessonList.this, LessonGrammarIntroActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startLessonDiscussionActivity(int lessonNumber) {
        Intent intent = new Intent(LessonList.this, LessonDiscussionActivity.class);
        intent.putExtra("lessonNumber", lessonNumber);
        startActivity(intent);
    }
}
