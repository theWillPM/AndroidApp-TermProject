package com.example.wwce;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class User {
    public static Set<User> userList;
    private String username;
    private String password;
    private String email;
    Set<String> rates;

    public User(String username, String password, String email) {
        this.username = username.toLowerCase().trim();
        this.password = password;
        this.email = email.toLowerCase().trim();
        this.rates = new HashSet<>();

        AddUserToList();
    }

    private void AddUserToList() {
        userList.add(this);
    }

    public void setUserRates(Set<String> rates) {
        this.rates = rates;
    }
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {return email; }


    @NonNull
    @Override
    public String toString() {
        return this.username;
    }
}
