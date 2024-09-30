package com.example.JapLearn;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;


import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;

import java.io.InputStream;
import android.Manifest;

public class ProfileFragment extends Fragment {

    private TextView emailTextView, usernameTextView;
    private ImageView detailImage;
    private DatabaseReference usersRef;
    private FirebaseAuth firebaseAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
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

        TextView dailyStreakTextView = view.findViewById(R.id.DailyStreakTextView);
        TextView hiraganaProgressTextView = view.findViewById(R.id.HiraganaProgressTextView);
        TextView katakanaProgressTextView = view.findViewById(R.id.KatakanaProgressTextView);
        TextView vocabProgressTextView = view.findViewById(R.id.VocabProgressTextView);
        TextView kanaShootWaveTextView = view.findViewById(R.id.KanaShootWaveTextView);
        TextView kanaShootHSTextView = view.findViewById(R.id.KanaShootHSTextView);
        TextView nihongoRaceTextView = view.findViewById(R.id.NihongoRaceTextView);
        TextView nihongoRaceWinsTextView = view.findViewById(R.id.NihongoRaceWinsTextView);
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://jlearn-25b34-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = firebaseDatabase.getReference("users");

        // Retrieve user ID of the user whose profile is to be displayed
        Bundle arguments = getArguments();
        String userId;
        if (arguments != null && arguments.containsKey("userId")) {
            userId = arguments.getString("userId");
        } else {
            userId = firebaseAuth.getCurrentUser().getUid();
        }

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
                        hiraganaProgressTextView.setText("Hiragana Progress: " + user.getHiraganaProgress());
                        katakanaProgressTextView.setText("Katakana Progress: " + user.getKatakanaProgress());
                        vocabProgressTextView.setText("Vocabulary Progress: " + user.getVocabularyProgress());
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

        return view;
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



}
