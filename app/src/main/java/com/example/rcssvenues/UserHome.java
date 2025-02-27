package com.example.rcssvenues;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserHome extends AppCompatActivity {

    private ListView venueListView;
    private Button btnViewBookings;
    private TextView tvWelcome;
    private ArrayAdapter<String> adapter;
    private Db_handler db; // SQLite Database Helper
    private FirebaseFirestore firestore; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Initialize UI components
        tvWelcome = findViewById(R.id.tvWelcome);
        venueListView = findViewById(R.id.venueListView);
        btnViewBookings = findViewById(R.id.btnViewBookings);

        // Set welcome text dynamically (if user details are available)
        tvWelcome.setText("Welcome, User"); // Replace with actual username if available

        // Initialize SQLite database helper
        db = new Db_handler(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Load venues from SQLite and Firestore
        loadVenues();

        // Handle ListView item clicks
        venueListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedVenue = adapter.getItem(position);
            Toast.makeText(UserHome.this, "Selected: " + selectedVenue, Toast.LENGTH_SHORT).show();

            // Navigate to a venue details screen
            Intent venueDetailsIntent = new Intent(UserHome.this, UserBookings.class);
            venueDetailsIntent.putExtra("VenueName", selectedVenue);
            startActivity(venueDetailsIntent);
        });

        // Handle button click to view bookings
        btnViewBookings.setOnClickListener(v -> {
            Intent myBookingsIntent = new Intent(UserHome.this, UserBookings.class);
            startActivity(myBookingsIntent);
        });
    }

    /**
     * Loads venues from SQLite and Firestore databases and updates the ListView.
     */
    private void loadVenues() {
        // Fetch venues from SQLite
        List<String> sqliteVenues = db.getAllVenues();

        // Fetch venues from Firestore
        fetchVenuesFromFirestore(sqliteVenues);
    }

    /**
     * Fetches venues from Firestore and combines them with SQLite venues.
     *
     * @param sqliteVenues The list of venues fetched from SQLite.
     */
    private void fetchVenuesFromFirestore(List<String> sqliteVenues) {
        firestore.collection("venues")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> firestoreVenues = new ArrayList<>();

                        // Extract venue data from Firestore
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String location = document.getString("location");
                            int capacity = document.getLong("capacity").intValue();

                            // Format the venue details
                            String venueDetails = name + " - " + location + " (Capacity: " + capacity + ")";
                            firestoreVenues.add(venueDetails);
                        }

                        // Combine SQLite and Firestore venues
                        List<String> allVenues = new ArrayList<>();
                        allVenues.addAll(sqliteVenues);
                        allVenues.addAll(firestoreVenues);

                        // Display the combined list in the ListView
                        displayVenues(allVenues);
                    } else {
                        Log.e("Firestore", "Error fetching venues: " + task.getException().getMessage());
                        Toast.makeText(this, "Failed to fetch venues from Firestore", Toast.LENGTH_SHORT).show();

                        // Display only SQLite venues if Firestore fetch fails
                        displayVenues(sqliteVenues);
                    }
                });
    }

    /**
     * Displays the list of venues in the ListView.
     *
     * @param venues The list of venues to display.
     */
    private void displayVenues(List<String> venues) {
        if (venues.isEmpty()) {
            Toast.makeText(this, "No venues available.", Toast.LENGTH_SHORT).show();
        } else {
            // Set adapter for the ListView
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, venues);
            venueListView.setAdapter(adapter);
        }
    }
}