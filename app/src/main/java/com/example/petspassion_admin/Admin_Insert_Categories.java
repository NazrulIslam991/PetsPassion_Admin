package com.example.petspassion_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.UUID;
import java.util.regex.Pattern;


public class Admin_Insert_Categories extends AppCompatActivity {
    private ImageView categoryImageView, back;
    private EditText categoryNameEditText;
    private Button addCategoryButton;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private static final Pattern Name_pattern = Pattern.compile("^[A-Z].*$");     // Pattern for validating category name

    private ALodingDialog aLodingDialog;     // Loading dialog instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_insert_categories);
        getWindow().setStatusBarColor(ContextCompat.getColor(Admin_Insert_Categories.this,R.color.adminHome));     // Set the status bar color


        categoryImageView = findViewById(R.id.categories_image);
        categoryNameEditText = findViewById(R.id.etCategoriesName);
        addCategoryButton = findViewById(R.id.btnAddCategories);

        back = findViewById(R.id.categories_insert_to_admin_home_page);

        // Initialize Firebase references
        storageReference = FirebaseStorage.getInstance().getReference("Categories");
        databaseReference = FirebaseDatabase.getInstance().getReference("Categories");

        // Initialize loading dialog
        aLodingDialog = new ALodingDialog(Admin_Insert_Categories.this);



        //............................................. Back button click listener to finish activity........................................................
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        //..................................................... Listener to allow the user to select an image.....................................................
        categoryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectAnImage();
            }
        });


        // .......................................Listener to validate and upload category on button click.....................................................
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndUploadCategory();
            }
        });

    }



    //.................................. Method to open image selection from the device...........................................................
    private void SelectAnImage() {
        Intent intent = new Intent(); // Create a new intent
        intent.setType("image/*"); // Set type to select image
        intent.setAction(Intent.ACTION_GET_CONTENT); // Action to get content from the user
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1); // Launch intent for result
    }


    // ........................................Handling the result after image selection................................................
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Store the selected image URI
            categoryImageView.setImageURI(imageUri); // Display the selected image
        }
    }



    //................................................ Method to validate category input and proceed to upload..................................................
    private void validateAndUploadCategory(){

        final String categorie_name = categoryNameEditText.getText().toString().trim();

        if(categorie_name.isEmpty()){
            categoryNameEditText.requestFocus();
            categoryNameEditText.setError("Categories name cannot be empty");
            return;
        } else if (!Name_pattern.matcher(categorie_name).matches()) {
            categoryNameEditText.requestFocus();
            categoryNameEditText.setError("Category name must start with a capital letter. Example: 'Pets'");
            return;
        } else if (imageUri==null) {
            categoryImageView.requestFocus();
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            checkCategoryExists(categorie_name);  // Check if category already exists
        }
    }



    //..................................... Method to check if the category already exists in the database...................................................
    private void checkCategoryExists(final String categorie_name){
        databaseReference.orderByChild("categories_name").equalTo(categorie_name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(Admin_Insert_Categories.this,"Categories name already exists !!!",Toast.LENGTH_SHORT).show();
                }
                else {
                    uploadCategory(categorie_name);  // Proceed to upload category if it doesn't exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin_Insert_Categories.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    // .............................................Method to upload the category data and image...........................................................
    private void uploadCategory(final String categorie_name) {

        aLodingDialog.show();
        new Handler().postDelayed(() -> aLodingDialog.dismiss(), 20000);

        // Generate a unique name for the image file and upload to Firebase Storage
        final StorageReference fileRef = storageReference.child(UUID.randomUUID().toString() + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        aLodingDialog.dismiss();
                        String Image_uri = uri.toString(); // Get the image URL
                        DataClass dataClass = new DataClass(categorie_name, Image_uri);

                        // Push category data to Firebase Realtime Database
                        String uploadId = databaseReference.push().getKey();
                        databaseReference.child(uploadId).setValue(dataClass);
                        Toast.makeText(Admin_Insert_Categories.this, "Category added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                aLodingDialog.dismiss();
                Toast.makeText(Admin_Insert_Categories.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }
}