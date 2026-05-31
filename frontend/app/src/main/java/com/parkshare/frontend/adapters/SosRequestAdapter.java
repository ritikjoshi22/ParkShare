package com.parkshare.frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkshare.api.models.SosRequestDto;
import com.parkshare.frontend.databinding.ItemSosRequestBinding;

import java.util.ArrayList;
import java.util.List;

public class SosRequestAdapter extends RecyclerView.Adapter<SosRequestAdapter.VH> {

    public interface Listener {
        void onComplete(SosRequestDto request);
        void onNavigate(SosRequestDto request);
    }

    private final List<SosRequestDto> items = new ArrayList<>();
    private final Listener listener;

    public SosRequestAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<SosRequestDto> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSosRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SosRequestDto item = items.get(position);
        holder.binding.tvMessage.setText(item.getEmergencyMessage() != null
                ? item.getEmergencyMessage() : "Emergency at " + item.getLatitude() + ", " + item.getLongitude());
        holder.binding.tvStatus.setText(item.getStatus());
        holder.binding.btnComplete.setOnClickListener(v -> listener.onComplete(item));
        holder.binding.btnNavigate.setOnClickListener(v -> listener.onNavigate(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemSosRequestBinding binding;

        VH(ItemSosRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
