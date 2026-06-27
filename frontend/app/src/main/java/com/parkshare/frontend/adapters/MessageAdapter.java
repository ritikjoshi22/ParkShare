package com.parkshare.frontend.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkshare.api.models.MessageDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final List<MessageDto> messages = new ArrayList<>();
    private final long currentUserId;

    public MessageAdapter(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<MessageDto> newMessages) {
        messages.clear();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    public void addMessage(MessageDto message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageDto message = messages.get(position);
        boolean isMe = message.getSenderId() == currentUserId;

        holder.tvMessage.setText(message.getMessage());
        
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.layoutBubble.getLayoutParams();
        if (isMe) {
            params.gravity = Gravity.END;
            holder.layoutBubble.setBackgroundResource(R.drawable.bg_chat_bubble_me);
            holder.tvMessage.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
        } else {
            params.gravity = Gravity.START;
            holder.layoutBubble.setBackgroundResource(R.drawable.bg_chat_bubble_other);
            holder.tvMessage.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
        }
        holder.layoutBubble.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        View layoutBubble;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            layoutBubble = itemView.findViewById(R.id.layoutBubble);
        }
    }
}
