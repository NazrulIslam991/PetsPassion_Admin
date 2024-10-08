package com.example.petspassion_admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Insert_Fragment extends Fragment {
    private CardView insertProductCard, insertCategoriesCard, showCategoriesCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=  inflater.inflate(R.layout.fragment_insert_, container, false);

        // Reference the CardViews
        insertProductCard = view.findViewById(R.id.insert_product_card);
        insertCategoriesCard = view.findViewById(R.id.insert_categories_card);
        showCategoriesCard = view.findViewById(R.id.show_categories_card);


        insertProductCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Admin_Insert_Product.class);
                startActivity(intent);
            }
        });



        insertCategoriesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Admin_Insert_Categories.class);
                startActivity(intent);
            }
        });



        showCategoriesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Admin_ShowCategories.class);
                startActivity(intent);
            }
        });
        return view;
    }
}