package com.example.JapLearn;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    ArrayList<UserModel.User> userList;
    ArrayList<UserModel.User> fullUserList;
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail(); // Fetch the logged-in user's email

            // Query users with teacherEmail equal to the logged-in user's email
            eventListener = databaseReference.orderByChild("teacherEmail").equalTo(userEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userList.clear();
                    fullUserList.clear();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        UserModel.User user = itemSnapshot.getValue(UserModel.User.class);
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
        } else {
            // Handle the case where there is no logged-in user
            dialog.dismiss();
            // Optionally show a message or redirect to login
        }


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
    public void onItemClick(UserModel.User user) {
        Intent intent = new Intent(UserManagementActivity.this, ProfileActivity.class);

        // Pass user details to the ProfileActivity
        intent.putExtra("userId", user.getUserId());
        intent.putExtra("username", user.getUsername());
        intent.putExtra("email", user.getEmail());
        intent.putExtra("isFromUserManagement", true);
        // Start the ProfileActivity
        startActivity(intent);

        // Optionally hide the recyclerView and searchView if needed
        recyclerView.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);
    }

    public void searchList(String text) {
        if (text.isEmpty()) {
            adapter.searchDataList(fullUserList);
        } else {
            ArrayList<UserModel.User> searchList = new ArrayList<>();
            for (UserModel.User user : fullUserList) {
                if (user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                    searchList.add(user);
                }
            }
            adapter.searchDataList(searchList);
        }
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
