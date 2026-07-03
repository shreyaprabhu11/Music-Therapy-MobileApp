package com.project.musicapp.core.models;

public class Feedback {

    // --- Member Variables ---
    private int id;
    private String name;
    private String email;
    private String message;
    private String time;
    private String date;

    // --- Constructors ---

    public Feedback() {
        // Default constructor is empty
    }

    public Feedback(int id, String name, String email, String message, String time, String date) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.message = message;
        this.time = time;
        this.date = date;
    }


    // --- Getter and Setter Methods ---

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}


