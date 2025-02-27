package com.example.rcssvenues;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings;

    public BookingAdapter(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public final BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public final void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.textView.setText(booking.toString());
    }

    @Override
    public final int getItemCount() {
        return bookings.size();
    }

    // Method to update the list of bookings
    @SuppressLint("NotifyDataSetChanged")
    public final void updateBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    // Method to get the list of bookings
    public final List<Booking> getBookings() {
        return bookings;
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }

        @NonNull
        @Override
        public final String toString() {
            return "BookingViewHolder{" +
                    "textView=" + textView +
                    '}';
        }
    }

    @NonNull
    @Override
    public final String toString() {
        return "BookingAdapter{" +
                "bookings=" + bookings +
                '}';
    }
}