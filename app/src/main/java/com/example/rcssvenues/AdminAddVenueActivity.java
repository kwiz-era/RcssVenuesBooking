package com.example.rcssvenues;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminAddVenueActivity extends AppCompatActivity {

    private EditText editTextVenueName, editTextLocation, editTextCapacity;
    private Db_handler dbHandler;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_venue);

        // Initialize views
        editTextVenueName = findViewById(R.id.editTextAdminVenueName);
        editTextLocation = findViewById(R.id.editTextAdminLocation);
        editTextCapacity = findViewById(R.id.editTextAdminCapacity);
        Button buttonAddVenue = findViewById(R.id.buttonAdminAddVenue);

        // Initialize SQLite database handler
        dbHandler = new Db_handler(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Handle add venue button click
        buttonAddVenue.setOnClickListener(v -> addVenue());
    }

    private void addVenue() {
        // Get input values
        String name = editTextVenueName.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || location.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = Integer.parseInt(capacityStr);

        // Add venue to SQLite
        dbHandler.addVenue(name, location, capacity);
        Toast.makeText(this, "Venue added to SQLite successfully!", Toast.LENGTH_SHORT).show();

        // Add venue to Firestore
        addVenueToFirestore(name, location, capacity);
    }

    private void addVenueToFirestore(String name, String location, int capacity) {
        // Create a new venue object
        Map<String, Object> venue = new HashMap<>();
        venue.put("name", name);
        venue.put("location", location);
        venue.put("capacity", capacity);

        // Add venue to Firestore
        firestore.collection("venues")
                .add(venue)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Venue added to Firestore successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Venue added with ID: " + documentReference.getId());
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add venue to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding venue", e);
                });
    }
}