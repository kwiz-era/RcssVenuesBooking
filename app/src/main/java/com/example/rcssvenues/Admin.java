package com.example.rcssvenues;

//import static android.os.Build.VERSION_CODES.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Admin extends AppCompatActivity {

    // Declare UI elements
    private Button buttonViewVenues, buttonAdminAddVenue, buttonEditVenue, buttonDeleteVenue, buttonManageUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Set the layout for this activity

        // Initialize UI elements
        buttonViewVenues = findViewById(R.id.buttonViewVenues);
        buttonAdminAddVenue = findViewById(R.id.buttonAdminAddVenue);
        buttonEditVenue = findViewById(R.id.buttonEditVenue);
        buttonDeleteVenue = findViewById(R.id.buttonDeleteVenue);
        buttonManageUsers = findViewById(R.id.buttonManageUsers);

        // Set click listeners for buttons
        buttonViewVenues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to View Venues activity
                Intent intent = new Intent(Admin.this, AdminViewVenuesActivity.class);
                startActivity(intent);
            }
        });

        buttonAdminAddVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Add Venue activity
                Intent intent = new Intent(Admin.this, AdminAddVenueActivity.class);
                startActivity(intent);
            }
        });

        buttonEditVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Edit Venue activity
                Intent intent = new Intent(Admin.this, AdminEditVenueActivity.class);
                startActivity(intent);
            }
        });

        buttonDeleteVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Delete Venue activity
                Intent intent = new Intent(Admin.this, AdminDeleteVenueActivity.class);
                startActivity(intent);
            }
        });

        buttonManageUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Manage Users activity
                Intent intent = new Intent(Admin.this, AdminManageUsersActivity.class);
                startActivity(intent);
            }
        });
    }
}