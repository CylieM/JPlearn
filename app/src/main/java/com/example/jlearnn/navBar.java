package com.example.jlearnn;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class navBar extends AppCompatActivity {

    Button btnLogOut;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth.AuthStateListener authStateListener;
    TextView txtUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navbar);
        bottomNavigationView = findViewById(R.id.bottomNavView);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        String email = getIntent().getStringExtra("email");  // Retrieve the email
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.navHome) {
                    loadFragment(new HomeFragment(), false);
                } else if (itemId == R.id.navLeaderboard) {
                    loadFragment(new LeaderboardFragment(), false);
                } else if (itemId == R.id.navKanashoot) {
                    // Start KanaShootActivity
                    startActivity(new Intent(navBar.this, KanaShootActivity.class));
                    // Finish the current activity to prevent going back to it from KanaShootActivity
                    finish();
                } else if (itemId == R.id.navNihongorace) {
                    loadFragment(new NihongoRaceFragment(), false);
                } else if (itemId == R.id.navProfile) { //nav Profile
                    getUserRole(user.getUid(), new UserRoleCallback() {
                        @Override
                        public void onRoleRetrieved(String role) {
                            View view = bottomNavigationView.findViewById(R.id.navProfile);
                            PopupMenu popupMenu = new PopupMenu(navBar.this, view);

                            // Inflate the appropriate menu layout based on the user's role
                            if ("Admin".equals(role)) {
                                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu()); // Admin profile menu
                            } else if ("Teacher".equals(role)) {
                                popupMenu.getMenuInflater().inflate(R.menu.profile_menu_teacher, popupMenu.getMenu()); // Teacher profile menu
                            } else {
                                popupMenu.getMenuInflater().inflate(R.menu.profile_menu_student, popupMenu.getMenu()); // Student profile menu
                            }

                            popupMenu.show();
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    int itemId = item.getItemId();
                                    if (itemId == R.id.navProfile) {
                                        loadFragment(new ProfileFragment(), false);
                                        return true;
                                    } else if (itemId == R.id.navLogout) {
                                        // Handle logout
                                        firebaseAuth.signOut();
                                        // Redirect to login activity
                                        Intent intent = new Intent(navBar.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                        return true;
                                    } else if (itemId == R.id.navAdminPanel) {
                                        // Open the UploadActivity when the AdminPanel item is clicked
                                        startActivity(new Intent(navBar.this, LessonItemCRUD.class));
                                        return true;
                                    } else if (itemId == R.id.navUserPanel) {
                                        // Redirect to user management activity
                                        startActivity(new Intent(navBar.this, UserManagementActivity.class));
                                        return true;
                                    }
                                    return false;
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            // Handle the error
                            Log.e("User Role", "Error: " + error);
                        }
                    });
                    return false;  // return false to not select the profile item
                }
                return true;
            }

        });


        // Load HomeFragment by default
        loadFragment(new HomeFragment(), true);
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (isAppInitialized) {
            fragmentTransaction.add(R.id.framelayout, fragment);
        } else {
            fragmentTransaction.replace(R.id.framelayout, fragment);
        }
        fragmentTransaction.commit();
    }
    public interface UserRoleCallback {
        void onRoleRetrieved(String role);
        void onError(String error);
    }


    private void getUserRole(String userId, UserRoleCallback callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    callback.onRoleRetrieved(role);
                } else {
                    callback.onError("User data does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }



}