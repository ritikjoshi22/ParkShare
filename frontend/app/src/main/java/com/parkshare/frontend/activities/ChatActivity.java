package com.parkshare.frontend.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.parkshare.api.models.MessageDto;
import com.parkshare.frontend.adapters.MessageAdapter;
import com.parkshare.frontend.databinding.ActivityChatBinding;
import com.parkshare.frontend.repository.ChatRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_RECEIVER_ID = "receiver_id";
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_NAME = "name";

    private ActivityChatBinding binding;
    private ChatRepository chatRepository;
    private SessionManager sessionManager;
    private MessageAdapter adapter;
    private long receiverId;
    private Long bookingId;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override
        public void run() {
            loadMessages();
            handler.postDelayed(this, 3000); // Poll every 3 seconds
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatRepository = new ChatRepository();
        sessionManager = SessionManager.getInstance(this);

        receiverId = getIntent().getLongExtra(EXTRA_RECEIVER_ID, -1);
        long bId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1);
        if (bId > 0) bookingId = bId;
        
        String name = getIntent().getStringExtra(EXTRA_NAME);
        if (name != null) binding.toolbar.setTitle(name);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new MessageAdapter(sessionManager.getUserId());
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(pollTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(pollTask);
    }

    private void loadMessages() {
        if (bookingId == null) return;
        chatRepository.getMessages(bookingId, new RepositoryCallback<List<MessageDto>>() {
            @Override
            public void onSuccess(List<MessageDto> data) {
                if (data != null) {
                    adapter.setMessages(data);
                    if (data.size() > 0) {
                        binding.rvMessages.scrollToPosition(data.size() - 1);
                    }
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        binding.etMessage.setText("");
        chatRepository.sendMessage(receiverId, bookingId, text, new RepositoryCallback<MessageDto>() {
            @Override
            public void onSuccess(MessageDto data) {
                adapter.addMessage(data);
                binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
