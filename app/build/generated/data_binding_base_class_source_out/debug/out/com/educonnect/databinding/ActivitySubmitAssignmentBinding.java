// Generated by view binder compiler. Do not edit!
package com.educonnect.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.educonnect.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySubmitAssignmentBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button btnChooseFile;

  @NonNull
  public final Button btnSubmit;

  @NonNull
  public final EditText etComment;

  @NonNull
  public final ProgressBar progressBar;

  @NonNull
  public final Toolbar toolbar;

  @NonNull
  public final TextView tvAssignmentTitle;

  @NonNull
  public final TextView tvErrorMessage;

  @NonNull
  public final TextView tvFileName;

  private ActivitySubmitAssignmentBinding(@NonNull ConstraintLayout rootView,
      @NonNull Button btnChooseFile, @NonNull Button btnSubmit, @NonNull EditText etComment,
      @NonNull ProgressBar progressBar, @NonNull Toolbar toolbar,
      @NonNull TextView tvAssignmentTitle, @NonNull TextView tvErrorMessage,
      @NonNull TextView tvFileName) {
    this.rootView = rootView;
    this.btnChooseFile = btnChooseFile;
    this.btnSubmit = btnSubmit;
    this.etComment = etComment;
    this.progressBar = progressBar;
    this.toolbar = toolbar;
    this.tvAssignmentTitle = tvAssignmentTitle;
    this.tvErrorMessage = tvErrorMessage;
    this.tvFileName = tvFileName;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySubmitAssignmentBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySubmitAssignmentBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_submit_assignment, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySubmitAssignmentBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnChooseFile;
      Button btnChooseFile = ViewBindings.findChildViewById(rootView, id);
      if (btnChooseFile == null) {
        break missingId;
      }

      id = R.id.btnSubmit;
      Button btnSubmit = ViewBindings.findChildViewById(rootView, id);
      if (btnSubmit == null) {
        break missingId;
      }

      id = R.id.etComment;
      EditText etComment = ViewBindings.findChildViewById(rootView, id);
      if (etComment == null) {
        break missingId;
      }

      id = R.id.progressBar;
      ProgressBar progressBar = ViewBindings.findChildViewById(rootView, id);
      if (progressBar == null) {
        break missingId;
      }

      id = R.id.toolbar;
      Toolbar toolbar = ViewBindings.findChildViewById(rootView, id);
      if (toolbar == null) {
        break missingId;
      }

      id = R.id.tvAssignmentTitle;
      TextView tvAssignmentTitle = ViewBindings.findChildViewById(rootView, id);
      if (tvAssignmentTitle == null) {
        break missingId;
      }

      id = R.id.tvErrorMessage;
      TextView tvErrorMessage = ViewBindings.findChildViewById(rootView, id);
      if (tvErrorMessage == null) {
        break missingId;
      }

      id = R.id.tvFileName;
      TextView tvFileName = ViewBindings.findChildViewById(rootView, id);
      if (tvFileName == null) {
        break missingId;
      }

      return new ActivitySubmitAssignmentBinding((ConstraintLayout) rootView, btnChooseFile,
          btnSubmit, etComment, progressBar, toolbar, tvAssignmentTitle, tvErrorMessage,
          tvFileName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
