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
        Button btnGrammar = view.findViewById(R.id.btnLessons);


        btnGrammar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChooseLessonActivity.class);
            startActivity(intent);
        });


        return view;
    }
}
