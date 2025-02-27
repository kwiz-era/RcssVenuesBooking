package com.example.rcssvenues;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Users extends AppCompatActivity {
    public Users() {
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check user role
        if (!isUser()) {
            finish();
            startActivity(new Intent(this, MainActivity.class)); // Redirect to login/home
            return;
        }

        setContentView(R.layout.activity_users);

        // Initialize buttons
        Button userHome = findViewById(R.id.user_home);
        Button userBookVenue = findViewById(R.id.user_book_venue);
        Button userBookings = findViewById(R.id.users_bookings);

        // Set onClick listeners
        setButtonClickListener(userHome, UserHome.class, "Navigating to Home");
        setButtonClickListener(userBookVenue, UserBookVenue.class, "Navigating to Book Venue");
        setButtonClickListener(userBookings, UserBookings.class, "Viewing Booking Details");
    }

    private boolean isUser() {
        // Replace with actual user validation logic
        return getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getBoolean("isUser", false); // Change "isUser" key as per your logic
    }

    private void setButtonClickListener(Button button, Class<?> targetActivity, String message) {
        button.setOnClickListener(v -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, targetActivity));
        });
    }
}
