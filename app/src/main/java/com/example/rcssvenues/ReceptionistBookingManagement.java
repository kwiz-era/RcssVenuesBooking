package com.example.rcssvenues;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReceptionistBookingManagement extends AppCompatActivity {

    // Declare UI elements
    private EditText searchBooking;
    private RecyclerView bookingsList;
    private Button btnAddBooking;

    // Adapter for RecyclerView
    private BookingAdapter bookingAdapter;

    // Database handler and Firestore instance
    private Db_handler dbHandler;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptionist_booking_management);

        // Initialize UI elements
        searchBooking = findViewById(R.id.search_booking);
        bookingsList = findViewById(R.id.bookings_list);
        btnAddBooking = findViewById(R.id.btnAddBooking);

        // Initialize SQLite database handler
        dbHandler = new Db_handler(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        bookingsList.setLayoutManager(new LinearLayoutManager(this));

        // Fetch bookings from SQLite and Firestore
        fetchBookings();

        // Set click listener for the Add Booking button
        btnAddBooking.setOnClickListener(v -> {
            Intent intent = new Intent(ReceptionistBookingManagement.this, ReceptionistVenueManagement.class);
            startActivity(intent);
        });

        // Implement search functionality with a TextWatcher
        searchBooking.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBookings(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Fetches bookings from SQLite and Firestore and updates the RecyclerView.
     */
    private void fetchBookings() {
        List<Booking> sqliteBookings = dbHandler.getAllBookings();
        fetchBookingsFromFirestore(sqliteBookings);
    }

    /**
     * Fetches bookings from Firestore and combines them with SQLite bookings.
     *
     * @param sqliteBookings The list of bookings fetched from SQLite.
     */
    private void fetchBookingsFromFirestore(List<Booking> sqliteBookings) {
        firestore.collection("bookings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Booking> firestoreBookings = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String venueName = document.getString("venueName");
                            int venueId = document.getLong("venueID").intValue(); // Fix: Convert Firestore Long to int
                            String bookingDateTime = document.getString("bookingDateTime");
                            String status = document.getString("status");

                            // Create a Booking object
                            Booking booking = new Booking(id, venueName, venueId, bookingDateTime, status);
                            firestoreBookings.add(booking);
                        }

                        // Combine SQLite and Firestore bookings
                        List<Booking> allBookings = new ArrayList<>(sqliteBookings);
                        allBookings.addAll(firestoreBookings);

                        // Update the RecyclerView adapter
                        bookingAdapter = new BookingAdapter(allBookings);
                        bookingsList.setAdapter(bookingAdapter);
                    } else {
                        Log.e("Firestore", "Error fetching bookings: " + task.getException().getMessage());
                        Toast.makeText(this, "Failed to fetch bookings from Firestore", Toast.LENGTH_SHORT).show();

                        bookingAdapter = new BookingAdapter(sqliteBookings);
                        bookingsList.setAdapter(bookingAdapter);
                    }
                });
    }

    /**
     * Filters bookings based on a search query.
     *
     * @param query The search query.
     */
    private void filterBookings(String query) {
        if (bookingAdapter == null) return;

        List<Booking> allBookings = bookingAdapter.getBookings();
        List<Booking> filteredBookings = new ArrayList<>();

        for (Booking booking : allBookings) {
            if (booking.getVenueName().toLowerCase().contains(query.toLowerCase()) ||
                    booking.getDate().contains(query) ||
                    booking.getStatus().toLowerCase().contains(query.toLowerCase())) {
                filteredBookings.add(booking);
            }
        }

        bookingAdapter.updateBookings(filteredBookings);
    }
}
