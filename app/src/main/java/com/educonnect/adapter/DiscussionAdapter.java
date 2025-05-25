package com.educonnect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

public class DiscussionAdapter extends BaseAdapter {
    private Context context;
    private List<DiscussionMessage> messages;
    private int currentUserId;
    private LayoutInflater inflater;

    // Constructor for TeacherDiscussionsActivity
    public DiscussionAdapter(Context context, List<DiscussionMessage> messages, int currentUserId) {
        this.context = context;
        this.messages = messages != null ? messages : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.inflater = LayoutInflater.from(context);
    }

    // Constructor for DiscussionActivity
    public DiscussionAdapter(Context context, int currentUserId) {
        this.context = context;
        this.messages = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.inflater = LayoutInflater.from(context);
    }

    public void setMessages(List<DiscussionMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public DiscussionMessage getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DiscussionMessage message = getItem(position);

        // Check if the message is from the current user
        boolean isCurrentUser = message.getUserId() == currentUserId;

        if (convertView == null) {
            if (isCurrentUser) {
                convertView = inflater.inflate(R.layout.item_message_mine, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.item_message, parent, false);
            }
        }

        // Get views
        TextView tvSender = convertView.findViewById(R.id.tvSender);
        TextView tvMessage = convertView.findViewById(R.id.tvMessage);
        TextView tvTime = convertView.findViewById(R.id.tvTime);

        // Set data
        tvSender.setText(message.getUserName());
        tvMessage.setText(message.getMessage());

        // Format time
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US);
            Date date = inputFormat.parse(message.getCreatedAt());
            tvTime.setText(outputFormat.format(date));
        } catch (ParseException e) {
            tvTime.setText(message.getCreatedAt());
        }

        return convertView;
    }
}
