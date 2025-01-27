package com.example.petspassion_admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {


    // ............................Context and list to hold category data.........................................
    private Context context;
    private List<DataClass> categoryList;




    //..................................... Constructor for initializing the adapter with context and category list.........................................
    public CategoryAdapter(Context context, List<DataClass> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }





    //.................................. This method inflates the item layout for a category and returns a ViewHolder.................................................................
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }





    // ..........................................This method binds data to the ViewHolder for each category item.......................................................
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        DataClass category = categoryList.get(position);  // Get the current category from the list
        holder.categoriesName.setText(category.getCategory_name());  // Set category name to the TextView
        Picasso.get().load(category.getCategory_image()).into(holder.categoriesImage);   // Load category image using Picasso library

        // Set a click listener for the item, if the context is the Admin_ShowCategories activity
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof Admin_ShowCategories) {
                showPopupMenu(holder, category, v);   // Show a popup menu for actions (Update/Delete)
            }
        });
    }




    // ..............................................This method returns the total number of items in the category list..........................................................
    @Override
    public int getItemCount() {
        return categoryList.size();
    }





    //............................... This method displays a popup menu with options to update or delete a category.............................................
    private void showPopupMenu(CategoryViewHolder holder, DataClass category, View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.getMenuInflater().inflate(R.menu.category_context_menu, popupMenu.getMenu());   // Inflate the popup menu with menu items defined in the XML
        popupMenu.setOnMenuItemClickListener(menuItem -> handleMenuItemClick(menuItem.getItemId(), category));  // Set a click listener for the menu items
        popupMenu.show(); // Show the popup menu
    }






    //..................................... This method handles the menu item click actions for update and delete.............................................
    private boolean handleMenuItemClick(int itemId, DataClass category) {
        if (itemId == R.id.update_category) {
            startUpdateCategoryActivity(category);
            return true;
        } else if (itemId == R.id.delete_category) {
            deleteCategory(category.getCategory_name(), category.getCategory_image());
            return true;
        } else {
            return false;
        }
    }





    // .....................................This method starts the UpdateCategories activity with category details passed via an Intent..................................................
    private void startUpdateCategoryActivity(DataClass category) {
        Intent intent = new Intent(context, UpdateCategories.class);
        intent.putExtra("category_name", category.getCategory_name());
        intent.putExtra("category_image", category.getCategory_image());
        context.startActivity(intent);
    }





    // .................................This method deletes a category from Firebase based on the category name.............................................
    private void deleteCategory(String categoryName, String categoryImage) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Categories");   // Reference to the "Categories" node in Firebase
        databaseReference.orderByChild("category_name").equalTo(categoryName)        // Query to find the category by name
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {                   // If the category exists, delete it
                        task.getResult().getChildren().forEach(snapshot -> snapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {                     // Notify success and update the UI
                                    Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show();
                                    categoryList.removeIf(item -> item.getCategory_name().equals(categoryName));           // Remove the category from the list and refresh the RecyclerView
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete category", Toast.LENGTH_SHORT).show()));
                    } else {
                        Toast.makeText(context, "Category not found", Toast.LENGTH_SHORT).show();                          // Show a message if the category was not found
                    }
                });
    }





    // ....................................ViewHolder class to hold category views (name and image) for the RecyclerView..............................................................
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoriesImage;
        TextView categoriesName;

        // Constructor to initialize the view components
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoriesImage = itemView.findViewById(R.id.category_image);
            categoriesName = itemView.findViewById(R.id.category_name);
        }
    }
}
