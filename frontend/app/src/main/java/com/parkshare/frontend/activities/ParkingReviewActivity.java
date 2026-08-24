package com.parkshare.frontend.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.api.models.ReviewDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.ActivityParkingReviewBinding;
import com.parkshare.frontend.repository.ReviewRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

public class ParkingReviewActivity extends AppCompatActivity {

    public static final String EXTRA_PARKING_ID = "parking_id";
    public static final String EXTRA_PARKING_NAME = "parking_name";

    private ActivityParkingReviewBinding binding;
    private long parkingId;
    private final ReviewRepository reviewRepository = new ReviewRepository();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParkingReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parkingId = getIntent().getLongExtra(EXTRA_PARKING_ID, -1);
        String parkingName = getIntent().getStringExtra(EXTRA_PARKING_NAME);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (parkingName != null) {
            binding.tvReviewTitle.setText(getString(R.string.review_parking_title, parkingName));
        }

        binding.btnWriteReview.setOnClickListener(v -> submitReview());

        binding.btnAttachPhoto.setOnClickListener(v -> Toast.makeText(this, "Photo upload is optional and will be available in the next update.", Toast.LENGTH_SHORT).show());
    }

    private void submitReview() {
        int rating = (int) binding.ratingBar.getRating();
        if (rating == 0) {
            Toast.makeText(this, R.string.select_rating_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = binding.etExperience.getText() != null ? binding.etExperience.getText().toString().trim() : "";

        binding.btnWriteReview.setEnabled(false);
        reviewRepository.submitParkingReview(parkingId, rating, comment, new RepositoryCallback<ReviewDto>() {
            @Override
            public void onSuccess(ReviewDto data) {
                Toast.makeText(ParkingReviewActivity.this, R.string.review_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                binding.btnWriteReview.setEnabled(true);
                Toast.makeText(ParkingReviewActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
