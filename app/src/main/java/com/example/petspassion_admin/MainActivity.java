package com.example.petspassion_admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.adminHome));

        sessionManager = new SessionManager(this);

        new Handler().postDelayed(() -> {
            // Check if the user is already logged in
            if (sessionManager.isLoggedIn()) {
                // If logged in, navigate to AdminHome_Activity
                Intent intent = new Intent(MainActivity.this, AdminHome_Activity.class);
                startActivity(intent);
            } else {
                // If not logged in, navigate to Login_Activity
                Intent intent = new Intent(MainActivity.this, Login_Activity.class);
                startActivity(intent);
            }
            finish();
        }, 800);
    }
}
