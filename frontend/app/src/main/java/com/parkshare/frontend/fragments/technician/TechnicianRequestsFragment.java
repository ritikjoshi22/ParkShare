package com.parkshare.frontend.fragments.technician;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.parkshare.api.models.SosRequestDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.adapters.SosRequestAdapter;
import com.parkshare.frontend.utils.LoadingHelper;
import com.parkshare.frontend.utils.ShimmerUi;
import com.parkshare.frontend.databinding.FragmentTechnicianRequestsBinding;
import com.parkshare.frontend.repository.SosRepository;
import com.parkshare.frontend.utils.MapsNavigationHelper;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;

public class TechnicianRequestsFragment extends Fragment implements SosRequestAdapter.Listener {

    private FragmentTechnicianRequestsBinding binding;
    private SosRequestAdapter adapter;
    private SosRepository sosRepository;
    private ShimmerFrameLayout shimmerLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTechnicianRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sosRepository = new SosRepository();
        adapter = new SosRequestAdapter(this);
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRequests.setAdapter(adapter);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        ShimmerUi.prepareListSkeleton(view, R.layout.shimmer_booking_item, 5);

        binding.swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    private void load() {
        if (!binding.swipeRefresh.isRefreshing()) {
            LoadingHelper.showShimmer(shimmerLayout, null);
            binding.rvRequests.setVisibility(View.INVISIBLE);
        }
        sosRepository.getRequests(1, "active", new RepositoryCallback<List<SosRequestDto>>() {
            @Override
            public void onSuccess(List<SosRequestDto> data) {
                LoadingHelper.hideShimmer(shimmerLayout);
                binding.swipeRefresh.setRefreshing(false);
                binding.rvRequests.setVisibility(View.VISIBLE);
                adapter.setItems(data);
                binding.tvEmpty.setVisibility(data == null || data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                LoadingHelper.hideShimmer(shimmerLayout);
                binding.swipeRefresh.setRefreshing(false);
                binding.rvRequests.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onComplete(SosRequestDto request) {
        sosRepository.updateStatus(request.getId(), "resolved", new RepositoryCallback<SosRequestDto>() {
            @Override
            public void onSuccess(SosRequestDto data) {
                Toast.makeText(requireContext(), "Request completed", Toast.LENGTH_SHORT).show();
                load();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onNavigate(SosRequestDto request) {
        MapsNavigationHelper.openNavigation(requireContext(), request.getLatitude(),
                request.getLongitude(), "SOS request");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
