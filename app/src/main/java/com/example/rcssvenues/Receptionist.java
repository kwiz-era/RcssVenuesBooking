package com.example.rcssvenues;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Receptionist extends AppCompatActivity {

    // Declare UI elements
    private Button btnDashboard, btnBookingManagement, btnVenueManagement, btnNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptionist); // Set the layout for this activity

        // Initialize UI elements
        btnDashboard = findViewById(R.id.btn_dashboard);
        btnBookingManagement = findViewById(R.id.btn_booking_management);
        btnVenueManagement = findViewById(R.id.btn_venue_management);
        btnNotifications = findViewById(R.id.btn_notifications);

        // Set click listeners for buttons
        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Dashboard Screen
                Intent intent = new Intent(Receptionist.this, ReceptionistDashboard.class);
                startActivity(intent);
            }
        });

        btnBookingManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Booking Management Screen
                Intent intent = new Intent(Receptionist.this, ReceptionistBookingManagement.class);
                startActivity(intent);
            }
        });

        btnVenueManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Venue Management Screen
                Intent intent = new Intent(Receptionist.this, ReceptionistVenueManagement.class);
                startActivity(intent);
            }
        });

       btnNotifications.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
                // Navigate to Notification/Feedback Screen
               Intent intent = new Intent(Receptionist.this, ReceptionistNotifications.class);
                startActivity(intent);
            }
       });
    }
}