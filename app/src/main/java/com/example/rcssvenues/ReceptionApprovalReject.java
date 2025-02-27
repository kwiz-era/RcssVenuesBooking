package com.example.rcssvenues;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReceptionApprovalReject extends AppCompatActivity {

    private RecyclerView recyclerViewBookings;
    private Button buttonApproveAll, buttonRejectAll;
    private BookingAdapter bookingAdapter;
    private Db_handler dbHandler;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception_approval_reject);

        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        buttonApproveAll = findViewById(R.id.buttonApproveAll);
        buttonRejectAll = findViewById(R.id.buttonRejectAll);

        dbHandler = new Db_handler(this);
        firestore = FirebaseFirestore.getInstance();

        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));

        fetchBookings();

        buttonApproveAll.setOnClickListener(v -> approveAllBookings());
        buttonRejectAll.setOnClickListener(v -> rejectAllBookings());
    }

    private void fetchBookings() {
        List<Booking> sqliteBookings = dbHandler.getAllBookings();
        fetchBookingsFromFirestore(sqliteBookings);
    }

    private void fetchBookingsFromFirestore(List<Booking> sqliteBookings) {
        firestore.collection("bookings")
                .whereEqualTo("status", "Pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Booking> firestoreBookings = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String firestoreId = document.getId();
                            String venueName = document.getString("venueName");
                            String bookingDateTime = document.getString("bookingDateTime");
                            String status = document.getString("status");

                            int venueId = 0;  // Default venue ID

                            // Add Firestore booking
                            Booking booking = new Booking(firestoreId, venueName, venueId, bookingDateTime, status);
                            firestoreBookings.add(booking);
                        }

                        List<Booking> allBookings = new ArrayList<>();
                        allBookings.addAll(sqliteBookings);
                        allBookings.addAll(firestoreBookings);

                        bookingAdapter = new BookingAdapter(allBookings);
                        recyclerViewBookings.setAdapter(bookingAdapter);
                    } else {
                        Log.e("Firestore", "Error fetching bookings: " + task.getException().getMessage());
                        Toast.makeText(this, "Failed to fetch bookings from Firestore", Toast.LENGTH_SHORT).show();

                        bookingAdapter = new BookingAdapter(sqliteBookings);
                        recyclerViewBookings.setAdapter(bookingAdapter);
                    }
                });
    }

    private void approveAllBookings() {
        List<Booking> bookings = bookingAdapter.getBookings();

        for (Booking booking : bookings) {
            if (booking.getId() != null) {
                updateBookingStatusInFirestore(booking.getId(), "Approved");
            } else {
                dbHandler.updateBookingStatus(Integer.parseInt(booking.getId()), "Approved");
            }
        }

        fetchBookings();
        Toast.makeText(this, "All bookings approved!", Toast.LENGTH_SHORT).show();
    }

    private void rejectAllBookings() {
        List<Booking> bookings = bookingAdapter.getBookings();

        for (Booking booking : bookings) {
            if (booking.getId() != null) {
                updateBookingStatusInFirestore(String.valueOf(booking.getId()), "Rejected");
            } else {
                dbHandler.updateBookingStatus(Integer.parseInt(String.valueOf(booking.getId())), "Rejected");
            }
        }

        fetchBookings();
        Toast.makeText(this, "All bookings rejected!", Toast.LENGTH_SHORT).show();
    }

    private void updateBookingStatusInFirestore(String bookingId, String status) {
        firestore.collection("bookings").document(bookingId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Booking status updated: " + bookingId))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating booking: " + bookingId, e));
    }
}