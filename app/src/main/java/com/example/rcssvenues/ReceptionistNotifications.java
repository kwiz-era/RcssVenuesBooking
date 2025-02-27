package com.example.rcssvenues;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistNotifications extends AppCompatActivity {

    // Declare UI elements
    private ListView notificationListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptionist_notification); // Set the layout for this activity

        // Initialize UI elements
        notificationListView = findViewById(R.id.notificationListView);

        // Fetch notifications from the database or a static list
        List<String> notifications = fetchNotifications();

        // Create an adapter to display the notifications in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1, // Default layout for list items
                notifications
        );

        // Set the adapter to the ListView
        notificationListView.setAdapter(adapter);
    }

    // Method to fetch notifications (replace with actual database logic)
    private List<String> fetchNotifications() {
        List<String> notifications = new ArrayList<>();
        notifications.add("New booking request for Golden Aureole on 2023-10-25");
        notifications.add("Venue Auditorium has been approved for event on 2023-10-30");
        notifications.add("Reminder: Event at Seminar Hall starts in 2 hours");
        notifications.add("Feedback received for event at Conference Room");
        return notifications;
    }
}