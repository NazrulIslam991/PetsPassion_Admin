package com.example.petspassion_admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UpdateProductActivity extends AppCompatActivity {

    private EditText updateProductName, updateCategories, updateProductDescription, updateProductPrice, updateProductDiscount, updateProductQuantity;
    private ImageView updateProductImage, update_to_admin_home_page;
    private Button updateProductBtn;
    private String productId;
    private Uri imageUri;
    private ALodingDialog aLodingDialog;

    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private DatabaseReference categoriesRef;

    private List<String> categoriesList = new ArrayList<>();
    private int selectedCategoryIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        updateProductName = findViewById(R.id.update_product_name);
        updateCategories = findViewById(R.id.update_categories);
        updateProductDescription = findViewById(R.id.update_product_description);
        updateProductPrice = findViewById(R.id.update_product_price);
        updateProductDiscount = findViewById(R.id.update_product_Discount); // Ensure this matches your XML ID
        updateProductQuantity = findViewById(R.id.update_product_quantity);
        updateProductImage = findViewById(R.id.update_product_image);
        //updateBtnSelectImage = findViewById(R.id.update_btn_select_image);
        updateProductBtn = findViewById(R.id.update_product);



        getWindow().setStatusBarColor(ContextCompat.getColor(UpdateProductActivity.this, R.color.adminHome));

        update_to_admin_home_page = findViewById(R.id.update_to_admin_home_page);
        update_to_admin_home_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        productId = getIntent().getStringExtra("productId");


        // ................................................Initialize Firebase references for product data and images.............................................
        databaseRef = FirebaseDatabase.getInstance().getReference("Products").child(productId);
        storageRef = FirebaseStorage.getInstance().getReference("ProductImages");
        categoriesRef = FirebaseDatabase.getInstance().getReference("Categories");

        aLodingDialog = new ALodingDialog(UpdateProductActivity.this);


        // Disable default title if using custom TextView
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        //........................................ Load the product details and categories from Firebase..........................................................
        loadProductDetails();
        loadCategories();



        //........................................... Set up listeners for selecting an image and category........................................................
        updateProductImage.setOnClickListener(v -> selectImage());
        updateCategories.setOnClickListener(v -> showCategoryDialog());


        // Listener to handle updating the product
        updateProductBtn.setOnClickListener(v -> updateProductDetails());
    }





    // ......................................Load product details from Firebase...........................................................
    private void loadProductDetails() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataClass product = snapshot.getValue(DataClass.class);
                if (product != null) {
                    // Populate fields with product data
                    updateProductName.setText(product.getProduct_name());
                    updateCategories.setText(product.getProduct_categories());
                    updateProductDescription.setText(product.getProduct_description());
                    updateProductPrice.setText(product.getProduct_price());
                    updateProductDiscount.setText(product.getProduct_discount()); // Load discount
                    updateProductQuantity.setText(product.getProduct_quantity());

                    // Load the product image using Picasso
                    Picasso.get().load(product.getProduct_image()).into(updateProductImage);


                    // Find the selected category in the categories list
                    for (int i = 0; i < categoriesList.size(); i++) {
                        if (categoriesList.get(i).equals(product.getProduct_categories())) {
                            selectedCategoryIndex = i;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProductActivity.this, "Failed to load product details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    //.............................................. Load categories from Firebase.......................................................
    private void loadCategories() {
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesList.clear();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryName = categorySnapshot.child("categories_name").getValue(String.class);
                    if (categoryName != null) {
                        categoriesList.add(categoryName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProductActivity.this, "Failed to load categories: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    //.................................................. Show a dialog for selecting a category......................................................................
    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateProductActivity.this);
        builder.setTitle("Select Category");

        String[] categoriesArray = categoriesList.toArray(new String[0]);

        builder.setSingleChoiceItems(categoriesArray, selectedCategoryIndex, (dialog, which) -> selectedCategoryIndex = which);

        builder.setPositiveButton("OK", (dialog, which) -> {
            if (selectedCategoryIndex != -1) {
                updateCategories.setText(categoriesArray[selectedCategoryIndex]);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }




    // ..............................................Open the gallery to select an image.........................................................
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }


    //.......................................... Handle the result of the image selection...........................................................
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            updateProductImage.setImageURI(imageUri);
        }
    }


    //............................................... Update the product details in Firebase........................................................
    private void updateProductDetails() {
        String name = updateProductName.getText().toString().trim();
        String categories = updateCategories.getText().toString().trim();
        String description = updateProductDescription.getText().toString().trim();
        String price = updateProductPrice.getText().toString().trim();
        String discount = updateProductDiscount.getText().toString().trim(); // Get discount
        String quantity = updateProductQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(categories) || TextUtils.isEmpty(description) || TextUtils.isEmpty(price) || TextUtils.isEmpty(quantity) || TextUtils.isEmpty(discount)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        aLodingDialog.show();


        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(productId + ".jpg");
            fileRef.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateProductInDatabase(name, categories, description, price, discount, quantity, uri.toString());
                    });
                } else {
                    aLodingDialog.dismiss();
                    Toast.makeText(UpdateProductActivity.this, "Image upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String imageUrl = snapshot.child("product_image").getValue(String.class);
                    updateProductInDatabase(name, categories, description, price, discount, quantity, imageUrl);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    aLodingDialog.dismiss();
                    Toast.makeText(UpdateProductActivity.this, "Failed to get image URL: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    // ...................................................Update the product data in Firebase.................................................................
    private void updateProductInDatabase(String name, String categories, String description, String price, String discount, String quantity, String imageUrl) {
        // Include the product_id in the DataClass
        DataClass updatedProduct = new DataClass(productId, name, categories, description, price, discount, quantity, imageUrl);

        // Save the updated product to the database, with the product ID
        databaseRef.setValue(updatedProduct).addOnCompleteListener(task -> {
            aLodingDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(UpdateProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity after successful update
            } else {
                Toast.makeText(UpdateProductActivity.this, "Failed to update product: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
