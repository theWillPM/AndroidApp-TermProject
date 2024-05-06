package com.example.wwce;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

/**
 * this class is the login animation between main activity and dashboard
 */
public class Intermission extends AppCompatActivity {
    String userName;
    TextView txt_userName;
    Intermission inter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermission);
        inter = this;
        Intent i = getIntent();
        userName = i.getStringExtra("user");

        txt_userName = findViewById(R.id.txt_user_name);
        txt_userName.setText(userName.toUpperCase());

        txt_userName.animate()
                .scaleY(1.5f).scaleX(1.5f)
                .setDuration(1000);
        new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            // when timer ends, reset.
            public void onFinish() {
                Intent i = new Intent(inter, Dashboard.class);
                i.putExtra("user", userName);
                startActivity(i);
            }
        }.start();
    }


}