package com.example.rcssvenues;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminEditVenueActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText editTextVenueID, editTextVenueNameEdit, editTextLocationEdit, editTextCapacityEdit;
    private Db_handler dbHandler; // SQLite database handler
    private FirebaseFirestore firestore; // Firestore instance

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_venue); // Set the layout for this activity

        // Initialize UI elements
        editTextVenueID = findViewById(R.id.editTextVenueID);
        editTextVenueNameEdit = findViewById(R.id.editTextVenueNameEdit);
        editTextLocationEdit = findViewById(R.id.editTextLocationEdit);
        editTextCapacityEdit = findViewById(R.id.editTextCapacityEdit);
        Button buttonUpdateVenue = findViewById(R.id.buttonUpdateVenue);

        // Initialize SQLite database handler
        dbHandler = new Db_handler(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Set click listener for the Update Venue button
        buttonUpdateVenue.setOnClickListener(v -> {
            // Get input values
            String venueIDStr = editTextVenueID.getText().toString();
            String newVenueName = editTextVenueNameEdit.getText().toString();
            String newLocation = editTextLocationEdit.getText().toString();
            String newCapacityStr = editTextCapacityEdit.getText().toString();

            // Validate input
            if (venueIDStr.isEmpty() || newVenueName.isEmpty() || newLocation.isEmpty() || newCapacityStr.isEmpty()) {
                Toast.makeText(AdminEditVenueActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert venue ID and capacity to integers
            int venueID = Integer.parseInt(venueIDStr);
            int newCapacity = Integer.parseInt(newCapacityStr);

            // Update the venue in SQLite
            boolean isUpdatedInSQLite = dbHandler.updateVenue(venueID, newVenueName, newLocation, newCapacity);

            if (isUpdatedInSQLite) {
                Toast.makeText(AdminEditVenueActivity.this, "Venue updated in SQLite successfully!", Toast.LENGTH_SHORT).show();
                clearInputFields(); // Clear input fields

                // Update the venue in Firestore
                updateVenueInFirestore(venueID, newVenueName, newLocation, newCapacity);
            } else {
                Toast.makeText(AdminEditVenueActivity.this, "Failed to update venue in SQLite. Please check the Venue ID.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates a venue in Firestore using its ID.
     *
     * @param venueID      The ID of the venue to update.
     * @param newVenueName The new name of the venue.
     * @param newLocation  The new location of the venue.
     * @param newCapacity  The new capacity of the venue.
     */
    private void updateVenueInFirestore(int venueID, String newVenueName, String newLocation, int newCapacity) {
        // Convert the venue ID to a string (Firestore document IDs are strings)
        String venueIDStr = String.valueOf(venueID);

        // Create a map with the updated venue data
        Map<String, Object> updatedVenue = new HashMap<>();
        updatedVenue.put("name", newVenueName);
        updatedVenue.put("location", newLocation);
        updatedVenue.put("capacity", newCapacity);

        // Update the venue in Firestore
        firestore.collection("venues").document(venueIDStr)
                .update(updatedVenue)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminEditVenueActivity.this, "Venue updated in Firestore successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Venue updated with ID: " + venueIDStr);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminEditVenueActivity.this, "Failed to update venue in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error updating venue", e);
                });
    }

    // Helper method to clear input fields
    private void clearInputFields() {
        editTextVenueID.setText("");
        editTextVenueNameEdit.setText("");
        editTextLocationEdit.setText("");
        editTextCapacityEdit.setText("");
    }

    @NonNull
    @Override
    public final String toString() {
        return "AdminEditVenueActivity{" +
                "editTextVenueID=" + editTextVenueID +
                ", editTextVenueNameEdit=" + editTextVenueNameEdit +
                ", editTextLocationEdit=" + editTextLocationEdit +
                ", editTextCapacityEdit=" + editTextCapacityEdit +
                ", dbHandler=" + dbHandler +
                ", firestore=" + firestore +
                '}';
    }
}