package com.lms.model;

/**
 * Model class representing a Library Member entity.
 * Encapsulates member details: ID, name, email, and phone number.
 */
public class Member {
    private int id;
    private String name;
    private String email;
    private String phone;

    // Default constructor
    public Member() {
    }

    // Parameterized constructor
    public Member(int id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Member [ID=" + id + ", Name=" + name + ", Email=" + email + ", Phone=" + phone + "]";
    }
}

