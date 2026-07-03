package com.project.musicapp.features.consultant.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.musicapp.R;
import com.project.musicapp.core.models.TimeSlot;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private List<TimeSlot> timeSlots;
    private final TimeSlotActionListener actionListener;

    public interface TimeSlotActionListener {
        void onSlotEnableToggled(TimeSlot slot, boolean isChecked);
        void onSlotDelete(TimeSlot slot);
    }

    public TimeSlotAdapter(List<TimeSlot> timeSlots, TimeSlotActionListener listener) {
        this.timeSlots = timeSlots;
        this.actionListener = listener;
    }

    public void setSlots(List<TimeSlot> newTimeSlots) {
        this.timeSlots = newTimeSlots;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlot slot = timeSlots.get(position);
        holder.bind(slot);
    }

    @Override
    public int getItemCount() {
        return timeSlots != null ? timeSlots.size() : 0;
    }

    class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFromTime;
        private final TextView tvToTime;
        private final TextView tvDelete;
        private final SwitchCompat switchEnable;
        private final TextView tvEnableDisable;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFromTime = itemView.findViewById(R.id.tv_from_time);
            tvToTime = itemView.findViewById(R.id.tv_to_time);
            tvDelete = itemView.findViewById(R.id.tv_delete);
            switchEnable = itemView.findViewById(R.id.switch_enable);
            tvEnableDisable = itemView.findViewById(R.id.tv_enable_disable);
        }

        public void bind(TimeSlot slot) {
            if (slot == null) {
                itemView.setVisibility(View.GONE);
                return;
            }
            itemView.setVisibility(View.VISIBLE);

            tvFromTime.setText(slot.getFromTime() != null ? slot.getFromTime() : "Invalid Date");
            tvToTime.setText(slot.getToTime() != null ? slot.getToTime() : "");

            // ** THE CRASH FIX IS HERE **
            // 1. ALWAYS remove the listener before setting the UI state.
            // This prevents the listener from firing while the RecyclerView is binding.
            switchEnable.setOnCheckedChangeListener(null);

            // 2. Set the UI state from the data model.
            switchEnable.setChecked(slot.isEnabled());
            tvEnableDisable.setText(slot.isEnabled() ? "DISABLE" : "ENABLE");
            updateBackgroundColor(slot.isEnabled());

            // 3. NOW, re-attach the listener to handle user interactions.
            switchEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // This code is now guaranteed to only run when the user physically interacts with the switch.
                actionListener.onSlotEnableToggled(slot, isChecked);
                
                // Update the UI here for immediate responsiveness.
                tvEnableDisable.setText(isChecked ? "DISABLE" : "ENABLE");
                updateBackgroundColor(isChecked);
            });

            tvDelete.setOnClickListener(v -> {
                actionListener.onSlotDelete(slot);
            });
        }

        private void updateBackgroundColor(boolean isEnabled) {
            if (isEnabled) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.pink_light));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.pink_very_light));
            }
        }
    }
}
