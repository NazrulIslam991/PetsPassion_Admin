package com.example.petspassion_admin;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    // List of products and the context
    private List<DataClass> productList;
    private Context context;


    // ....................................Constructor to initialize the product list and context....................................................
    public AdminProductAdapter(List<DataClass> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }



    // ...................................Inflates the item layout and creates the ViewHolder.....................................................
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_admin, parent, false);
        return new ProductViewHolder(view);
    }



    //.................................... Binds the product data to the UI elements in the ViewHolder............................................
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        DataClass product = productList.get(position); // Get product at the current position

        holder.productName.setText(product.getProduct_name());  // Set product name

        String originalPriceText = product.getProduct_price() + " tk";   // Set product price
        holder.productPrice.setText(originalPriceText);

        // Check if there's a discount, calculate discounted price if applicable
        if (product.getProduct_discount() != null && !product.getProduct_discount().isEmpty()) {
            double originalPrice = Double.parseDouble(product.getProduct_price());
            double discount = Double.parseDouble(product.getProduct_discount());
            double discountedPrice = originalPrice - (originalPrice * discount / 100);
            String discountedPriceText = String.format("%.2f tk", discountedPrice);
            holder.productDiscountPrice.setText(discountedPriceText);  // Set discounted price
        } else {
            holder.productDiscountPrice.setText(originalPriceText); // No discount, show original price
        }

        holder.productDiscount.setText(product.getProduct_discount() + "%"); // Set discount percentage

        holder.productQuantity.setText(product.getProduct_quantity());  // Set product quantity

        holder.productDescription.setText(product.getProduct_description()); // Set product description

        // Load the product image using Picasso
        Picasso.get()
                .load(product.getProduct_image())
                .into(holder.productImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Image loaded successfully
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.productImage.setImageResource(R.drawable.initial_image);
                    }
                });

        holder.menuMore.setOnClickListener(v -> showPopupMenu(v, position));   // Set up the menu button for more actions (update/delete)
    }




    //..................................... Returns the number of products in the list......................................................
    @Override
    public int getItemCount() {
        return productList.size();
    }



    // ..................................Method to show the popup menu for update or delete options.....................................................
    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());  // Inflate menu resource
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_update) {
                // If update option is selected, start UpdateProductActivity
                Intent updateIntent = new Intent(context, UpdateProductActivity.class);
                updateIntent.putExtra("productId", productList.get(position).getProduct_id());
                context.startActivity(updateIntent);
                return true;
            } else if (itemId == R.id.menu_delete) {
                // If delete option is selected, confirm before deleting
                confirmDeleteProduct(position);
                return true;
            }
            return false;
        });
        popup.show();  // Display the menu
    }




    //............................................... Method to show a confirmation dialog before deleting a product.........................................................
    private void confirmDeleteProduct(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Yes", (dialog, which) -> deleteProduct(position))
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }



    //..................................... Method to delete a product from Firebase database..........................................................
    private void deleteProduct(int position) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Products")
                .child(productList.get(position).getProduct_id());
        databaseRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }





    //.................................. ViewHolder class to hold references to the UI components in the item layout.......................................................
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productDiscount, productDiscountPrice, productQuantity, productDescription;
        ImageView productImage, menuMore;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productDiscount = itemView.findViewById(R.id.product_discount);
            productDiscountPrice = itemView.findViewById(R.id.product_D_price);
            productQuantity = itemView.findViewById(R.id.tvProductQuantity);
            productDescription = itemView.findViewById(R.id.product_description);
            productImage = itemView.findViewById(R.id.product_image);
            menuMore = itemView.findViewById(R.id.menu_more);

            productPrice.setPaintFlags(productPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);     // Strike-through text style for the original price
        }
    }
}

