package com.example.rcssvenues;

import androidx.annotation.NonNull;

public class Booking {
    private final int id; // Firestore ID (String) or SQLite ID converted to String
    private final String venueName;
    private final int venueId;
    private final String date;
    private final String status;

    // Constructor for both Firestore and SQLite bookings
    public Booking(int id, String venueName, int venueId, String date, String status) {
        this.id = id;
        this.venueName = venueName;
        this.venueId = venueId;
        this.date = date;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getVenueName() {
        return venueName;
    }

    public int getVenueId() {
        return venueId;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    @NonNull
    @Override
    public String toString() {
        return "Booking{" +
                "id='" + id + '\'' +
                ", venueName='" + venueName + '\'' +
                ", venueId=" + venueId +
                ", date='" + date + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}