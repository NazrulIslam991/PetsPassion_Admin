package com.example.petspassion_admin;

public class DataClass {
    private String category_name;
    private String category_image;
    private String product_name;
    private String product_categories;
    private String product_description;
    private String product_price;
    private String product_discount;
    private String product_quantity;
    private String product_image;
    private String product_id;

    public DataClass() {
    }

    public DataClass(String product_id, String product_name, String product_categories, String product_description, String product_price, String product_discount, String product_quantity, String product_image) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.product_categories = product_categories;
        this.product_description = product_description;
        this.product_price = product_price;
        this.product_discount = product_discount;
        this.product_quantity = product_quantity;
        this.product_image = product_image;
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

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_categories() {
        return product_categories;
    }

    public void setProduct_categories(String product_categories) {
        this.product_categories = product_categories;
    }

    public String getProduct_description() {
        return product_description;
    }

    public void setProduct_description(String product_description) {
        this.product_description = product_description;
    }

    public String getProduct_price() {
        return product_price;
    }

    public void setProduct_price(String product_price) {
        this.product_price = product_price;
    }

    public String getProduct_discount() {
        return product_discount;
    }

    public void setProduct_discount(String product_discount) {
        this.product_discount = product_discount;
    }

    public String getProduct_quantity() {
        return product_quantity;
    }

    public void setProduct_quantity(String product_quantity) {
        this.product_quantity = product_quantity;
    }

    public String getProduct_image() {
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }
}
