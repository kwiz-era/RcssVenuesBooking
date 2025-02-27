package com.example.rcssvenues;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserBookVenue extends AppCompatActivity {

    private EditText etBookingDateTime;
    private Button btnConfirmBooking;
    private TextView tvVenueName, tvVenueDetails;
    private Db_handler dbHandler; // SQLite database handler
    private FirebaseFirestore firestore; // Firestore instance
    private String userEmail;  // Assume user email is retrieved via login or session
    private int venueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_book_venue);

        // Initialize UI components
        etBookingDateTime = findViewById(R.id.etBookingDateTime);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        tvVenueName = findViewById(R.id.tvVenueName);
        tvVenueDetails = findViewById(R.id.tvVenueDetails);

        // Initialize SQLite database handler
        dbHandler = new Db_handler(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get venue details passed via Intent
        venueID = getIntent().getIntExtra("venueID", -1);  // Get venue ID
        String venueName = getIntent().getStringExtra("venueName");
        String venueDetails = getIntent().getStringExtra("venueDetails");

        // Set venue details dynamically
        tvVenueName.setText(venueName);
        tvVenueDetails.setText(venueDetails);

        // Example: Assume userEmail is retrieved from session or login
        userEmail = "user@example.com"; // Replace with dynamic user email

        // Set up the EditText to trigger date and time picker dialogs
        etBookingDateTime.setOnClickListener(v -> showDateTimePicker());

        // Handle Confirm Booking button click
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    /**
     * Show a DatePickerDialog and TimePickerDialog to select date and time.
     */
    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(UserBookVenue.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // On date set, show TimePickerDialog
                    showTimePicker(selectedYear, selectedMonth, selectedDay);
                },
                year, month, day);
        datePickerDialog.show();
    }

    /**
     * Show a TimePickerDialog to select time after date is picked.
     */
    private void showTimePicker(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(UserBookVenue.this,
                (view, selectedHour, selectedMinute) -> {
                    // Format selected date and time
                    String formattedDateTime = String.format("%04d-%02d-%02d %02d:%02d",
                            year, month + 1, day, selectedHour, selectedMinute);
                    etBookingDateTime.setText(formattedDateTime);
                },
                hour, minute, true);
        timePickerDialog.show();
    }

    /**
     * Handle the Confirm Booking button click.
     */
    private void confirmBooking() {
        String selectedDateTime = etBookingDateTime.getText().toString();

        if (selectedDateTime.isEmpty()) {
            Toast.makeText(this, "Please select a date and time for booking.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert the booking into SQLite
        boolean isBookingSuccessfulInSQLite = dbHandler.addBooking(userEmail, venueID, selectedDateTime);

        if (isBookingSuccessfulInSQLite) {
            Toast.makeText(this, "Booking confirmed in SQLite for: " + selectedDateTime, Toast.LENGTH_LONG).show();

            // Insert the booking into Firestore
            addBookingToFirestore(userEmail, venueID, selectedDateTime);
        } else {
            Toast.makeText(this, "Booking failed in SQLite. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds a booking to Firestore.
     *
     * @param userEmail        The email of the user making the booking.
     * @param venueID          The ID of the venue being booked.
     * @param selectedDateTime The selected date and time for the booking.
     */
    private void addBookingToFirestore(String userEmail, int venueID, String selectedDateTime) {
        // Create a new booking object
        Map<String, Object> booking = new HashMap<>();
        booking.put("userEmail", userEmail);
        booking.put("venueID", venueID);
        booking.put("bookingDateTime", selectedDateTime);

        // Add booking to Firestore
        firestore.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Booking confirmed in Firestore for: " + selectedDateTime, Toast.LENGTH_LONG).show();
                    Log.d("Firestore", "Booking added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add booking to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding booking", e);
                });
    }
}