package com.example.jlearnn;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    private String email;

    public static HomeFragment newInstance(String email) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString("email");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Set the email text


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
            Intent intent = new Intent(getActivity(), TestYourself.class);
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
