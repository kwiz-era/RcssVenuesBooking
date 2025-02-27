package com.example.rcssvenues;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDeleteVenueActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText editTextVenueIDDelete;
    private Db_handler dbHandler; // SQLite database handler
    private FirebaseFirestore firestore; // Firestore instance

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_delete_venue); // Set the layout for this activity

        // Initialize UI elements
        editTextVenueIDDelete = findViewById(R.id.editTextVenueIDDelete);
        Button buttonDeleteVenue = findViewById(R.id.buttonDeleteVenue);

        // Initialize SQLite database handler
        dbHandler = new Db_handler(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Set click listener for the Delete Venue button
        buttonDeleteVenue.setOnClickListener(v -> {
            // Get the venue ID from the EditText
            String venueIDStr = editTextVenueIDDelete.getText().toString();

            // Validate input
            if (venueIDStr.isEmpty()) {
                Toast.makeText(AdminDeleteVenueActivity.this, "Please enter a Venue ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert the venue ID to an integer
            int venueID = Integer.parseInt(venueIDStr);

            // Delete the venue from SQLite
            boolean isDeletedFromSQLite = dbHandler.deleteVenue(venueID);

            if (isDeletedFromSQLite) {
                Toast.makeText(AdminDeleteVenueActivity.this, "Venue deleted from SQLite successfully!", Toast.LENGTH_SHORT).show();
                editTextVenueIDDelete.setText(""); // Clear the input field

                // Delete the venue from Firestore
                deleteVenueFromFirestore(venueID);
            } else {
                Toast.makeText(AdminDeleteVenueActivity.this, "Failed to delete venue from SQLite. Please check the Venue ID.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a venue from Firestore using its ID.
     *
     * @param venueID The ID of the venue to delete.
     */
    private void deleteVenueFromFirestore(int venueID) {
        // Convert the venue ID to a string (Firestore document IDs are strings)
        String venueIDStr = String.valueOf(venueID);

        // Delete the venue from Firestore
        firestore.collection("venues").document(venueIDStr)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminDeleteVenueActivity.this, "Venue deleted from Firestore successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Venue deleted with ID: " + venueIDStr);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminDeleteVenueActivity.this, "Failed to delete venue from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error deleting venue", e);
                });
    }

    @NonNull
    @Override
    public final String toString() {
        return "AdminDeleteVenueActivity{" +
                "editTextVenueIDDelete=" + editTextVenueIDDelete +
                ", dbHandler=" + dbHandler +
                ", firestore=" + firestore +
                '}';
    }
}