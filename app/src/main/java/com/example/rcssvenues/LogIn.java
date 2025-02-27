package com.example.rcssvenues;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kotlin.io.encoding.Base64;

public class LogIn extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogIn;
    private FirebaseAuth mAuth;
    private Db_handler dbHandler; // SQLite database handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SQLite database handler
        dbHandler = new Db_handler(this);

        // Initialize views
        editTextEmail = findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        buttonLogIn = findViewById(R.id.buttonLogIn);

        // Set click listener for the login button
        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        // Get input values
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the email is from the "rajagiri.edu" domain
        if (!email.endsWith("@rajagiri.edu")) {
            Toast.makeText(this, "Only rajagiri.edu emails are allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Firebase login successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d("Login", "Firebase login successful: " + user.getEmail());

                                // Fetch the user's role from SQLite
                                String role = dbHandler.getUserRoleByEmail(email);
                                if (role != null) {
                                    // Navigate to the appropriate activity based on the role
                                    navigateToRoleActivity(role);
                                } else {
                                    // Role not found in SQLite
                                    Toast.makeText(LogIn.this, "User role not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            // Firebase login failed
                            Log.e("Login", "Firebase authentication failed: " + task.getException().getMessage());
                            Toast.makeText(LogIn.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToRoleActivity(String role) {
        Intent intent;
        switch (role.toLowerCase()) {
            case "admin":
                intent = new Intent(LogIn.this, Admin.class);
                break;
            case "receptionist":
                intent = new Intent(LogIn.this, Receptionist.class);
                break;
            default:
                intent = new Intent(LogIn.this, Base64.Default.class);
                break;
        }
        startActivity(intent);
        finish(); // Close the login activity
    }
}