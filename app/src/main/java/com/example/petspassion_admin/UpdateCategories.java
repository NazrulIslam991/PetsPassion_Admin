package com.example.petspassion_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class UpdateCategories extends AppCompatActivity {

    private ImageView categoryImageView, back;
    private EditText categoryNameEditText;
    private Button updateCategoryButton;
    private Uri imageUri;

    private StorageReference storageReference;
    private DatabaseReference categoryDatabaseReference;
    private DatabaseReference productDatabaseReference;


    private ALodingDialog aLodingDialog;

    private String categoryName;
    private String categoryImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_categories);

        categoryImageView = findViewById(R.id.categories_image);
        categoryNameEditText = findViewById(R.id.etCategoriesName);
        updateCategoryButton = findViewById(R.id.btnAddCategories);
        back = findViewById(R.id.categories_insert_to_admin_home_page);

        getWindow().setStatusBarColor(ContextCompat.getColor(UpdateCategories.this, R.color.adminHome));          // Set the status bar color


        // Initialize Firebase references
        storageReference = FirebaseStorage.getInstance().getReference("Categories");
        categoryDatabaseReference = FirebaseDatabase.getInstance().getReference("Categories");
        productDatabaseReference = FirebaseDatabase.getInstance().getReference("Products");





        // ..................................Set the back button click listener to finish the activity.......................................
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        aLodingDialog = new ALodingDialog(UpdateCategories.this);





        // .......................................Retrieve category data from the intent...............................................
        Intent intent = getIntent();
        categoryName = intent.getStringExtra("category_name");
        categoryImage = intent.getStringExtra("category_image");




        // Set the category name in the EditText and load the category image into the ImageView
        categoryNameEditText.setText(categoryName);
        Picasso.get().load(categoryImage).into(categoryImageView);



        // Set the imageView click listener to allow the user to select a new image
        categoryImageView.setOnClickListener(v -> selectImage());


        // Set the update button click listener to update the category
        updateCategoryButton.setOnClickListener(v -> updateCategory());
    }



    //......................................... Method to allow the user to select an image from their device................................................
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }




    //.................................. Handle the result of the image selection...................................................
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            categoryImageView.setImageURI(imageUri);
        }
    }




    // .........................................Method to update the category.........................................................
    private void updateCategory() {
        final String updatedCategoryName = categoryNameEditText.getText().toString().trim();

        if (updatedCategoryName.isEmpty()) {
            Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
            return;
        }

        aLodingDialog.show();

        // If a new image is selected, upload it to Firebase Storage
        if (imageUri != null) {
            final StorageReference fileRef = storageReference.child(UUID.randomUUID().toString() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the download URL of the uploaded image
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                @Override
                                public void onSuccess(Uri uri) {
                                    // Update the category in the database with the new name and image URL
                                    String updatedImageUrl = uri.toString();
                                    updateCategoryInDatabase(updatedCategoryName, updatedImageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        // Dismiss the loading dialog and show an error message if the image upload fails
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            aLodingDialog.dismiss();
                            Toast.makeText(UpdateCategories.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            updateCategoryInDatabase(updatedCategoryName, categoryImage);              // If no image was selected, proceed with updating the category with the existing image URL
        }
    }





    // .........................................Method to check if the updated category name already exists in the database....................................................
    private void updateCategoryInDatabase(String updatedCategoryName, String updatedImageUrl) {
        // Check if the updated category name already exists
        categoryDatabaseReference.orderByChild("category_name").equalTo(updatedCategoryName).get().addOnCompleteListener(checkTask -> {
            if (checkTask.isSuccessful() && checkTask.getResult().exists()) {
                // If a category with the updated name exists and it's not the current category
                if (!updatedCategoryName.equals(categoryName)) {
                    aLodingDialog.dismiss();
                    Toast.makeText(UpdateCategories.this, "Category name already exists. Please choose a different name.", Toast.LENGTH_SHORT).show();
                } else {
                    updateCategoryNameInDatabase(updatedCategoryName, updatedImageUrl);
                }
            } else {
                // If no category with the updated name exists, proceed with the update
                updateCategoryNameInDatabase(updatedCategoryName, updatedImageUrl);
            }
        });
    }






    // ............................................Method to update the category in the database once the name check is passed.......................................................
    private void updateCategoryNameInDatabase(String updatedCategoryName, String updatedImageUrl) {
        categoryDatabaseReference.orderByChild("category_name").equalTo(categoryName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                task.getResult().getChildren().forEach(snapshot -> {
                    snapshot.getRef().child("category_name").setValue(updatedCategoryName);
                    snapshot.getRef().child("category_image").setValue(updatedImageUrl)
                            .addOnSuccessListener(aVoid -> {
                                updateProductsCategoryName(categoryName, updatedCategoryName);
                            })
                            .addOnFailureListener(e -> {
                                aLodingDialog.dismiss();
                                Toast.makeText(UpdateCategories.this, "Failed to update category", Toast.LENGTH_SHORT).show();
                            });
                });
            } else {
                aLodingDialog.dismiss();
                Toast.makeText(UpdateCategories.this, "Category not found", Toast.LENGTH_SHORT).show();
            }
        });
    }





    //.......................................... Method to update all products that belong to the old category name to the new one.......................................................
    private void updateProductsCategoryName(String oldCategoryName, String newCategoryName) {
        productDatabaseReference.orderByChild("product_categories").equalTo(oldCategoryName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Method to update all products that belong to the old category name to the new one
                if (dataSnapshot.exists()) {
                    for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                        productSnapshot.getRef().child("product_categories").setValue(newCategoryName);
                    }
                    aLodingDialog.dismiss();
                    Toast.makeText(UpdateCategories.this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous activity
                } else {
                    aLodingDialog.dismiss();
                    Toast.makeText(UpdateCategories.this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous activity
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                aLodingDialog.dismiss();
                Toast.makeText(UpdateCategories.this, "Failed to update products", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
