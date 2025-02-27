package com.example.rcssvenues;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ReceptionistVenueManagement extends AppCompatActivity {

    private Db_handler dbHandler;
    private ListView venueListView;
    private Button btnAddVenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptionist_venue_management);

        dbHandler = new Db_handler(this);
        venueListView = findViewById(R.id.venues_list);
        btnAddVenue = findViewById(R.id.add_venue_button);

        // Populate list of existing venues
        List<String> venues = dbHandler.getAllVenues();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, venues);
        venueListView.setAdapter(adapter);

        // Handle adding a new venue
        btnAddVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open AddVenueActivity (you will need to create this)
                // Example:
                // Intent intent = new Intent(VenueManagementActivity.this, AddVenueActivity.class);
                // startActivity(intent);
            }
        });

        // Handle item click for viewing/updating/deleting venue
        venueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle venue update or delete
            }
        });
    }
}
