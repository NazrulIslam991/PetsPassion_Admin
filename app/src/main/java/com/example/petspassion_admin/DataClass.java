package com.example.petspassion_admin;

public class DataClass {
    private String category_name;
    private String category_image;

    public DataClass() {
    }


    // Constructor for categories
    public DataClass(String category_name, String category_image) {
        this.category_name = category_name;
        this.category_image = category_image;
    }

    public String getCategory_name() {
        return category_name;
    }

    public String getCategory_image() {
        return category_image;
    }
}
