package com.example.rcssvenues;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {

    private static final String TAG = "FirestoreHelper";

    // Firebase Firestore instance
    private final FirebaseFirestore firestore;

    // Constructor
    public FirestoreHelper() {
        firestore = FirebaseFirestore.getInstance();
    }

    // ==================== Add Data ====================

    /**
     * Adds a new document to a Firestore collection.
     *
     * @param collection The name of the Firestore collection.
     * @param data       The data to add as a Map.
     * @param listener   Callback to handle success or failure.
     */
    public void addDocument(String collection, Map<String, Object> data, OnFirestoreCompleteListener listener) {
        firestore.collection(collection)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Document added with ID: " + documentReference.getId());
                        listener.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                        listener.onFailure(e);
                    }
                });
    }

    // ==================== Update Data ====================

    /**
     * Updates a document in a Firestore collection.
     *
     * @param collection The name of the Firestore collection.
     * @param documentId The ID of the document to update.
     * @param data       The data to update as a Map.
     * @param listener   Callback to handle success or failure.
     */
    public void updateDocument(String collection, String documentId, Map<String, Object> data, OnFirestoreCompleteListener listener) {
        firestore.collection(collection)
                .document(documentId)
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document updated: " + documentId);
                        listener.onSuccess(documentId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error updating document", e);
                        listener.onFailure(e);
                    }
                });
    }

    // ==================== Delete Data ====================

    /**
     * Deletes a document from a Firestore collection.
     *
     * @param collection The name of the Firestore collection.
     * @param documentId The ID of the document to delete.
     * @param listener   Callback to handle success or failure.
     */
    public void deleteDocument(String collection, String documentId, OnFirestoreCompleteListener listener) {
        firestore.collection(collection)
                .document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document deleted: " + documentId);
                        listener.onSuccess(documentId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting document", e);
                        listener.onFailure(e);
                    }
                });
    }

    // ==================== Query Data ====================

    /**
     * Fetches all documents from a Firestore collection.
     *
     * @param collection The name of the Firestore collection.
     * @param listener   Callback to handle success or failure.
     */
    public void getAllDocuments(String collection, OnFirestoreQueryCompleteListener listener) {
        firestore.collection(collection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Document: " + document.getId() + " => " + document.getData());
                            }
                            listener.onSuccess(task.getResult());
                        } else {
                            Log.e(TAG, "Error fetching documents", task.getException());
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    // ==================== Callback Interfaces ====================

    /**
     * Callback interface for Firestore operations.
     */
    public interface OnFirestoreCompleteListener {
        void onSuccess(String documentId);
        void onFailure(Exception exception);
    }

    /**
     * Callback interface for Firestore query operations.
     */
    public interface OnFirestoreQueryCompleteListener {
        void onSuccess(QuerySnapshot querySnapshot);
        void onFailure(Exception exception);
    }
}