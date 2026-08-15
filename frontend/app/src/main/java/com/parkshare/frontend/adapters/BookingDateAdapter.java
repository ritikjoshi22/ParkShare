package com.parkshare.frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.parkshare.frontend.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BookingDateAdapter extends RecyclerView.Adapter<BookingDateAdapter.ViewHolder> {

    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    private final List<LocalDate> dates;
    private final OnDateSelectedListener listener;
    private int selectedPosition = 0;

    public BookingDateAdapter(List<LocalDate> dates, OnDateSelectedListener listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_date, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalDate date = dates.get(position);
        DateTimeFormatter dayNameFmt = DateTimeFormatter.ofPattern("EEE", Locale.getDefault());
        
        holder.tvDayName.setText(date.format(dayNameFmt));
        holder.tvDayNumber.setText(String.valueOf(date.getDayOfMonth()));

        boolean isSelected = position == selectedPosition;
        holder.cardDate.setStrokeColor(isSelected 
            ? ContextCompat.getColor(holder.itemView.getContext(), R.color.primary)
            : ContextCompat.getColor(holder.itemView.getContext(), R.color.gray_200));
        
        holder.cardDate.setCardBackgroundColor(isSelected
            ? ContextCompat.getColor(holder.itemView.getContext(), R.color.primary)
            : ContextCompat.getColor(holder.itemView.getContext(), R.color.white));

        holder.tvDayName.setTextColor(isSelected ? holder.white : holder.textPrimary);
        holder.tvDayNumber.setTextColor(isSelected ? holder.white : holder.textPrimary);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            listener.onDateSelected(date);
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardDate;
        TextView tvDayName, tvDayNumber;
        int white, textPrimary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDate = itemView.findViewById(R.id.cardDate);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            white = ContextCompat.getColor(itemView.getContext(), R.color.white);
            textPrimary = ContextCompat.getColor(itemView.getContext(), R.color.text_primary);
        }
    }
}
