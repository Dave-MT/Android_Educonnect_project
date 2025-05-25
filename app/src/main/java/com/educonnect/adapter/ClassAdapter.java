package com.educonnect.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.ClassDetailActivity;
import com.educonnect.R;
import com.educonnect.model.Class;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {
    private List<Class> classList;
    private Context context;

    public ClassAdapter(List<Class> classList, Context context) {
        this.classList = classList;
        this.context = context;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        Class classItem = classList.get(position);
        holder.tvClassName.setText(classItem.getName());
        holder.tvClassDescription.setText(classItem.getDescription());

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ClassDetailActivity.class);
            intent.putExtra("CLASS_ID", classItem.getId());
            intent.putExtra("CLASS_NAME", classItem.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvClassName;
        TextView tvClassDescription;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvClassDescription = itemView.findViewById(R.id.tvClassDescription);
        }
    }
}
