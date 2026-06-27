package com.parkshare.frontend.adapters;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.parkshare.api.models.ParkingSlotDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.ItemParkingSlotBinding;

import java.util.ArrayList;
import java.util.List;

public class ParkingSlotAdapter extends RecyclerView.Adapter<ParkingSlotAdapter.ViewHolder> {

    public interface Listener {
        void onSlotSelected(ParkingSlotDto slot);
    }

    private final Listener listener;
    private final List<ParkingSlotDto> slots = new ArrayList<>();
    private Long selectedSlotId;

    public ParkingSlotAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<ParkingSlotDto> items) {
        slots.clear();
        if (items != null) {
            slots.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setSelectedSlotId(Long id) {
        selectedSlotId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemParkingSlotBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingSlotDto slot = slots.get(position);
        holder.binding.tvSlotLabel.setText(slot.getLabel() != null
                ? slot.getLabel() : "P" + slot.getSlotNumber());

        String status = slot.getDisplayStatus();
        boolean selected = selectedSlotId != null && slot.getId() == selectedSlotId;
        boolean available = "available".equals(status);

        int colorRes = R.color.slot_available;
        if (selected) {
            colorRes = R.color.slot_selected;
        } else if ("occupied".equals(status)) {
            colorRes = R.color.slot_occupied;
        } else if ("reserved".equals(status)) {
            colorRes = R.color.slot_reserved;
        } else if ("maintenance".equals(status)) {
            colorRes = R.color.slot_maintenance;
        }

        int color = ContextCompat.getColor(holder.itemView.getContext(), colorRes);
        holder.binding.cardSlot.setCardBackgroundColor(color);
        holder.binding.tvCarIcon.setTextColor(
                selected ? Color.BLACK : Color.WHITE);
        holder.binding.cardSlot.setAlpha(available || selected ? 1f : 0.55f);
        holder.binding.cardSlot.setEnabled(available);
        holder.itemView.setEnabled(available);

        if (selected) {
            ObjectAnimator.ofFloat(holder.binding.cardSlot, View.SCALE_X, 1f, 1.08f, 1f).setDuration(220).start();
            ObjectAnimator.ofFloat(holder.binding.cardSlot, View.SCALE_Y, 1f, 1.08f, 1f).setDuration(220).start();
        } else {
            holder.binding.cardSlot.setScaleX(1f);
            holder.binding.cardSlot.setScaleY(1f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (available) {
                listener.onSlotSelected(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemParkingSlotBinding binding;

        ViewHolder(ItemParkingSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
