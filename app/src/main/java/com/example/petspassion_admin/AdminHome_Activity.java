package com.example.petspassion_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayDeque;
import java.util.Deque;

public class AdminHome_Activity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;


    //.............................. Deque (double-ended queue) is used to keep track of the navigation stack of fragments........................................
    Deque<Integer> integerDeque = new ArrayDeque<>(4);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        getWindow().setStatusBarColor(ContextCompat.getColor(AdminHome_Activity.this, R.color.adminHome));


        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Push the default fragment (home) into the stack and load the Home fragment
        integerDeque.push(R.id.home);
        loadFragment(new Home_Fragment());


        // Set the Home tab as the default selected item
        bottomNavigationView.setSelectedItemId(R.id.home);




        //............................... Set the item selected listener for the bottom navigation.........................................
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // If the selected fragment is already in the stack, remove it to avoid duplication
                if (integerDeque.contains(id)) {
                    integerDeque.remove(id);
                }

                // Push the newly selected fragment to the top of the stack
                integerDeque.push(id);

                // Load the corresponding fragment based on the item selected
                loadFragment(getFragment(item.getItemId()));
                return true;
            }
        });
    }



    // ....................................Method to determine which fragment to load based on the selected menu item...............................................
    private Fragment getFragment(int itemId) {
        if (itemId == R.id.home) {
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
            return new Home_Fragment();
        }
        else if (itemId == R.id.insert) {
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            return new Insert_Fragment();
        }
        else if (itemId == R.id.order) {
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
            return new Order_Fragment();
        } else {
            bottomNavigationView.getMenu().getItem(3).setChecked(true);
            return new Profile_Fragment();
        }
    }



    //............................. Method to load a fragment into the frame layout (the container for fragments)......................................
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout, fragment, fragment.getClass().getSimpleName())
                .commit();
    }



    // .............................. the back button press action to handle fragment navigation...................................
    @Override
    public void onBackPressed() {
        // Get the current fragment displayed
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.framelayout);
        // If the current fragment is the Home fragment, show an exit confirmation dialog
        if (currentFragment instanceof Home_Fragment) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish(); // Exit the app
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        // If the current fragment is not Home, pop the top fragment from the stack
        else {
            integerDeque.pop();
            // If the stack is not empty, load the fragment at the top of the stack
            if (!integerDeque.isEmpty()) {
                loadFragment(getFragment(integerDeque.peek()));
            }
            // If the stack is empty, use the default back button behavior
            else {
                super.onBackPressed();
            }
        }
    }
}
