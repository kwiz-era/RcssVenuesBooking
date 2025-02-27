package com.example.rcssvenues;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminViewVenuesActivity extends AppCompatActivity {

    private ListView listViewVenues;
    private Db_handler dbHandler; // SQLite database handler
    private FirebaseFirestore firestore; // Firestore instance

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_venues);

        // Initialize views
        listViewVenues = findViewById(R.id.listViewVenues);

        // Initialize SQLite database handler
        dbHandler = new Db_handler(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Load venues from SQLite and Firestore
        loadVenues();
    }

    private void loadVenues() {
        // Fetch venues from SQLite
        List<String> sqliteVenues = dbHandler.getAllVenues();

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
            Toast.makeText(this, "No venues found", Toast.LENGTH_SHORT).show();
        } else {
            // Create an ArrayAdapter to display the venues in the ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1, // Default layout for list items
                    venues
            );

            // Set the adapter to the ListView
            listViewVenues.setAdapter(adapter);
        }
    }

    @NonNull
    @Override
    public final String toString() {
        return "AdminViewVenuesActivity{" +
                "listViewVenues=" + listViewVenues +
                ", dbHandler=" + dbHandler +
                ", firestore=" + firestore +
                '}';
    }
}