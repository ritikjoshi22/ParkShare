package com.parkshare.frontend.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.databinding.ItemBookingBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    public interface OnBookingActionListener {
        void onCancel(BookingDto booking);
    }

    private final List<BookingDto> items = new ArrayList<>();
    private final OnBookingActionListener listener;

    public BookingAdapter(OnBookingActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<BookingDto> bookings) {
        items.clear();
        if (bookings != null) {
            items.addAll(bookings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingBinding binding = ItemBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingDto booking = items.get(position);
        String name = booking.getParkingSpace() != null
                ? booking.getParkingSpace().getParkingName()
                : "Parking #" + booking.getId();
        holder.binding.tvParkingName.setText(name);
        holder.binding.tvBookingDate.setText(booking.getBookingDate() + " • " + booking.getStartTime());
        holder.binding.tvAmount.setText(String.format(Locale.getDefault(), "NPR %.0f", booking.getTotalAmount()));
        holder.binding.tvStatus.setText(capitalize(booking.getBookingStatus()));

        boolean cancellable = "pending".equals(booking.getBookingStatus())
                || "confirmed".equals(booking.getBookingStatus());
        holder.binding.btnCancel.setVisibility(cancellable ? View.VISIBLE : View.GONE);
        holder.binding.btnCancel.setOnClickListener(v -> listener.onCancel(booking));

        int statusColor = Color.parseColor("#FF9800");
        if ("confirmed".equals(booking.getBookingStatus())) {
            statusColor = Color.parseColor("#4CAF50");
        } else if ("cancelled".equals(booking.getBookingStatus())) {
            statusColor = Color.parseColor("#F44336");
        } else if ("completed".equals(booking.getBookingStatus())) {
            statusColor = Color.parseColor("#2196F3");
        }
        holder.binding.tvStatus.setTextColor(statusColor);
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase(Locale.getDefault()) + value.substring(1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemBookingBinding binding;

        ViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
