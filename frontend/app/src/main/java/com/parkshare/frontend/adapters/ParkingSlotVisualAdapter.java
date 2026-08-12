package com.parkshare.frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.parkshare.api.models.ParkingSlotDto;
import com.parkshare.frontend.R;

import java.util.ArrayList;
import java.util.List;

public class ParkingSlotVisualAdapter extends RecyclerView.Adapter<ParkingSlotVisualAdapter.ViewHolder> {

    public interface Listener {
        void onSlotSelected(ParkingSlotDto slot);
    }

    private final List<ParkingSlotDto> items = new ArrayList<>();
    private final Listener listener;
    private Long selectedSlotId;

    public ParkingSlotVisualAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<ParkingSlotDto> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setSelectedSlotId(Long id) {
        this.selectedSlotId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking_slot_visual, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingSlotDto slot = items.get(position);
        holder.tvLabel.setText(slot.getLabel());

        String status = slot.getDisplayStatus();
        int color;
        if (selectedSlotId != null && slot.getId() == selectedSlotId) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.slot_selected);
        } else {
            String s = status != null ? status.toLowerCase() : "";
            switch (s) {
                case "available":
                    color = ContextCompat.getColor(holder.itemView.getContext(), R.color.slot_available);
                    break;
                case "paid":
                case "confirmed":
                case "checked_in":
                    color = ContextCompat.getColor(holder.itemView.getContext(), R.color.slot_occupied); // Red (Payment Done)
                    break;
                case "occupied":
                case "reserved":
                case "pending":
                case "unpaid":
                    color = ContextCompat.getColor(holder.itemView.getContext(), R.color.slot_reserved); // Blue (Payment Pending)
                    break;
                default:
                    color = ContextCompat.getColor(holder.itemView.getContext(), R.color.slot_maintenance);
                    break;
            }
        }
        
        holder.card.setCardBackgroundColor(color);
        holder.itemView.setOnClickListener(v -> listener.onSlotSelected(slot));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvLabel;
        ImageView ivCar;

        ViewHolder(@NonNull View view) {
            super(view);
            card = view.findViewById(R.id.cardSlot);
            tvLabel = view.findViewById(R.id.tvSlotLabel);
            ivCar = view.findViewById(R.id.ivSlotCar);
        }
    }
}
