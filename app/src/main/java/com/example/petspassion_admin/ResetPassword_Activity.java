package com.example.petspassion_admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword_Activity extends AppCompatActivity {
    private EditText resetPass;
    private Button reset_button;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        getWindow().setStatusBarColor(ContextCompat.getColor(ResetPassword_Activity.this, R.color.adminHome));


        resetPass = findViewById(R.id.resetemail);
        reset_button = findViewById(R.id.resetbutton);
        firebaseAuth = FirebaseAuth.getInstance();


        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = resetPass.getText().toString().trim();

                // Check if the email field is empty
                if (email.isEmpty()) {
                    resetPass.requestFocus();
                    resetPass.setError("Email cannot be empty");
                    return;
                }

                // Check if the email format is valid
                else if (!email.matches("^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*@(gmail\\.com|outlook\\.com|yahoo\\.com)$") && !email.matches("^cse_[0-9]{16}@lus\\.ac\\.bd$")) {
                    resetPass.requestFocus();
                    resetPass.setError("Invalid email format");
                    return;
                }
                else{
                    // Add logic to only allow a specific email
                    if (email.equals("nazrulislamnayon991@gmail.com")) {
                        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPassword_Activity.this, "Password reset email is sent.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ResetPassword_Activity.this, Login_Activity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(ResetPassword_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ResetPassword_Activity.this, "Error: This email is not authorized for password reset.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
