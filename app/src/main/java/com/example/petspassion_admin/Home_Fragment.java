package com.example.petspassion_admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mancj.materialsearchbar.MaterialSearchBar;
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
    private MaterialSearchBar searchBar; // MaterialSearchBar for searching

    private List<DataClass> productList;    // List to hold products
    private List<DataClass> originalProductList; // To restore original list

    private AdminProductAdapter adminProductAdapter;  // Adapter for RecyclerView

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        noProductsFound = rootView.findViewById(R.id.no_products_found);
        searchBar = rootView.findViewById(R.id.searchBar); // Reference to MaterialSearchBar

        // Set up RecyclerView with LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the product lists and adapter
        productList = new ArrayList<>();
        originalProductList = new ArrayList<>();
        adminProductAdapter = new AdminProductAdapter(productList, getContext());
        recyclerView.setAdapter(adminProductAdapter);

        // Set up swipe-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Load products from Firebase
        loadProducts();

        // Set up the search bar for filtering products
        setupSearchBar();

        return rootView;
    }

    // ..............................................Method to configure the search functionality (MaterialSearchBar)..........................................................
    private void setupSearchBar() {
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    restoreOriginalProductList();  // If search is closed, restore the original list
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                if (!TextUtils.isEmpty(text)) {
                    searchProducts(text.toString()); // Search products based on text input
                }
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                // Handle extra button clicks like voice search
            }
        });
    }

    // ..................................................Method to load products from Firebase database....................................................
    private void loadProducts() {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products");
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();

                // Loop through the products and add them to the list
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    DataClass product = productSnapshot.getValue(DataClass.class);
                    if (product != null) {
                        product.setProduct_id(productSnapshot.getKey());  // Set product ID
                        productList.add(product);  // Add to product list
                    }
                }

                originalProductList = new ArrayList<>(productList);  // Keep a copy of the original list
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    // ................................................Method to search products based on input.............................................................
    private void searchProducts(String query) {
        List<DataClass> filteredList = new ArrayList<>();
        String searchQuery = query.toLowerCase().trim();  // Normalize query to lowercase

        try {
            double maxPrice = Double.parseDouble(searchQuery); // Try to parse the query as a price
            for (DataClass product : originalProductList) {
                double productPrice = Double.parseDouble(product.getProduct_price());
                if (productPrice <= maxPrice) {
                    filteredList.add(product);  // Add products matching the price range
                }
            }
        } catch (NumberFormatException e) {
            // If it's not a price, search by name or product ID
            for (DataClass product : originalProductList) {
                String productName = product.getProduct_name().toLowerCase();
                String productId = product.getProduct_id().toLowerCase();

                if (productName.contains(searchQuery) || productId.contains(searchQuery)) {
                    filteredList.add(product);  // Add products matching name or ID
                }
            }
        }

        updateFilteredList(filteredList);
    }

    // ............................................Method to update filtered product list in the UI.......................................................
    private void updateFilteredList(List<DataClass> filteredList) {
        if (filteredList.isEmpty()) {
            showEmptyState();  // Show "no products found" if no matches
        } else {
            noProductsFound.setVisibility(View.GONE);  // Hide "no products found" text
            recyclerView.setVisibility(View.VISIBLE);
        }

        productList.clear();  // Clear existing products and add the filtered ones
        productList.addAll(filteredList);
        adminProductAdapter.notifyDataSetChanged();
    }

    // ..............................................Method to restore the original product list....................................................
    private void restoreOriginalProductList() {
        productList.clear();
        productList.addAll(originalProductList);
        adminProductAdapter.notifyDataSetChanged();
        updateUI();
    }

    // .....................................................Method to update the UI when products are loaded................................................
    private void updateUI() {
        if (productList.isEmpty()) {
            showEmptyState();
        } else {
            noProductsFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adminProductAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation
    }

    // .....................................Method to show the "No Products Found" message........................................................
    private void showEmptyState() {
        noProductsFound.setVisibility(View.VISIBLE);  // Show "no products found" message
        recyclerView.setVisibility(View.GONE);
    }

    // .......................................... Method to refresh product data when user swipes down.....................................................
    private void refreshData() {
        loadProducts();  // Reload products from Firebase
    }
}
