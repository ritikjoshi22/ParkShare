package com.parkshare.frontend.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parkshare.api.models.BookingDto;
import com.parkshare.api.models.ParkingImageDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.ItemBookingBinding;
import com.parkshare.frontend.utils.DateTimeFormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    public interface OnBookingActionListener {
        void onCancel(BookingDto booking);

        void onViewQr(BookingDto booking);

        void onExtend(BookingDto booking);

        void onPay(BookingDto booking);

        void onChat(BookingDto booking);

        void onWriteReview(BookingDto booking);
    }

    private final List<BookingDto> items = new ArrayList<>();
    private final OnBookingActionListener listener;
    private boolean allowCancel = true;
    private boolean showQrButton = false;
    private boolean showQrWithoutCode = false;

    public BookingAdapter(OnBookingActionListener listener) {
        this.listener = listener;
    }

    public void setAllowCancel(boolean allowCancel) {
        this.allowCancel = allowCancel;
    }

    public void setShowQrButton(boolean showQrButton) {
        this.showQrButton = showQrButton;
    }

    /** When true, show QR action for active bookings even if qr_code is not in the list payload. */
    public void setShowQrWithoutCode(boolean showQrWithoutCode) {
        this.showQrWithoutCode = showQrWithoutCode;
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
        holder.binding.tvBookingDate.setText(DateTimeFormatUtil.formatBookingDate(booking.getStartTime()));
        holder.binding.tvBookingTime.setText(DateTimeFormatUtil.formatTimeRange(booking.getStartTime(), booking.getEndTime()));

        holder.binding.tvAmount.setText(String.format(Locale.getDefault(), "NPR %.0f", booking.getTotalAmount()));
        holder.binding.tvStatus.setText(capitalize(booking.getBookingStatus()));

        if (booking.getParkingSpace() != null && booking.getParkingSpace().getImages() != null && !booking.getParkingSpace().getImages().isEmpty()) {
            ParkingImageDto img = booking.getParkingSpace().getImages().get(0);
            Glide.with(holder.itemView.getContext())
                    .load(img.getImageUrl())
                    .placeholder(R.drawable.ic_parking)
                    .into(holder.binding.ivParking);
        }

        boolean cancellable = allowCancel && ("pending".equals(booking.getBookingStatus())
                || "confirmed".equals(booking.getBookingStatus()));
        holder.binding.btnCancel.setVisibility(cancellable ? View.VISIBLE : View.GONE);
        holder.binding.btnCancel.setOnClickListener(v -> listener.onCancel(booking));

        boolean active = "pending".equals(booking.getBookingStatus())
                || "confirmed".equals(booking.getBookingStatus())
                || "checked_in".equals(booking.getBookingStatus());
        boolean canShowQr = showQrButton && active && (showQrWithoutCode || hasQr(booking));
        holder.binding.btnViewQr.setVisibility(canShowQr ? View.VISIBLE : View.GONE);
        holder.binding.btnViewQr.setOnClickListener(v -> listener.onViewQr(booking));

        boolean canExtend = showQrButton && ("confirmed".equals(booking.getBookingStatus())
                || "checked_in".equals(booking.getBookingStatus()));
        if (holder.binding.btnExtend != null) {
            holder.binding.btnExtend.setVisibility(canExtend ? View.VISIBLE : View.GONE);
            holder.binding.btnExtend.setOnClickListener(v -> listener.onExtend(booking));
        }
        boolean needsPay = booking.getAmountDue() > 0 && !"paid".equals(booking.getPaymentStatus());
        if (holder.binding.btnPay != null) {
            holder.binding.btnPay.setVisibility(needsPay ? View.VISIBLE : View.GONE);
            holder.binding.btnPay.setOnClickListener(v -> listener.onPay(booking));
        }

        if (holder.binding.btnChat != null) {
            holder.binding.btnChat.setOnClickListener(v -> listener.onChat(booking));
        }

        if (holder.binding.btnWriteReview != null) {
            holder.binding.btnWriteReview.setOnClickListener(v -> listener.onWriteReview(booking));
        }

        if (holder.binding.btnMore != null) {
            holder.binding.btnMore.setOnClickListener(v -> {
                // Placeholder for future actions like Delete from history, etc.
                Toast.makeText(holder.itemView.getContext(), "More options coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Logical thinking: Only show review section for active or completed bookings
        boolean showReview = "confirmed".equals(booking.getBookingStatus())
                || "checked_in".equals(booking.getBookingStatus())
                || "completed".equals(booking.getBookingStatus());
        holder.binding.layoutReview.setVisibility(showReview ? View.VISIBLE : View.GONE);

        int statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.accent);
        int statusBg = R.drawable.bg_light_gray_card;

        if ("confirmed".equals(booking.getBookingStatus())) {
            statusColor = Color.parseColor("#2E7D32"); // Green
            statusBg = R.drawable.bg_light_green_card;
        } else if ("checked_in".equals(booking.getBookingStatus())) {
            statusColor = Color.parseColor("#1E3A8A"); // Professional Blue
            statusBg = R.drawable.bg_light_blue_card;
        } else if ("cancelled".equals(booking.getBookingStatus())) {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.error);
            statusBg = R.drawable.bg_light_gray_card;
        } else if ("completed".equals(booking.getBookingStatus())) {
            statusColor = Color.parseColor("#1E3A8A");
            statusBg = R.drawable.bg_light_blue_card;
        } else if ("pending".equals(booking.getBookingStatus())) {
            statusColor = Color.parseColor("#F59E0B"); // Accent/Orange
            statusBg = R.drawable.bg_light_gray_card;
        }

        holder.binding.tvStatus.setTextColor(statusColor);
        holder.binding.tvStatus.setBackgroundResource(statusBg);
    }

    private boolean hasQr(BookingDto booking) {
        return booking.getQrCode() != null && !booking.getQrCode().isEmpty();
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
