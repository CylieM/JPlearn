package com.example.JapLearn;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

import android.Manifest;
import android.widget.Toast;


public class ProfileFragment extends Fragment {

    private TextView emailTextView, usernameTextView, progHiragana, progKatakana, progVocab, progGrammar;
    private ImageView detailImage;
    private DatabaseReference usersRef;
    private FirebaseAuth firebaseAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        // Initialize views
        emailTextView = view.findViewById(R.id.emailTextView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        detailImage = view.findViewById(R.id.detailImage);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        detailImage = view.findViewById(R.id.detailImage);
        progHiragana = view.findViewById(R.id.prog_hiragana);
        progKatakana = view.findViewById(R.id.prog_katakana);
        progVocab = view.findViewById(R.id.prog_vocab);
        progGrammar = view.findViewById(R.id.prog_grammar);


        TextView dailyStreakTextView = view.findViewById(R.id.DailyStreakTextView);
        TextView hiraganaProgressTextView = view.findViewById(R.id.HiraganaProgressTextView);
        TextView katakanaProgressTextView = view.findViewById(R.id.KatakanaProgressTextView);
        TextView vocabProgressTextView = view.findViewById(R.id.VocabProgressTextView);
        TextView grammarProgressTextView = view.findViewById(R.id.GrammarProgressTextView);
        TextView kanaShootWaveTextView = view.findViewById(R.id.KanaShootWaveTextView);
        TextView kanaShootHSTextView = view.findViewById(R.id.KanaShootHSTextView);
        TextView nihongoRaceTextView = view.findViewById(R.id.NihongoRaceTextView);
        TextView nihongoRaceWinsTextView = view.findViewById(R.id.NihongoRaceWinsTextView);
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = firebaseDatabase.getReference("users");

        // Retrieve user ID
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("userId")) {
            userId = arguments.getString("userId");
        } else {
            userId = firebaseAuth.getCurrentUser().getUid();
        }

        // Fetch current lesson progress



        // Retrieve and display user's username and profile picture from Realtime Database
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
                        hiraganaProgressTextView.setText("Hiragana Progress: " );
                        katakanaProgressTextView.setText("Katakana Progress: " );
                        vocabProgressTextView.setText("Vocabulary Progress: " );
                        grammarProgressTextView.setText("Grammar Progress: ");
                        kanaShootWaveTextView.setText("Kanashoot Game Mode final wave: " + user.getKSWaves());
                        kanaShootHSTextView.setText("Kanashoot Game Mode highest score: " + user.getKSHighScore());
                        nihongoRaceTextView.setText("NihongoRace Game Mode best WPM: " + user.getNRaceBestWPM());
                        nihongoRaceWinsTextView.setText("NihongoRace Game Mode First Place Wins: " + user.getNRaceFirstPlace());
                        Glide.with(getActivity())
                                .load(user.getProfilePicture()) // Load image from profilePicture URL or Firebase Storage reference
                                .into(detailImage);
                    } else {
                        usernameTextView.setText("Username: Not Found");
                        emailTextView.setText("Email: Not Found");
                    }
                } else {
                    usernameTextView.setText("Username: Error");
                    emailTextView.setText("Email: Error");
                }
            }
        });

        // Disable profile picture change functionality when viewing other user's profile
        if (userId.equals(firebaseAuth.getCurrentUser().getUid())) {
            detailImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open image picker to select new profile picture
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            });
        } else {
            detailImage.setOnClickListener(null);
        }

        fetchCurrentLesson();


        return view;


    }

    private void fetchCurrentLesson() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentLesson = dataSnapshot.child("currentLesson").getValue(String.class);

                    // Set the progress of Hiragana, Katakana, Vocab, and Grammar based on currentLesson
                    switch (currentLesson) {
                        case "1":
                            progHiragana.setText("In-Progress");
                            progKatakana.setText("Locked");
                            progVocab.setText("Locked");
                            progGrammar.setText("Locked");
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
                            progHiragana.setText("Completed");
                            progKatakana.setText("Completed");
                            progVocab.setText("Completed");
                            progGrammar.setText("Completed");
                            break;
                    }
                } else {
                    Log.e(TAG, "User data does not exist for userId: " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error fetching user data", databaseError.toException());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                // Update ImageView with the selected image
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
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
                                // ...
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    }
}
