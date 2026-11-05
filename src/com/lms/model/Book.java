package com.lms.model;

/**
 * Model class representing a Book entity in the library.
 * Encapsulates book attributes: ID, title, author, category, and available quantity.
 */
public class Book {
    private int id;
    private String title;
    private String author;
    private String category;
    private int quantity;

    // Default constructor
    public Book() {
    }

    // Parameterized constructor
    public Book(int id, String title, String author, String category, int quantity) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Book [ID=" + id + ", Title=" + title + ", Author=" + author + 
               ", Category=" + category + ", Quantity=" + quantity + "]";
    }
}

