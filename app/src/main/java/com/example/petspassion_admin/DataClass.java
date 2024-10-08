package com.example.petspassion_admin;

public class DataClass {
    private String categories_name;
    private String category_image;

    public DataClass() {
    }


    // Constructor for categories
    public DataClass(String category_name, String category_image) {
        this.categories_name = category_name;
        this.category_image = category_image;
    }

    public String getCategories_name() {
        return categories_name;
    }

    public String getCategory_image() {
        return category_image;
    }
}
