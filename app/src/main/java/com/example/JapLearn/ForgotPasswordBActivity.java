package com.example.JapLearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordBActivity extends AppCompatActivity {

    public EditText editPassword, enterPassword;
    Button btnResetPassword;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_b);

        firebaseAuth = FirebaseAuth.getInstance();
        editPassword = findViewById(R.id.editPassword);
        enterPassword = findViewById(R.id.enterPassword);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = editPassword.getText().toString().trim();
                String reEnterPassword = enterPassword.getText().toString().trim();

                if (newPassword.isEmpty() || reEnterPassword.isEmpty()) {
                    Toast.makeText(ForgotPasswordBActivity.this, "Please enter and confirm your new password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(reEnterPassword)) {
                    Toast.makeText(ForgotPasswordBActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordBActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ForgotPasswordBActivity.this, "Password update failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(ForgotPasswordBActivity.this, "User is not logged in", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
