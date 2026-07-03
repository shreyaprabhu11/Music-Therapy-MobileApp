package com.project.musicapp.features.Admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.project.musicapp.core.models.Feedback;
import com.project.musicapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Adapter for displaying Feedback items in a ListView.
 * ✅ NOW SUPPORTS LiveData<List<Feedback>> - automatically updates when data changes
 */
public class FeedbackCustomAdapter extends ArrayAdapter<Feedback> {

    private Context context;
    private List<Feedback> feedbackList;

    /**
     * ✅ NEW: Constructor that accepts LiveData and observes changes
     */
    public FeedbackCustomAdapter(@NonNull Context context, LiveData<List<Feedback>> liveDataFeedbackList) {
        super(context, 0, new ArrayList<>());
        this.context = context;
        this.feedbackList = new ArrayList<>();

        // ✅ Observe LiveData and update adapter when data changes
        if (context instanceof LifecycleOwner) {
            liveDataFeedbackList.observe((LifecycleOwner) context, new Observer<List<Feedback>>() {
                @Override
                public void onChanged(List<Feedback> feedbacks) {
                    if (feedbacks != null) {
                        updateData(feedbacks);
                    }
                }
            });
        }
    }

    /**
     * ✅ LEGACY: Constructor that accepts regular List (for backward compatibility)
     */
    public FeedbackCustomAdapter(@NonNull Context context, List<Feedback> feedbackList) {
        super(context, 0, feedbackList);
        this.context = context;
        this.feedbackList = feedbackList != null ? feedbackList : new ArrayList<>();
    }

    /**
     * ✅ Update adapter data and notify changes
     */
    public void updateData(List<Feedback> newFeedbackList) {
        this.feedbackList.clear();
        if (newFeedbackList != null) {
            this.feedbackList.addAll(newFeedbackList);
        }
        clear();
        addAll(this.feedbackList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return feedbackList != null ? feedbackList.size() : 0;
    }

    @Override
    public Feedback getItem(int position) {
        return feedbackList != null && position < feedbackList.size() ? feedbackList.get(position) : null;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // ViewHolder pattern: Caches view lookups for faster performance.
        ViewHolder holder;

        if (convertView == null) {
            // A new view must be inflated.
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_chat, parent, false);

            // Create a new ViewHolder to hold references to the views.
            holder = new ViewHolder();
            holder.userName = convertView.findViewById(R.id.user_name);
            holder.userEmail = convertView.findViewById(R.id.user_email);
            holder.messageSnippet = convertView.findViewById(R.id.message_snippet);
            holder.timestamp = convertView.findViewById(R.id.timestamp);

            // Store the holder with the view.
            convertView.setTag(holder);
        } else {
            // A view is being recycled, retrieve the ViewHolder from the tag.
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data object for this position
        Feedback currentFeedback = getItem(position);

        // Populate the views with the data from the current feedback object
        if (currentFeedback != null) {
            holder.userName.setText(currentFeedback.getName() != null ? currentFeedback.getName() : "Unknown");
            holder.userEmail.setText(currentFeedback.getEmail() != null ? currentFeedback.getEmail() : "");
            holder.messageSnippet.setText(currentFeedback.getMessage() != null ? currentFeedback.getMessage() : "");
            holder.timestamp.setText(currentFeedback.getTime() != null ? currentFeedback.getTime() : "");
        }

        // Return the completed view to render on screen
        return convertView;
    }

    /**
     * The ViewHolder class holds the child views for one list item.
     * This avoids repeated calls to findViewById(), making the list scroll smoothly.
     */
    private static class ViewHolder {
        TextView userName;
        TextView userEmail;
        TextView messageSnippet;
        TextView timestamp;
    }
}
