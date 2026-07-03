package com.project.musicapp.core.models;

import java.io.Serializable;

public class User implements Serializable {
    public enum Role {
        CONSULTANT,
        PATIENT,
        ADMIN
    }

    int id;

    private Role role;
    String name, email, password, phone, profilePictureUrl;
    boolean isFirstLogin;

    public User() {
    }

    public User(int id, String name, String email, String password, Role role, String phone, String profilePictureUrl, boolean isFirstLogin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.profilePictureUrl = profilePictureUrl;
        this.isFirstLogin =   isFirstLogin;

    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    public boolean isFirstLogin() {
        return isFirstLogin;
    }
    public void setFirstLogin(boolean firstLogin) {
        isFirstLogin = firstLogin;
    }

}
