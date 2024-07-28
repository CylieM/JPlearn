package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class HomeFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private TextView tvCurrLesson;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Handle button clicks
        Button btnLessons = view.findViewById(R.id.btnLessons);
        Button btnTest = view.findViewById(R.id.btnTest);
        tvCurrLesson = view.findViewById(R.id.tv_currLesson);

        btnLessons.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LessonList.class);
            intent.putExtra("reviewMode", false); // Not in review mode
            startActivity(intent);
        });

        btnTest.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LessonsActivity.class);
            intent.putExtra("reviewMode", false); // Not in review mode
            startActivity(intent);
        });

        // Load the current lesson from SharedPreferences
        loadCurrentLesson();

        return view;
    }

    private void loadCurrentLesson() {
        String currentLesson = sharedPreferences.getString("currentLesson", "Katakana and Hiragana");
        tvCurrLesson.setText(currentLesson);
    }
}
