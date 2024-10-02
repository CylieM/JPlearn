package com.example.JapLearn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private TextView tvCurrLesson;
    private boolean isBackPressedOnce = false;

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

    @Override
    public void onResume() {
        super.onResume();
        // Disable back button handling when the fragment is visible
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to prevent back navigation
                // Optionally show a message or toast if you want
                Toast.makeText(getActivity(), "Use the logout option to exit", Toast.LENGTH_SHORT).show();
            }
        });
    }}
