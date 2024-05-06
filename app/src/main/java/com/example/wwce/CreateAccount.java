package com.example.wwce;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class CreateAccount extends AppCompatActivity {

    Button btn_create_account;
    Button btn_cancel;
    TextInputEditText input_username;
    TextInputEditText input_repeat_password;
    TextInputEditText input_password;
    TextInputEditText input_email;
    String username;
    String password;
    String repeat_password;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // get our views
        btn_create_account = findViewById(R.id.btn_createAccount);
        input_email = findViewById(R.id.input_email);
        input_username = findViewById(R.id.input_username);
        input_password = findViewById(R.id.input_password);
        input_repeat_password = findViewById(R.id.input_password2);
        btn_cancel = findViewById(R.id.btn_cancel);

        // set button listeners
        btn_create_account.setOnClickListener(this::parseInput);
        btn_cancel.setOnClickListener(v -> backToMain(v, false));

        // Add listener to password fields
        input_password.addTextChangedListener(passwordTextWatcher);
        input_repeat_password.addTextChangedListener(passwordTextWatcher);
    }

    /**
     * support function to check if email matches regex
     * @param email the email
     * @param regex the regular expression
     * @return
     */
    public boolean isValidEmail(String email, String regex) {
        return Pattern.compile(regex).matcher(email).matches();
    }

    /**
     * support function to check if password has at least 6 characters
     * @param password the password
     * @return
     */
    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    /**
     *  support function to check if the passwords match on both fields
     * @return
     */
    private boolean doPasswordsMatch() {
        return Objects.equals(password, repeat_password);
    }

    /**
     * Function to validate all inputs, calling other support functions
     * @param v
     */
    private void parseInput(View v) {

        // parsing the input so we can ignore CaseSens.
        username = String.valueOf(input_username.getText()).trim().toLowerCase();
        password = String.valueOf(input_password.getText()).trim();
        repeat_password = String.valueOf(input_repeat_password.getText()).trim();
        email = String.valueOf(input_email.getText()).trim().toLowerCase();

        // Validate email input through this RegEx:
        String regex = "^(.+)@(\\S+)$";

        // Validate the email
        if (!isValidEmail(email, regex)) {
            // Show error message for invalid email
            input_email.setError("Invalid email address");
            return;
        }

        if (!isValidPassword(password)) {
            // Show error message for invalid password length
            input_password.setError("Password must be at least 6 characters long");
            return;
        }

        if (!doPasswordsMatch()) {
            // Show error message for passwords not matching
            input_repeat_password.setError("Passwords do not match");
            return;
        }

        // Check if userList is empty
        if (User.userList == null) {
            User.userList = new HashSet<>();
        }

        // Create a new user
        User newUser = new User(username, password, email);

        // Add the new user to userList
        User.userList.add(newUser);

        // Save updated userList locally
        UserPreferences.saveUserList(User.userList);

        // Navigate back to the main activity
        backToMain(v, true);
    }

    // listener to check if passwords match
    private TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String password = input_password.getText().toString().trim();
            String repeatPassword = input_repeat_password.getText().toString().trim();

            if (!password.equals(repeatPassword)) {
                // Show warning that passwords do not match
                input_repeat_password.setError("Passwords do not match");
            } else {
                // Clear any previous error
                input_repeat_password.setError(null);
            }
        }
    };

    /**
     * return to main screen and pass intent extras
     * @param v
     * @param success was acc creation successful?
     */
    public void backToMain(View v, boolean success) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("success", success);
        i.putExtra("loadUser", false);
        startActivity(i);
    }
}