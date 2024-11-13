package com.example.petspassion_admin;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class Admin_Insert_Product extends AppCompatActivity {
    EditText etProductName, etProductDescription, etProductPrice, etProductQuantity, etProductCategory, etProductDiscount;
    ImageView imgProduct, back;
    Button btnAddProduct;
    List<String> categories = new ArrayList<>();   // List to store categories
    int selectedCategoryIndex = -1;
    String imageURL;
    Uri uri;
    private ALodingDialog aLodingDialog;
    DatabaseReference categoryRef;  //  Reference to Firebase Categories

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_insert_product);

        getWindow().setStatusBarColor(ContextCompat.getColor(Admin_Insert_Product.this, R.color.adminHome));   // Set the status bar color



        //................................ Initialize the UI elements........................................
        etProductName = findViewById(R.id.etProductName);
        etProductDescription = findViewById(R.id.etProductDescription);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductDiscount = findViewById(R.id.etProductDiscount);
        etProductQuantity = findViewById(R.id.etProductQuantity);
        etProductCategory = findViewById(R.id.categories);
        imgProduct = findViewById(R.id.imgProduct);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        back = findViewById(R.id.insert_admin_home_page);




        // Initialize Cartegories Firebase references
        categoryRef = FirebaseDatabase.getInstance().getReference("Categories");



        // Fetch categories from Firebase
        fetchCategories();



        // ................................................Set up back button to finish the activity...........................................................
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // ............................category input field to show a category selection dialog..............................................................
        etProductCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog();
            }
        });



        //.................................. image picker to select an image from the gallery..........................................................
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            imgProduct.setImageURI(uri);
                        } else {
                            Toast.makeText(Admin_Insert_Product.this, "No image selected !!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );



        // .............................image click to open the gallery for image selection............................................
        imgProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });


        // ...............................Add product button click listener...............................................
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    saveData();
                }
            }
        });

        aLodingDialog = new ALodingDialog(Admin_Insert_Product.this);
    }



    // ...................................Validate input fields before adding the product......................................................
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate product name
        if (etProductName.getText().toString().trim().isEmpty()) {
            etProductName.setError("Product name is required");
            etProductName.requestFocus();
            isValid = false;
        }
        // Validate product description
        else if (etProductDescription.getText().toString().trim().isEmpty()) {
            etProductDescription.setError("Product description is required");
            etProductDescription.requestFocus();
            isValid = false;
        }
        // Validate product price
        else if (etProductPrice.getText().toString().trim().isEmpty()) {
            etProductPrice.setError("Product price is required");
            etProductPrice.requestFocus();
            isValid = false;
        }
        // Validate product discount
        else if (etProductDiscount.getText().toString().trim().isEmpty()) {
            etProductDiscount.setError("Product discount is required");
            etProductDiscount.requestFocus();
            isValid = false;
        }
        // Validate product quantity
        else if (etProductQuantity.getText().toString().trim().isEmpty()) {
            etProductQuantity.setError("Product quantity is required");
            etProductQuantity.requestFocus();
            isValid = false;
        }
        // Validate product category
        else if (etProductCategory.getText().toString().trim().isEmpty()) {
            etProductCategory.setError("Product category is required");
            etProductCategory.requestFocus();
            isValid = false;
        }
        // Validate image selection
        else if (uri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }



    // ..........................................Fetch categories from Firebase database.....................................................
    private void fetchCategories() {
        etProductCategory.setEnabled(false);

        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataClass category = dataSnapshot.getValue(DataClass.class);
                    if (category != null) {
                        categories.add(category.getCategories_name());
                    }
                }

                etProductCategory.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin_Insert_Product.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                etProductCategory.setEnabled(true);
            }
        });
    }


    // ................................................Show the category selection dialog........................................................
    private void showCategoryDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(Admin_Insert_Product.this);
            builder.setTitle("Select Category");

            String[] categoryArray = categories.toArray(new String[0]);

            builder.setSingleChoiceItems(categoryArray, selectedCategoryIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectedCategoryIndex = which;
                }
            });

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (selectedCategoryIndex != -1) {
                        etProductCategory.setText(categories.get(selectedCategoryIndex));
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        }
    }



    // .....................................Save the product data and upload the image to Firebase Storage.......................................................
    public void saveData() {
        // Check if the URI (image) is selected
        if (uri != null) {
            String fileExtension = getFileExtension(uri);

            // Check if the file extension was obtained
            if (fileExtension == null) {
                // If the file extension is null, show an error and exit
                Toast.makeText(this, "Failed to get file extension", Toast.LENGTH_SHORT).show();
            } else {
                // If the file extension is valid, proceed with the upload
                aLodingDialog.show();

                // Generate a unique product ID for Firebase
                DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products");
                String productId = productRef.push().getKey();

                // Create a reference to store the image in Firebase Storage
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Products")
                        .child(productId + "." + fileExtension);

                // Upload the image to Firebase Storage
                storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // On successful upload, get the download URL
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageURL = uri.toString();
                                uploadData(productId);  // Proceed to upload the product data
                                aLodingDialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the image upload fails, show an error message
                        Toast.makeText(Admin_Insert_Product.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        aLodingDialog.dismiss();
                    }
                });
            }
        } else {
            // If no image is selected, show a toast message
            Toast.makeText(Admin_Insert_Product.this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadData(String productId) {
        String name = etProductName.getText().toString();
        String description = etProductDescription.getText().toString();
        String price = etProductPrice.getText().toString();
        String discount = etProductDiscount.getText().toString();
        String quantity = etProductQuantity.getText().toString();
        String category = etProductCategory.getText().toString();

        DataClass dataClass = new DataClass(productId, name, category, description, price, discount, quantity, imageURL);

        // Save data under product ID in Firebase
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products");
        productRef.child(productId).setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Admin_Insert_Product.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(Admin_Insert_Product.this, "Failed to add product", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // .............................................Get the file extension of the selected image URI.............................................
    private String getFileExtension(Uri uri) {
        if (uri != null) {
            return getContentResolver().getType(uri).split("/")[1];
        }
        return null;
    }

}