package com.educonnect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.R;
import com.educonnect.model.DiscussionMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecyclerDiscussionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;

    private List<DiscussionMessage> messages = new ArrayList<>();
    private Context context;
    private int currentUserId;

    public RecyclerDiscussionAdapter(Context context, int currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<DiscussionMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        DiscussionMessage message = messages.get(position);
        if (message.getUserId() == currentUserId) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_mine, parent, false);
            return new MyMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new OtherMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DiscussionMessage message = messages.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_MY_MESSAGE) {
            MyMessageViewHolder myHolder = (MyMessageViewHolder) holder;
            myHolder.tvMessage.setText(message.getMessage());
            myHolder.tvTime.setText(formatTime(message.getCreatedAt()));
        } else {
            OtherMessageViewHolder otherHolder = (OtherMessageViewHolder) holder;
            otherHolder.tvSender.setText(message.getUserName());
            otherHolder.tvMessage.setText(message.getMessage());
            otherHolder.tvTime.setText(formatTime(message.getCreatedAt()));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.US);
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return timestamp;
        }
    }

    static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTime;

        MyMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender;
        TextView tvMessage;
        TextView tvTime;

        OtherMessageViewHolder(View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
