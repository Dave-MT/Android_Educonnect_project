package com.educonnect.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.AssignmentSubmissionActivity;
import com.educonnect.R;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.Assignment;

import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {
  private List<Assignment> assignmentList;
  private Context context;

  public AssignmentAdapter(List<Assignment> assignmentList, Context context) {
      this.assignmentList = assignmentList;
      this.context = context;
  }

  @NonNull
  @Override
  public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
      return new AssignmentViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
      Assignment assignment = assignmentList.get(position);
      holder.tvAssignmentTitle.setText(assignment.getTitle());
      holder.tvAssignmentDescription.setText(assignment.getDescription());
      holder.tvDueDate.setText("Due: " + assignment.getDueDate());
      
      holder.cardView.setOnClickListener(v -> {
          Intent intent = new Intent(context, AssignmentSubmissionActivity.class);
          intent.putExtra("ASSIGNMENT_ID", assignment.getId());
          context.startActivity(intent);
      });
  }

  @Override
  public int getItemCount() {
      return assignmentList.size();
  }

  public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
      CardView cardView;
      TextView tvAssignmentTitle;
      TextView tvAssignmentDescription;
      TextView tvDueDate;

      public AssignmentViewHolder(@NonNull View itemView) {
          super(itemView);
          cardView = itemView.findViewById(R.id.cardView);
          tvAssignmentTitle = itemView.findViewById(R.id.tvAssignmentTitle);
          tvAssignmentDescription = itemView.findViewById(R.id.tvAssignmentDescription);
          tvDueDate = itemView.findViewById(R.id.tvDueDate);
      }
  }
}
