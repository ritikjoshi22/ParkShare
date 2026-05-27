package com.parkshare.frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkshare.frontend.databinding.ItemParkingBinding;
import com.parkshare.frontend.models.Parking;

import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ViewHolder> {

    private List<Parking> parkingList;
    private OnParkingActionListener listener;

    public interface OnParkingActionListener {
        void onParkingClick(Parking parking);
        void onViewOnMapClick(Parking parking);
    }

    public ParkingAdapter(List<Parking> parkingList, OnParkingActionListener listener) {
        this.parkingList = parkingList;
        this.listener = listener;
    }

    public void updateList(List<Parking> newList) {
        this.parkingList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemParkingBinding binding = ItemParkingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Parking parking = parkingList.get(position);
        holder.binding.tvParkingName.setText(parking.getName());
        holder.binding.tvParkingAddress.setText(parking.getAddress());
        holder.binding.tvPrice.setText("NPR " + parking.getPricePerHour() + "/hr");
        holder.binding.tvRating.setText(String.valueOf(parking.getRating()));
        holder.binding.tvSlots.setText(parking.getAvailableSlots() + " slots available");
        holder.binding.tvDistance.setText(parking.getDistance() != null ? parking.getDistance() : "-- km");

        if (parking.isOpen()) {
            holder.binding.tvStatus.setText("Open");
            holder.binding.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.binding.tvStatus.setText("Closed");
            holder.binding.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }

        holder.itemView.setOnClickListener(v -> listener.onParkingClick(parking));
        holder.binding.btnDetails.setOnClickListener(v -> listener.onParkingClick(parking));
        holder.binding.btnMap.setOnClickListener(v -> listener.onViewOnMapClick(parking));
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemParkingBinding binding;

        public ViewHolder(ItemParkingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}