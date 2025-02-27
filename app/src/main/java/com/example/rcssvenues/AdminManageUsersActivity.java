package com.example.rcssvenues;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminManageUsersActivity extends AppCompatActivity {

    // UI components
    private EditText editTextName, editTextEmail, editTextPassword, editTextRole;
    private Button buttonRegister;

    // Database handler and Firebase helper
    private Db_handler dbHandler; // SQLite database handler
    private FirebaseAuthHelper authHelper; // Firebase Authentication helper
    private FirebaseFirestore firestore; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        // Initialize views
        editTextName = findViewById(R.id.editTextText_name);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress_registration);
        editTextPassword = findViewById(R.id.editTextTextPassword_registration);
        editTextRole = findViewById(R.id.editTextText_Role);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Initialize database handler and Firebase helper
        dbHandler = new Db_handler(this);
        authHelper = new FirebaseAuthHelper();

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Set click listener for the register button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String role = editTextRole.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email domain
        if (!email.endsWith("@rajagiri.edu")) {
            Toast.makeText(this, "Only Rajagiri emails are allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the email is already registered in SQLite
        if (dbHandler.isEmailRegistered(email)) {
            Toast.makeText(this, "Email already registered in SQLite", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register the user with Firebase Authentication
        authHelper.registerUser(email, password, new FirebaseAuthHelper.OnAuthCompleteListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Save user details to SQLite
                dbHandler.addNewUser(name, email, password, role);

                // Save user details to Firestore
                saveUserToFirestore(name, email, role);

                // Show success message
                Toast.makeText(AdminManageUsersActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                // Clear input fields
                editTextName.setText("");
                editTextEmail.setText("");
                editTextPassword.setText("");
                editTextRole.setText("");
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(AdminManageUsersActivity.this, "Registration failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves user details to Firestore.
     *
     * @param name  The name of the user.
     * @param email The email of the user.
     * @param role  The role of the user.
     */
    private void saveUserToFirestore(String name, String email, String role) {
        // Create a new user object
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);

        // Add user to Firestore
        firestore.collection("users").document(email)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User added to Firestore successfully: " + email);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding user to Firestore", e);
                });
    }
}