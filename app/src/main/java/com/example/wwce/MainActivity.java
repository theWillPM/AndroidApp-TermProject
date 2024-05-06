package com.example.wwce;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Author: Willian Pereira Munhoz
 * Date: April 14th, 2024
 * Instructor: Pedro Henrique Magdaleno Ferreira
 * Android_Base: Android 14.0 x86_64, API 34, Pixel 3a API 34
 */
public class MainActivity extends AppCompatActivity {

    TextInputEditText input_username;
    TextInputEditText input_password;
    Button btn_connect;
    Button btn_createAccount;
    CheckBox remember;
    TextView forgotPassword;
    static boolean loadUser = true;
    static String username = "";
    static String password = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Check if we're returning from a successful 'create-account'
        Intent i = getIntent();
        boolean success = i.getBooleanExtra("success", false);
        if (success) Toast.makeText(this, "Created user", Toast.LENGTH_SHORT).show();

        // Even if this is not called, it is necessary to avoid NullPointerExceptions:
        UserPreferences userPreferences = new UserPreferences(this);
        // Deal with the "remember me" status:
        loadUser = UserPreferences.loadRememberMeStatus();
        loadUser = i.getBooleanExtra("loadUser", loadUser);

        // CALL THIS TO CLEAR ALL STORED DATA (uncomment) --
//         UserPreferences.clearSharedPreferences();

        super.onCreate(savedInstanceState);
        // Hide the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(R.layout.activity_main);

        // Load saved credentials and pre-fill username and password fields if they are not empty
        Pair<String, String> savedCredentials = UserPreferences.loadCredentials();

        forgotPassword = findViewById(R.id.txt_forgot_password);
        input_username = findViewById(R.id.input_username);
        input_password = findViewById(R.id.input_password);

        // Load usersArray from SharedPreferences
        User.userList = UserPreferences.loadUserList();

        remember = findViewById(R.id.checkBox_remember);
        btn_connect = findViewById(R.id.btn_connect);
        btn_createAccount = findViewById(R.id.btn_create_account);

        btn_connect.setOnClickListener(this::checkUserCredentials);
        btn_createAccount.setOnClickListener(this::CreateAccount);

        View rootLayout = findViewById(R.id.root_layout);

        // Add touch listener to root layout
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide keyboard when touch event occurs outside of the input field
                hideKeyboard(MainActivity.this);

                // Clear focus from the input field
                clearFocusFromInputField(v);

                // Request focus for the root layout to ensure that the input field loses focus
                rootLayout.requestFocus();

                return false;
            }
        });


        // Add a listener to our username field (I want to update my variable in real-time)
        input_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                username = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Add a listener to our input password field (I want to update my variable in real-time)
        input_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                password = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Function to handle "ENTER" keypress while entering password
        input_password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                // Call the btn_connect click listener
                hideKeyboard(this);
                btn_connect.performClick();
                return true;
            }
            return false;
        });

        // If 'remember me' then autofill and login:
        if (loadUser)
            if (savedCredentials != null) {
                String savedUsername = savedCredentials.first;
                String savedPassword = savedCredentials.second;

                // Pre-fill username and password fields if they are not empty
                if (!TextUtils.isEmpty(savedUsername) && !TextUtils.isEmpty(savedPassword)) {
                    input_username.setText(savedUsername);
                    input_password.setText(savedPassword);
                    username = savedUsername;
                    password = savedPassword;
                    checkUserCredentials(this.getCurrentFocus());
                }
            }


        // add listener to show password hint
        forgotPassword.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(username)) {
                // Ensure userList is not null
                if (User.userList != null) {
                    // Iterate over userList to find the matching username
                    for (User user : User.userList) {
                        if (user.getUsername().equalsIgnoreCase(username)) {
                            String password = user.getPassword();
                            // Create masked password
                            String maskedPassword = maskPassword(password);
                            // Display the masked password
                            Toast.makeText(MainActivity.this, "Password: " + maskedPassword, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } else {
                    // If userList is null
                    Toast.makeText(MainActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // If username is empty
                Toast.makeText(MainActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Check if the entered information is valid (if user has account and if password is correct)
     * @param view
     * @return
     */
    private boolean checkUserCredentials(View view) {
        Set<User> userList = User.userList;
        if (userList != null) {
            for (User user : userList) {
                String storedUsername = user.getUsername();
                String storedPassword = user.getPassword();
                Log.d("Users", "Expected :["+storedUsername+", "+storedPassword+"]");
                // Normalize and trim the input username
                String normalizedUsername = username.toLowerCase().trim();
                Log.d("Users", "Actual :["+username+", "+password+"]");

                if (normalizedUsername.equals(storedUsername) && password.equals(storedPassword)) {
                    // User authenticated
                    if (remember.isChecked()) {
                        UserPreferences.saveCredentials(normalizedUsername, password);
                        UserPreferences.saveRememberMeStatus(remember.isChecked());
                    }
                    Toast.makeText(this, "Authenticated", Toast.LENGTH_SHORT).show();
                    login(view);
                    return true;
                }
            }
        }

        // User not authenticated
        Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
        return false;
    }

    // Method to hide keyboard
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Method to clear focus from input field
    private void clearFocusFromInputField(View view) {
        if (view instanceof EditText) {
            view.clearFocus();
        }
    }
   // Login
    public void login(View v) {
        Intent i = new Intent(this, Intermission.class);
        i.putExtra("user", username);
        startActivity(i);
    }
    // Create Account
    public void CreateAccount(View v) {
        Intent i = new Intent(this, CreateAccount.class);
        startActivity(i);
    }
    // Method to mask password
    private String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        // Get the first and last character of the password
        char firstChar = password.charAt(0);
        char lastChar = password.charAt(password.length() - 1);
        // Mask all characters except the first and last
        return firstChar + password.substring(1, password.length() - 1).replaceAll(".", "*") + lastChar;
    }
}

