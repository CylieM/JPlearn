package com.example.JapLearn;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity implements View.OnClickListener {

    private TableLayout tableLayout;
    private TextView headerLastWave;
    UserModel userModel;
    FirebaseUser user;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard); // Update to your new layout XML
        // Initialize components
        initializeComponents();
        bottomNavigationView.setSelectedItemId(R.id.navLeaderboard);
        checkUserAuthentication();
        setBottomNavigationListener();
        tableLayout = findViewById(R.id.tableLayout);
        headerLastWave = findViewById(R.id.lastColumnHeader);


        // Fetch leaderboard data based on the default category
        fetchLeaderboardData("Progression");

        // Set onClickListener for buttons
        findViewById(R.id.buttonProgression).setOnClickListener(this);
        findViewById(R.id.buttonKanaShoot).setOnClickListener(this);
        findViewById(R.id.buttonNihongoRace).setOnClickListener(this);


    }
    private void initializeComponents() { // Initializes the necessary UI components and the UserModel.
        bottomNavigationView = findViewById(R.id.bottomNavView);
        userModel = new UserModel();
        user = userModel.getFirebaseAuth().getCurrentUser();
    }
    @Override
    public void onResume() {
        super.onResume();
        // Disable back button handling when the activity is visible
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to prevent back navigation
                // Optionally show a message or toast if you want
                Toast.makeText(LeaderboardActivity.this, "Use the logout option to exit", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onClick(View view) {
        onLeaderboardButtonClick(view);
    }

    private void onLeaderboardButtonClick(View view) {
        int viewId = view.getId();
        String category;

        if (viewId == R.id.buttonProgression) {
            category = "Progression";
        } else if (viewId == R.id.buttonKanaShoot) {
            category = "KanaShoot";
        } else if (viewId == R.id.buttonNihongoRace) {
            category = "NihongoRace";
        } else {
            return; // Ignore clicks on unknown views
        }

        fetchLeaderboardData(category);
    }
    private void setBottomNavigationListener() { // Sets the listener for the bottom navigation view to handle item selections.
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return handleNavigationItemSelected(menuItem.getItemId());
            }
        });
    }
    private boolean handleNavigationItemSelected(int itemId) { // Handles the selection of the navigation items.
        Intent intent = null;

        if (itemId == R.id.navHome) {
            intent = new Intent(LeaderboardActivity.this, HomeActivity.class);
        } else if (itemId == R.id.navLeaderboard) {
            intent = new Intent(LeaderboardActivity.this, LeaderboardActivity.class);
        } else if (itemId == R.id.navKanashoot) {
            intent = new Intent(LeaderboardActivity.this, KanaShootActivity.class);
        } else if (itemId == R.id.navNihongorace) {
            intent = new Intent(LeaderboardActivity.this, NihongoRaceActivity.class);
        } else if (itemId == R.id.navProfile) {
            handleProfileSelection();
            return false;  // Return false to not select the profile item
        }

        if (intent != null) {
            startActivity(intent);
            finish();  // Optionally, you may want to finish the current activity to prevent going back to it.
        }

        return true;  // Item was handled
    }

    private void handleProfileSelection() {
        getUserRole(user.getUid(), new HomeActivity.UserRoleCallback() {
            @Override
            public void onRoleRetrieved(String role) {
                showProfilePopupMenu(role);
            }

            @Override
            public void onError(String error) {
                Log.e("User Role", "Error: " + error);
            }
        });
    }

    private void showProfilePopupMenu(String role) { //Displays the profile popup menu based on the user's role.
        View view = bottomNavigationView.findViewById(R.id.navProfile);
        PopupMenu popupMenu = new PopupMenu(LeaderboardActivity.this, view);

        if ("Admin".equals(role)) {
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
        } else if ("Teacher".equals(role)) {
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu_teacher, popupMenu.getMenu());
        } else {
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu_student, popupMenu.getMenu());
        }

        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return handleProfilePopupMenuItemClick(item.getItemId());
            }
        });
    }

    private boolean handleProfilePopupMenuItemClick(int itemId) {
        Intent intent = null;

        if (itemId == R.id.navProfile) {
            intent = new Intent(LeaderboardActivity.this, ProfileActivity.class);
        } else if (itemId == R.id.navLogout) {
            handleLogout();
            return true;  // Return true to indicate the logout action was handled
        } else if (itemId == R.id.navAdminPanel) {
            intent = new Intent(LeaderboardActivity.this, LessonItemList.class);
        } else if (itemId == R.id.navUserPanel) {
            intent = new Intent(LeaderboardActivity.this, UserManagementActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            finish();
        }
        return false;  // Return false if no intent was started
    }

    private void handleLogout() { //Logs out the user and redirects to the login activity.
        userModel.getFirebaseAuth().signOut();
        redirectToLogin();
    }

    public interface UserRoleCallback {
        void onRoleRetrieved(String role);
        void onError(String error);
    }

    private void getUserRole(String userId, HomeActivity.UserRoleCallback callback) { //Fetches the user's role from the database.
        DatabaseReference userRef = userModel.getUserRef(userId);
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
    private void fetchLeaderboardData(final String category) {
        Log.d("Leaderboard", "Fetching data for category: " + category);
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");

        switch (category) {
            case "Progression":
                headerLastWave.setText("Progression");
                headerLastWave.setTextColor(Color.BLACK);
                break;
            case "KanaShoot":
                headerLastWave.setText("Wave");
                headerLastWave.setTextColor(Color.BLACK);
                break;
            case "NihongoRace":
                headerLastWave.setText("WPM");
                headerLastWave.setTextColor(Color.BLACK);
                break;
            default:
                headerLastWave.setText("Score");
                headerLastWave.setTextColor(Color.BLACK);
                break;
        }

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tableLayout.removeAllViews();

                List<UserModel.User> userList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    UserModel.User user = data.getValue(UserModel.User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                // Sort users by category value in descending order
                userList.sort((user1, user2) -> {
                    int value1 = getCategoryValue(user1, category);
                    int value2 = getCategoryValue(user2, category);
                    return Integer.compare(value2, value1);
                });

                // Display only the top 10 users
                int rank = 1;
                for (UserModel.User user : userList) {
                    if (rank > 10) break; // Limit to top 10
                    int categoryValue = getCategoryValue(user, category);
                    if (categoryValue > 0) {
                        addTableRow(rank, user.getUsername(), categoryValue, user.getProfilePicture());
                        rank++;
                    }
                }

                // Optional: You can add a total progression value display at the end if required
                int totalProgression = calculateTotalProgression(userList);
                addTotalProgressionRow(totalProgression);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }


    // Method to calculate total progression value from all users
    private int calculateTotalProgression(List<UserModel.User> userList) {
        int total = 0;
        for (UserModel.User user : userList) {
            total += getCategoryValue(user, "Progression");
        }
        return total;
    }


    // Method to add a row to display the total progression value in the leaderboard
    private void addTotalProgressionRow(int totalProgression) {
        TableRow totalRow = new TableRow(this);
        TextView totalText = new TextView(this);
        totalText.setTextColor(Color.BLACK);
        totalText.setTextSize(18);
        totalRow.addView(totalText);
        tableLayout.addView(totalRow);
    }

    private void checkUserAuthentication() { //Checks if the user is authenticated. If not, redirects to the login activity.
        if (user == null) {
            redirectToLogin();
        }
    }
    private void redirectToLogin() { //Redirects the user to the login activity.
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private int getCategoryValue(UserModel.User user, String category) {
        switch (category) {
            case "Progression":
                return user.getHiraganaProgress() + user.getKatakanaProgress() + user.getVocabularyProgress();
            case "KanaShoot":
                return user.getKSWaves();
            case "NihongoRace":
                return user.getNRaceBestWPM();
            default:
                return 0;
        }
    }

    private void addTableRow(int rank, String username, int categoryValue, String profilePictureUrl) {
        View tableRow = getLayoutInflater().inflate(R.layout.table_row_leaderboard, null); // Inflate your table_row_leaderboard layout

        TextView rankTextView = tableRow.findViewById(R.id.rankTextView);
        rankTextView.setText(String.valueOf(rank));

        ImageView profileImageView = tableRow.findViewById(R.id.profileImageView);
        Glide.with(this)
                .load(profilePictureUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.loading)
                        .circleCrop())
                .into(profileImageView);

        TextView usernameTextView = tableRow.findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);

        TextView categoryValueTextView = tableRow.findViewById(R.id.categoryValueTextView);
        categoryValueTextView.setText(String.valueOf(categoryValue));

        tableLayout.addView(tableRow);
    }



}
