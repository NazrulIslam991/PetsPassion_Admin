package com.example.petspassion_admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.adminHome));

        new Handler().postDelayed(() ->{
            Intent intent = new Intent(MainActivity.this,Login_Activity.class);
            startActivity(intent);
            finish();

        },800);

    }
}