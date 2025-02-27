package com.example.rcssvenues;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // Splash screen delay duration
    private static final int SPLASH_SCREEN_DURATION = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);


        // Initialize animations
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_anim);
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);

        // Bind UI components
        ImageView logoImageView = findViewById(R.id.logo_imageView);
        TextView frontPageTextView = findViewById(R.id.frontpage_textView);

        // Set animations to UI components
        logoImageView.setAnimation(topAnim);
        frontPageTextView.setAnimation(bottomAnim);

        // Delayed transition to the appropriate activity
        new Handler().postDelayed(() -> {
            // Check if the user is already logged in
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // User is logged in, fetch their role and redirect accordingly
                fetchUserRoleAndRedirect(currentUser.getEmail());
            } else {
                // No user is logged in, redirect to the login screen
                redirectToLogin();
            }
        }, SPLASH_SCREEN_DURATION);
    }

    /**
     * Fetches the user's role from Firestore or Realtime Database and redirects them to the appropriate activity.
     *
     * @param email The email of the logged-in user.
     */
    private void fetchUserRoleAndRedirect(String email) {
        // Fetch the user's role from Firestore or Realtime Database
        // For now, we'll assume the role is hardcoded based on the email
        String role = "default"; // Default role

        if (email.equals("admin@rajagiri.edu")) {
            role = "admin";
        } else if (email.equals("receptionist@rajagiri.edu")) {
            role = "receptionist";
        }

        redirectBasedOnRole(role);
    }

    /**
     * Redirects the user to the appropriate activity based on their role.
     *
     * @param role The role of the user (e.g., "admin", "receptionist", "user").
     */
    private void redirectBasedOnRole(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(MainActivity.this, Admin.class);
                break;
            case "receptionist":
                intent = new Intent(MainActivity.this, Receptionist.class);
                break;
            default:
                // If the role is unknown or user role
                intent = new Intent(MainActivity.this, Users.class);
                break;
        }
        startActivity(intent);
        finish(); // Close the splash screen
    }

    /**
     * Redirects the user to the login screen.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LogIn.class);
        startActivity(intent);
        finish(); // Close the splash screen
    }
}