package com.example.jlearnn;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class UserManagementActivity extends AppCompatActivity implements UserAdapter.OnItemClickListener {
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    ArrayList<RegistrationActivity.User> userList;
    ArrayList<RegistrationActivity.User> fullUserList;
    UserAdapter adapter;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(UserManagementActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(UserManagementActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        userList = new ArrayList<>();
        fullUserList = new ArrayList<>();
        adapter = new UserAdapter(UserManagementActivity.this, userList, this);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");

        // Query users with joinedRoomCode equal to 1
        eventListener = databaseReference.orderByChild("joinedRoomCode").equalTo(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                fullUserList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    RegistrationActivity.User user = itemSnapshot.getValue(RegistrationActivity.User.class);
                    if (user != null) {
                        userList.add(user);
                        fullUserList.add(user);
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
    }

    @Override
    public void onItemClick(RegistrationActivity.User user) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", user.getUserId());
        bundle.putString("username", user.getUsername());
        bundle.putString("email", user.getEmail());

        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.other_user_profile_fragment, profileFragment)
                .commit();

        FrameLayout frameLayout = findViewById(R.id.other_user_profile_fragment);
        frameLayout.setVisibility(View.VISIBLE);

        recyclerView.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);
    }

    public void searchList(String text) {
        if (text.isEmpty()) {
            adapter.searchDataList(fullUserList);
        } else {
            ArrayList<RegistrationActivity.User> searchList = new ArrayList<>();
            for (RegistrationActivity.User user : fullUserList) {
                if (user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                    searchList.add(user);
                }
            }
            adapter.searchDataList(searchList);
        }
    }

    @Override
    public void onBackPressed() {
        FrameLayout frameLayout = findViewById(R.id.other_user_profile_fragment);
        if (frameLayout.getVisibility() == View.VISIBLE) {
            frameLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
