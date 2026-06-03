package com.parkshare.frontend.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkshare.api.models.TechnicianServiceDto;
import com.parkshare.frontend.databinding.ItemTechnicianServiceBinding;

import java.util.ArrayList;
import java.util.List;

public class TechnicianServiceAdapter extends RecyclerView.Adapter<TechnicianServiceAdapter.VH> {

    public interface Listener {
        void onDelete(TechnicianServiceDto service);
    }

    private final List<TechnicianServiceDto> items = new ArrayList<>();
    private final Listener listener;

    public TechnicianServiceAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<TechnicianServiceDto> services) {
        items.clear();
        if (services != null) {
            items.addAll(services);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemTechnicianServiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TechnicianServiceDto item = items.get(position);
        holder.binding.tvName.setText(item.getServiceName());
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTechnicianServiceBinding binding;

        VH(ItemTechnicianServiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
