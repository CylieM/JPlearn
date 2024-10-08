package com.example.JapLearn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private TextView progHiragana, progKatakana, progVocab, progGrammar, emailTextView, usernameTextView, dailyStreakTextView, hiraganaProgressTextView, katakanaProgressTextView, vocabProgressTextView, kanaShootWaveTextView, kanaShootHSTextView, grammarProgressTextView, nihongoRaceTextView, nihongoRaceWinsTextView;
    private ImageView detailImage;
    private DatabaseReference usersRef;
    private FirebaseAuth firebaseAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private String userId;
    UserModel userModel;
    FirebaseUser user;
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initializeComponents();
        bottomNavigationView.setSelectedItemId(R.id.navProfile);
        checkUserAuthentication();
        setBottomNavigationListener();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        // Initialize views
        emailTextView = findViewById(R.id.emailTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        dailyStreakTextView = findViewById(R.id.DailyStreakTextView);
        hiraganaProgressTextView = findViewById(R.id.HiraganaProgressTextView);
        katakanaProgressTextView = findViewById(R.id.KatakanaProgressTextView);
        vocabProgressTextView = findViewById(R.id.VocabProgressTextView);
        grammarProgressTextView = findViewById(R.id.GrammarProgressTextView);
        kanaShootWaveTextView = findViewById(R.id.KanaShootWaveTextView);
        kanaShootHSTextView = findViewById(R.id.KanaShootHSTextView);
        nihongoRaceTextView = findViewById(R.id.NihongoRaceTextView);
        nihongoRaceWinsTextView = findViewById(R.id.NihongoRaceWinsTextView);
        detailImage = findViewById(R.id.detailImage);
        progHiragana =findViewById(R.id.prog_hiragana);
        progKatakana = findViewById(R.id.prog_katakana);
        progVocab = findViewById(R.id.prog_vocab);
        progGrammar = findViewById(R.id.prog_grammar);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = firebaseDatabase.getReference("users");

        // Determine if this activity is launched from UserManagementActivity
        Intent intent = getIntent();
        boolean isFromUserManagement = intent.getBooleanExtra("isFromUserManagement", false);
        String userId;

        if (isFromUserManagement) {
            // Get the user ID of the selected user from the intent
            userId = intent.getStringExtra("userId");
        } else {
            // Get the user ID of the logged-in user
            userId = firebaseAuth.getCurrentUser().getUid();

        }

        if (userId != null) {
            // Retrieve and display user's data from Realtime Database
            usersRef.child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        UserModel.User user = dataSnapshot.getValue(UserModel.User.class);
                        if (user != null) {
                            usernameTextView.setText("Username: " + user.getUsername());
                            emailTextView.setText("Email: " + user.getEmail());
                            dailyStreakTextView.setText("Daily Streak: " + user.getDailyStreak());
                            kanaShootWaveTextView.setText("Kanashoot Game Mode final wave: " + user.getKSWaves());
                            kanaShootHSTextView.setText("Kanashoot Game Mode highest score: " + user.getKSHighScore());
                            nihongoRaceTextView.setText("NihongoRace Game Mode best WPM: " + user.getNRaceBestWPM());
                            nihongoRaceWinsTextView.setText("NihongoRace Game Mode First Place Wins: " + user.getNRaceFirstPlace());
                            hiraganaProgressTextView.setText("Hiragana Progress: ");
                            katakanaProgressTextView.setText("Katakana Progress: ");
                            vocabProgressTextView.setText("Vocabulary Progress: " );
                            grammarProgressTextView.setText("Grammar Progress: " );

                            Glide.with(ProfileActivity.this)
                                    .load(user.getProfilePicture())
                                    .into(detailImage);
                        }
 else {
                            usernameTextView.setText("Username: Not Found");
                            emailTextView.setText("Email: Not Found");
                        }
                    } else {
                        usernameTextView.setText("Username: Error");
                        emailTextView.setText("Email: Error");
                    }
                }
            });
        }

        // Enable profile picture change functionality
        detailImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open image picker to select new profile picture
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        fetchCurrentLesson();

    }


    private void fetchCurrentLesson() {

        if (firebaseAuth.getCurrentUser() != null) {
            userId = firebaseAuth.getCurrentUser().getUid();
        }
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentLesson = dataSnapshot.child("currentLesson").getValue(String.class);

                    switch (currentLesson) {
                        case "1":
                            progHiragana.setText("In-Progress");
                            progKatakana.setText("Locked");
                            progVocab.setText("Locked");
                            progGrammar.setText("Locked");
//                            kanaShootWaveTextView.setText("Kanashoot Game Mode final wave: " + user.getKSWaves());
//                            kanaShootHSTextView.setText("Kanashoot Game Mode highest score: " + user.getKSHighScore());
//                            nihongoRaceTextView.setText("NihongoRace Game Mode best WPM: " + user.getNRaceBestWPM());
//                            nihongoRaceWinsTextView.setText("NihongoRace Game Mode First Place Wins: " + user.getNRaceFirstPlace());
                            break;
                        case "2":
                            progHiragana.setText("Completed");
                            progKatakana.setText("In-Progress");
                            progVocab.setText("Locked");
                            progGrammar.setText("Locked");
                            break;
                        case "3":
                            progHiragana.setText("Completed");
                            progKatakana.setText("Completed");
                            progVocab.setText("In-Progress");
                            progGrammar.setText("Locked");
                            break;
                        case "4":
                            progHiragana.setText("Completed");
                            progKatakana.setText("Completed");
                            progVocab.setText("Completed");
                            progGrammar.setText("In-Progress");
                            break;
                        default:
                            progHiragana.setText("In-Progress");
                            progKatakana.setText("Locked");
                            progVocab.setText("Locked");
                            progGrammar.setText("Locked");
                            break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                Log.e("ProfileActivity", "Database error: " + databaseError.getMessage());
            }

        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                // Update ImageView with the selected image
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, false);
                detailImage.setImageBitmap(resizedBitmap);

                // Get a reference to the storage service
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                // Create a reference to 'images/{userId}.jpg'
                String userId = firebaseAuth.getCurrentUser().getUid();
                StorageReference profilePicRef = storageRef.child("images/" + userId + ".jpg");

                // Upload the file to Firebase Storage
                profilePicRef.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get the download URL and save it to the database
                                profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        usersRef.child(userId).child("profilePicture").setValue(uri.toString());
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(ProfileActivity.this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                Toast.makeText(ProfileActivity.this, "Use the logout option to exit", Toast.LENGTH_SHORT).show();
            }
        });
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
            intent = new Intent(ProfileActivity.this, HomeActivity.class);
        } else if (itemId == R.id.navLeaderboard) {
            intent = new Intent(ProfileActivity.this, LeaderboardActivity.class);
        } else if (itemId == R.id.navKanashoot) {
            intent = new Intent(ProfileActivity.this, KanaShootActivity.class);
        } else if (itemId == R.id.navNihongorace) {
            intent = new Intent(ProfileActivity.this, NihongoRaceActivity.class);
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

    private void showProfilePopupMenu(String role) { //Displays the profile popup menu based on the user's role.
        View view = bottomNavigationView.findViewById(R.id.navProfile);
        PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, view);

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
            intent = new Intent(ProfileActivity.this, ProfileActivity.class);
        } else if (itemId == R.id.navLogout) {
            handleLogout();
            return true;  // Return true to indicate the logout action was handled
        } else if (itemId == R.id.navAdminPanel) {
            intent = new Intent(ProfileActivity.this, LessonItemList.class);
        } else if (itemId == R.id.navUserPanel) {
            intent = new Intent(ProfileActivity.this, UserManagementActivity.class);
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
}