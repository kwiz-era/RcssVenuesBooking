package com.example.rcssvenues;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserBookings extends AppCompatActivity {

    private ListView bookingListView;
    private Db_handler dbHandler; // SQLite database handler
    private FirebaseFirestore firestore; // Firestore instance
    private String userEmail = "user@example.com"; // Replace with dynamic user email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bookings);

        // Initialize ListView
        bookingListView = findViewById(R.id.bookingListView);

        // Initialize SQLite database handler
        dbHandler = new Db_handler(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Load bookings from SQLite and Firestore
        loadBookings();
    }

    /**
     * Loads bookings from SQLite and Firestore and displays them in the ListView.
     */
    private void loadBookings() {
        // Fetch bookings from SQLite
        List<String> sqliteBookings = dbHandler.getUserBookings(userEmail);

        // Fetch bookings from Firestore
        fetchBookingsFromFirestore(sqliteBookings);
    }

    /**
     * Fetches bookings from Firestore and combines them with SQLite bookings.
     *
     * @param sqliteBookings The list of bookings fetched from SQLite.
     */
    private void fetchBookingsFromFirestore(List<String> sqliteBookings) {
        firestore.collection("bookings")
                .whereEqualTo("userEmail", userEmail) // Filter by user email
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> firestoreBookings = new ArrayList<>();

                        // Extract booking data from Firestore
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String venueName = document.getString("venueName");
                            String bookingDateTime = document.getString("bookingDateTime");

                            // Format the booking details
                            String bookingDetails = venueName + " - " + bookingDateTime;
                            firestoreBookings.add(bookingDetails);
                        }

                        // Combine SQLite and Firestore bookings
                        List<String> allBookings = new ArrayList<>();
                        allBookings.addAll(sqliteBookings);
                        allBookings.addAll(firestoreBookings);

                        // Display the combined list in the ListView
                        displayBookings(allBookings);
                    } else {
                        Log.e("Firestore", "Error fetching bookings: " + task.getException().getMessage());
                        Toast.makeText(this, "Failed to fetch bookings from Firestore", Toast.LENGTH_SHORT).show();

                        // Display only SQLite bookings if Firestore fetch fails
                        displayBookings(sqliteBookings);
                    }
                });
    }

    /**
     * Displays the list of bookings in the ListView.
     *
     * @param bookings The list of bookings to display.
     */
    private void displayBookings(List<String> bookings) {
        if (bookings.isEmpty()) {
            Toast.makeText(this, "No bookings found.", Toast.LENGTH_SHORT).show();
        } else {
            // Set up an ArrayAdapter to display the bookings in the ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    bookings
            );
            bookingListView.setAdapter(adapter);

            // Handle clicks on ListView items
            bookingListView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedBooking = bookings.get(position);
                Toast.makeText(UserBookings.this, "Selected: " + selectedBooking, Toast.LENGTH_SHORT).show();
                // Additional logic to view or manage the booking can be added here
            });
        }
    }
}