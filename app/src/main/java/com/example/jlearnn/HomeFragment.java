package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class HomeFragment extends Fragment {





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Handle button clicks
        Button btnLessons = view.findViewById(R.id.btnLessons);
        Button btnSelfTesting = view.findViewById(R.id.btnSelfTesting);
        Button btnLessonReview = view.findViewById(R.id.btnLessonReview);

        btnLessons.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LessonDiscussionActivity.class);
            intent.putExtra("reviewMode", false); // Not in review mode
            startActivity(intent);
        });

        btnSelfTesting.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LessonTestYourself.class);
            startActivity(intent);
        });

        btnLessonReview.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LessonDiscussionActivity.class);
            intent.putExtra("reviewMode", true); // In review mode
            startActivity(intent);
        });

        return view;
    }
}
