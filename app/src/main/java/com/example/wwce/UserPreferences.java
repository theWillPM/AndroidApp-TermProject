package com.example.wwce;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to save and load data locally
 */
public class UserPreferences {
    private static final String PREF_NAME = "UserPreferences";
    private static final String KEY_USER_LIST = "user_list";
    private static final String KEY_CURRENCY_MAP = "currency_map";

    private static SharedPreferences sharedPreferences;

    public UserPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveUserList(Set<User> userSet) {
        // Ensure sharedPreferences is not null before using it
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(userSet);
            editor.putString(KEY_USER_LIST, json);
            editor.apply();
            Log.d("Users", "Saved user list: "+User.userList.toString());
        }
    }

    public static Set<User> loadUserList() {
        Gson gson = new Gson();
        // Ensure sharedPreferences is not null before using it
        if (sharedPreferences != null) {
            String json = sharedPreferences.getString(KEY_USER_LIST, null);
            Log.d("Users", "Loaded user List. " + json);
            Type type = new TypeToken<HashSet<User>>() {
            }.getType();
            return gson.fromJson(json, type);
        }
        return new HashSet<User>();
    }

    public static void saveCurrencyMap(Map<String, Double> currencyMap) {
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(currencyMap);
            editor.putString(KEY_CURRENCY_MAP, json);
            editor.apply();
        }
    }

    public static Map<String, Double> loadCurrencyMap() {
        // Ensure sharedPreferences is not null before using it
        if (sharedPreferences != null) {
            Gson gson = new Gson();
            String json = sharedPreferences.getString(KEY_CURRENCY_MAP, null);
            if (json != null && !json.isEmpty()) {
                Type type = new TypeToken<HashMap<String, Double>>(){}.getType();
                return gson.fromJson(json, type);
            }
        }
        return new HashMap<>(); // Return an empty map if sharedPreferences is null or json is empty
    }

    // This is here for debugging purposes, Pedro. Don't call this or all local data will be lost!
    public static void clearSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // Static method to save username and password
    public static void saveCredentials(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }
    // Static method to load username and password
    public static Pair<String, String> loadCredentials() {
        // Check if the sharedPreferences contains the keys "username" and "password"
        if (sharedPreferences.contains("username") && sharedPreferences.contains("password")) {
            String username = sharedPreferences.getString("username", "");
            String password = sharedPreferences.getString("password", "");
            if (!username.isEmpty() && !password.isEmpty()) {
                return new Pair<>(username, password);
            }
        }
        return null; // Return null if either username or password is missing or empty
    }

    // Static method to save remember me status
    public static void saveRememberMeStatus(boolean rememberMe) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("remember_me", rememberMe);
        editor.apply();
    }

    // Static method to load remember me status
    public static boolean loadRememberMeStatus() {
        return sharedPreferences.getBoolean("remember_me", false);
    }
}


