package com.example.petspassion_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Admin_ShowCategories extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<DataClass> categoryList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_show_categories);


        getWindow().setStatusBarColor(ContextCompat.getColor(Admin_ShowCategories.this, R.color.adminHome));


        recyclerView = findViewById(R.id.admin_category_recycler_view);
        // Use GridLayoutManager with 4 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(this, categoryList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Categories");

        fetchCategories();

        // Register for context menu
        registerForContextMenu(recyclerView);
    }

    private void fetchCategories() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataClass category = snapshot.getValue(DataClass.class);
                    categoryList.add(category);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Admin_ShowCategories.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.category_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.update_category) {
            // Handle update action
            return true;
        } else if (item.getItemId() == R.id.delete_category) {
            // Handle delete action
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

}