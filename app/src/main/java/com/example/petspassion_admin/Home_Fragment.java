package com.example.petspassion_admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Home_Fragment extends Fragment {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noProductsFound;
    private List<DataClass> productList;
    private AdminProductAdapter adminProductAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        noProductsFound = rootView.findViewById(R.id.no_products_found);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));  // Set the layout manager for the RecyclerView
        productList = new ArrayList<>();
        adminProductAdapter = new AdminProductAdapter(productList, getContext());
        recyclerView.setAdapter(adminProductAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        loadProducts();   // Load products from the Firebase database

        return rootView;
    }



    //............................................. Method to load products from the Firebase database...........................................................
    private void loadProducts() {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products");   // Reference to the "Products" node in the Firebase database
        productRef.addValueEventListener(new ValueEventListener() {
            // Attach a ValueEventListener to listen for changes in the database
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                // Iterate over all products in the snapshot
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    // Convert each snapshot to a DataClass object
                    DataClass product = productSnapshot.getValue(DataClass.class);
                    if (product != null) {
                        // Set the product ID using the Firebase key
                        product.setProduct_id(productSnapshot.getKey());
                        productList.add(product);// Add the product to the list
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }




    //............................................... Method to update the UI based on the product list..................................................
    private void updateUI() {
        if (productList.isEmpty()) {
            // Show a message if no products are found
            showEmptyState();
        } else {
            // If products are found, display the RecyclerView
            noProductsFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adminProductAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
    }



    // .......................................Method to show the "No Products Found" message and hide the RecyclerView.......................................................
    public void showEmptyState() {
        noProductsFound.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }


    //........................................ Method to refresh data when SwipeRefreshLayout is triggered.............................................................
    private void refreshData() {
        loadProducts();
    }
}
