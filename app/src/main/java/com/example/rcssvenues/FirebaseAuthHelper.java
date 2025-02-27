package com.example.rcssvenues;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthHelper {

    private static final String TAG = "FirebaseAuthHelper";

    // Firebase Authentication instance
    private final FirebaseAuth mAuth;

    // Constructor
    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Registers a new user with email and password using Firebase Authentication.
     *
     * @param email                  The user's email address.
     * @param password               The user's password.
     * @param onAuthCompleteListener Callback to handle success or failure.
     */
    public void registerUser(String email, String password, OnAuthCompleteListener onAuthCompleteListener) {
        // Validate email and password
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            onAuthCompleteListener.onFailure(new IllegalArgumentException("Email and password cannot be empty"));
            return;
        }

        // Validate email domain
        if (!email.endsWith("@rajagiri.edu")) {
            onAuthCompleteListener.onFailure(new IllegalArgumentException("Only Rajagiri emails are allowed"));
            return;
        }

        // Create a new user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "User registered: " + user.getUid());
                                onAuthCompleteListener.onSuccess(user);
                            }
                        } else {
                            // Registration failed
                            Log.e(TAG, "Registration failed: " + task.getException());
                            onAuthCompleteListener.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Logs in an existing user with email and password using Firebase Authentication.
     *
     * @param email                  The user's email address.
     * @param password               The user's password.
     * @param onAuthCompleteListener Callback to handle success or failure.
     */
    public void loginUser(String email, String password, OnAuthCompleteListener onAuthCompleteListener) {
        // Validate email and password
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            onAuthCompleteListener.onFailure(new IllegalArgumentException("Email and password cannot be empty"));
            return;
        }

        // Log in an existing user with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "User logged in: " + user.getUid());
                                onAuthCompleteListener.onSuccess(user);
                            }
                        } else {
                            // Login failed
                            Log.e(TAG, "Login failed: " + task.getException());
                            onAuthCompleteListener.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return The currently logged-in user, or null if no user is logged in.
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Logs out the current user.
     */
    public void logoutUser() {
        mAuth.signOut();
        Log.d(TAG, "User logged out");
    }

    /**
     * Interface for authentication callbacks.
     */
    public interface OnAuthCompleteListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception exception);
    }
}