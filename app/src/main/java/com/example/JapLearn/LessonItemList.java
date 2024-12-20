package com.example.JapLearn;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
public class LessonItemList extends AppCompatActivity {
    FloatingActionButton fab;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    List<LessonItemDataClass> dataList;
    LessonItemAdapter adapter;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_item_list);

        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(LessonItemList.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        AlertDialog.Builder builder = new AlertDialog.Builder(LessonItemList.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();
        adapter = new LessonItemAdapter(LessonItemList.this, dataList);
        recyclerView.setAdapter(adapter);
        databaseReference = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Lessons");

        dialog.show();

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for (DataSnapshot lessonSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot itemSnapshot : lessonSnapshot.getChildren()) {
                        try {
                            LessonItemDataClass lessonItemDataClass = itemSnapshot.getValue(LessonItemDataClass.class);
                            if (lessonItemDataClass != null && lessonItemDataClass.getDataRomaji() != null && !lessonItemDataClass.getDataRomaji().isEmpty()) {
                                lessonItemDataClass.setKey(itemSnapshot.getKey());
                                dataList.add(lessonItemDataClass);
                            } else {
                                Log.e("DataError", "Null or empty item in lesson " + lessonSnapshot.getKey());
                            }
                        } catch (DatabaseException e) {
                            Log.e("FirebaseError", "Data conversion error: " + e.getMessage());
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LessonItemList.this, LessonItemUploadActivity.class);
                startActivity(intent);
            }
        });
    }

    public void searchList(String text) {
        ArrayList<LessonItemDataClass> searchList = new ArrayList<>();
        for (LessonItemDataClass lessonItemDataClass : dataList) {
            if (lessonItemDataClass.getDataRomaji() != null && lessonItemDataClass.getDataRomaji().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(lessonItemDataClass);
            }
        }
        adapter.searchDataList(searchList);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        // Add flags to clear the activity stack if you want to ensure a clean navigation back to HomeActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Optional: Call finish if you want to close the current activity
    }

}
