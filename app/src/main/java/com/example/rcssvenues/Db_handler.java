package com.example.rcssvenues;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Db_handler extends SQLiteOpenHelper {

    // Database Information
    private static final String DATABASE_NAME = "VenueManager.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_VENUES = "venues";
    private static final String TABLE_BOOKINGS = "bookings";

    // Common Column Names
    private static final String COLUMN_ID = "id";

    // Users Table Columns
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role";

    // Venues Table Columns
    private static final String COLUMN_VENUE_NAME = "name";
    private static final String COLUMN_VENUE_LOCATION = "location";
    private static final String COLUMN_VENUE_CAPACITY = "capacity";

    // Bookings Table Columns
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_VENUE_ID = "venue_id";
    private static final String COLUMN_BOOKING_DATETIME = "booking_datetime";
    private static final String COLUMN_BOOKING_STATUS = "status";

    // Firebase Firestore instance
    private FirebaseFirestore firestore;

    // Table Creation Queries
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_ROLE + " TEXT);";

    private static final String CREATE_TABLE_VENUES =
            "CREATE TABLE " + TABLE_VENUES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_VENUE_NAME + " TEXT, " +
                    COLUMN_VENUE_LOCATION + " TEXT, " +
                    COLUMN_VENUE_CAPACITY + " INTEGER);";

    private static final String CREATE_TABLE_BOOKINGS =
            "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_EMAIL + " TEXT, " +
                    COLUMN_VENUE_ID + " INTEGER, " +
                    COLUMN_BOOKING_DATETIME + " TEXT, " +
                    COLUMN_BOOKING_STATUS + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_VENUE_ID + ") REFERENCES " + TABLE_VENUES + "(" + COLUMN_ID + "));";

    // Constructor
    public Db_handler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_VENUES);
        db.execSQL(CREATE_TABLE_BOOKINGS);

        // Insert default users
        insertDefaultUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENUES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);

        // Create tables again
        onCreate(db);
    }

    // ==================== User Operations ====================

    /**
     * Inserts default users into the database.
     */
    private void insertDefaultUsers(SQLiteDatabase db) {
        insertUser(db, "admin@rajagiri.edu", hashPassword("Admin@123"), "admin");
        insertUser(db, "receptionist@rajagiri.edu", hashPassword("Receptionist@123"), "receptionist");
    }

    /**
     * Inserts a new user into the database.
     */
    private void insertUser(SQLiteDatabase db, String username, String password, String role) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);

        long result = db.insert(TABLE_USERS, null, values);
        if (result == -1) {
            Log.e("Db_handler", "Failed to insert user: " + username);
        } else {
            Log.d("Db_handler", "User inserted successfully: " + username);
        }
    }

    /**
     * Adds a new user to the database and Firestore.
     */
    public void addNewUser(String username, String password, String role, String s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashPassword(password));
        values.put(COLUMN_ROLE, role);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        if (result != -1) {
            Log.d("Db_handler", "User inserted successfully: " + username);
            addUserToFirestore(username, password, role); // Add to Firestore
        } else {
            Log.e("Db_handler", "Failed to insert user: " + username);
        }
    }

    /**
     * Adds a user to Firestore.
     */
    private void addUserToFirestore(String username, String password, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put(COLUMN_USERNAME, username);
        user.put(COLUMN_PASSWORD, hashPassword(password));
        user.put(COLUMN_ROLE, role);

        firestore.collection(TABLE_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "User added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding user", e);
                });
    }

    /**
     * Checks if the provided email and password match a user in the database.
     */
    public boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, hashPassword(password)});

        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return isValid;
    }

    /**
     * Fetches the role of a user by their email.
     */
    @SuppressLint("Range")
    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ROLE + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        String role = null;
        if (cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndex(COLUMN_ROLE));
        }
        cursor.close();
        db.close();

        return role;
    }

    /**
     * Checks if an email is already registered in the database.
     */
    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        boolean isRegistered = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return isRegistered;
    }

    // ==================== Venue Operations ====================

    /**
     * Adds a new venue to the database and Firestore.
     */
    public void addVenue(String name, String location, int capacity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_VENUE_NAME, name);
        values.put(COLUMN_VENUE_LOCATION, location);
        values.put(COLUMN_VENUE_CAPACITY, capacity);

        long result = db.insert(TABLE_VENUES, null, values);
        db.close();

        if (result != -1) {
            Log.d("Db_handler", "Venue inserted successfully: " + name);
            addVenueToFirestore(name, location, capacity); // Add to Firestore
        } else {
            Log.e("Db_handler", "Failed to insert venue: " + name);
        }
    }

    /**
     * Adds a venue to Firestore.
     */
    private void addVenueToFirestore(String name, String location, int capacity) {
        Map<String, Object> venue = new HashMap<>();
        venue.put(COLUMN_VENUE_NAME, name);
        venue.put(COLUMN_VENUE_LOCATION, location);
        venue.put(COLUMN_VENUE_CAPACITY, capacity);

        firestore.collection(TABLE_VENUES)
                .add(venue)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Venue added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding venue", e);
                });

    }

    // ==================== Booking Operations ====================

    /**
     * Adds a new booking to the database and Firestore.
     */
    public boolean addBooking(String userEmail, int venueID, String selectedDateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_EMAIL, userEmail);
        values.put(COLUMN_VENUE_ID, venueID);
        values.put(COLUMN_BOOKING_DATETIME, selectedDateTime);

        long result = db.insert(TABLE_BOOKINGS, null, values);
        db.close();

        if (result != -1) {
            addBookingToFirestore(userEmail, venueID, selectedDateTime); // Add to Firestore
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a booking to Firestore.
     */
    private void addBookingToFirestore(String userEmail, int venueID, String bookingDateTime) {
        Map<String, Object> booking = new HashMap<>();
        booking.put(COLUMN_USER_EMAIL, userEmail);
        booking.put(COLUMN_VENUE_ID, venueID);
        booking.put(COLUMN_BOOKING_DATETIME, bookingDateTime);
        booking.put(COLUMN_BOOKING_STATUS, "Pending"); // Default status

        firestore.collection(TABLE_BOOKINGS)
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Booking added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding booking", e);
                });
    }

    // ==================== Utility Methods ====================

    /**
     * Hashes a password using SHA-256.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("Db_handler", "Failed to hash password", e);
            return null;
        }
    }


    /**
     * Deletes a venue from the SQLite database and Firebase Firestore.
     *
     * @param venueID The ID of the venue to delete.
     * @return True if the venue was deleted successfully, false otherwise.
     */
    public boolean deleteVenue(int venueID) {
        // Delete from SQLite database
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_VENUES, COLUMN_ID + " = ?", new String[]{String.valueOf(venueID)});
        db.close();

        if (rowsAffected > 0) {
            Log.d("Db_handler", "Venue deleted from SQLite: " + venueID);

            // Delete from Firebase Firestore
            deleteVenueFromFirestore(venueID);
            return true;
        } else {
            Log.e("Db_handler", "Failed to delete venue from SQLite: " + venueID);
            return false;
        }
    }

    /**
     * Deletes a venue from Firebase Firestore.
     *
     * @param venueID The ID of the venue to delete.
     */
    private void deleteVenueFromFirestore(int venueID) {
        firestore.collection(TABLE_VENUES)
                .whereEqualTo(COLUMN_ID, venueID) // Find the venue by its ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete the document
                            firestore.collection(TABLE_VENUES).document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Venue deleted from Firestore: " + venueID);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error deleting venue from Firestore", e);
                                    });
                        }
                    } else {
                        Log.e("Firestore", "Error finding venue in Firestore", task.getException());
                    }
                });
    }

    /**
     * Updates a venue in the SQLite database and Firebase Firestore.
     *
     * @param venueID      The ID of the venue to update.
     * @param newVenueName The new name of the venue.
     * @param newLocation  The new location of the venue.
     * @param newCapacity  The new capacity of the venue.
     * @return True if the venue was updated successfully, false otherwise.
     */
    public boolean updateVenue(int venueID, String newVenueName, String newLocation, int newCapacity) {
        // Update in SQLite database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_VENUE_NAME, newVenueName);
        values.put(COLUMN_VENUE_LOCATION, newLocation);
        values.put(COLUMN_VENUE_CAPACITY, newCapacity);

        int rowsAffected = db.update(TABLE_VENUES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(venueID)});
        db.close();

        if (rowsAffected > 0) {
            Log.d("Db_handler", "Venue updated in SQLite: " + venueID);

            // Update in Firebase Firestore
            updateVenueInFirestore(venueID, newVenueName, newLocation, newCapacity);
            return true;
        } else {
            Log.e("Db_handler", "Failed to update venue in SQLite: " + venueID);
            return false;
        }
    }

    /**
     * Updates a venue in Firebase Firestore.
     *
     * @param venueID      The ID of the venue to update.
     * @param newVenueName The new name of the venue.
     * @param newLocation  The new location of the venue.
     * @param newCapacity  The new capacity of the venue.
     */
    private void updateVenueInFirestore(int venueID, String newVenueName, String newLocation, int newCapacity) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(COLUMN_VENUE_NAME, newVenueName);
        updates.put(COLUMN_VENUE_LOCATION, newLocation);
        updates.put(COLUMN_VENUE_CAPACITY, newCapacity);

        firestore.collection(TABLE_VENUES)
                .whereEqualTo(COLUMN_ID, venueID) // Find the venue by its ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Update the document
                            firestore.collection(TABLE_VENUES).document(document.getId()).update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Venue updated in Firestore: " + venueID);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error updating venue in Firestore", e);
                                    });
                        }
                    } else {
                        Log.e("Firestore", "Error finding venue in Firestore", task.getException());
                    }
                });
    }

    /**
     * Fetches all venues from the SQLite database.
     *
     * @return A list of venue names.
     */
    @SuppressLint("Range")
    public List<String> getAllVenues() {
        List<String> venuesList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch all venues
        String query = "SELECT " + COLUMN_VENUE_NAME + " FROM " + TABLE_VENUES;
        Cursor cursor = db.rawQuery(query, null);

        // Iterate through the cursor and add venue names to the list
        if (cursor.moveToFirst()) {
            do {
                String venueName = cursor.getString(cursor.getColumnIndex(COLUMN_VENUE_NAME));
                venuesList.add(venueName);
            } while (cursor.moveToNext());
        }

        // Close the cursor and database connection
        cursor.close();
        db.close();

        return venuesList;
    }

    /**
     * Fetches the role of a user by their email address.
     *
     * @param email The email address of the user.
     * @return The role of the user, or null if the user is not found.
     */
    @SuppressLint("Range")
    public String getUserRoleByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String role = null;

        // Query to fetch the role for the given email
        String query = "SELECT " + COLUMN_ROLE + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        // Check if the cursor has data
        if (cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndex(COLUMN_ROLE)); // Fetch the role of the user
        }

        // Close the cursor and database connection
        cursor.close();
        db.close();

        return role;
    }

    /**
     * Fetches all bookings from the SQLite database.
     *
     * @return A list of Booking objects.
     */
    @SuppressLint("Range")
    public List<Booking> getAllBookings() {
        List<Booking> bookingsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch all bookings
        String query = "SELECT * FROM " + TABLE_BOOKINGS;
        Cursor cursor = db.rawQuery(query, null);

        // Iterate through the cursor and create Booking objects
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String userEmail = cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL));
                int venueId = cursor.getInt(cursor.getColumnIndex(COLUMN_VENUE_ID));
                String bookingDateTime = cursor.getString(cursor.getColumnIndex(COLUMN_BOOKING_DATETIME));
                String status = cursor.getString(cursor.getColumnIndex(COLUMN_BOOKING_STATUS));

                // Create a new Booking object and add it to the list
                Booking booking = new Booking(id, userEmail, venueId, bookingDateTime, status);
                bookingsList.add(booking);
            } while (cursor.moveToNext());
        }

        // Close the cursor and database connection
        cursor.close();
        db.close();

        return bookingsList;
    }

    /**
     * Updates the status of a booking in the SQLite database and Firebase Firestore.
     *
     * @param bookingId The ID of the booking to update.
     * @param newStatus The new status of the booking (e.g., "Approved", "Rejected").
     */
    public void updateBookingStatus(int bookingId, String newStatus) {
        // Update in SQLite database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_BOOKING_STATUS, newStatus);

        int rowsAffected = db.update(TABLE_BOOKINGS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(bookingId)});
        db.close();

        if (rowsAffected > 0) {
            Log.d("Db_handler", "Booking status updated in SQLite: " + bookingId);

            // Update in Firebase Firestore
            updateBookingStatusInFirestore(bookingId, newStatus);
        } else {
            Log.e("Db_handler", "Failed to update booking status in SQLite: " + bookingId);
        }
    }

    /**
     * Updates the status of a booking in Firebase Firestore.
     *
     * @param bookingId The ID of the booking to update.
     * @param newStatus The new status of the booking.
     */
    private void updateBookingStatusInFirestore(int bookingId, String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(COLUMN_BOOKING_STATUS, newStatus);

        firestore.collection(TABLE_BOOKINGS)
                .whereEqualTo(COLUMN_ID, bookingId) // Find the booking by its ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Update the document
                            firestore.collection(TABLE_BOOKINGS).document(document.getId()).update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Booking status updated in Firestore: " + bookingId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error updating booking status in Firestore", e);
                                    });
                        }
                    } else {
                        Log.e("Firestore", "Error finding booking in Firestore", task.getException());
                    }
                });
    }
}