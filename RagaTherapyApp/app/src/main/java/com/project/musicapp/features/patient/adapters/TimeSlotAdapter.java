package com.project.musicapp.features.patient.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.musicapp.R;
import com.project.musicapp.core.models.TimeSlot;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private List<TimeSlot> timeSlots;
    private int selectedPosition = -1; // for single selection
    private OnSlotSelectedListener listener;

    public interface OnSlotSelectedListener {
        void onSlotSelected(TimeSlot selectedSlot);
    }

    public TimeSlotAdapter(List<TimeSlot> timeSlots, OnSlotSelectedListener listener) {
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.time_slot_item, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlot slot = timeSlots.get(position);
        holder.tvSlotTime.setText(slot.getFromTime() + " - " + slot.getToTime());

        // Use selectedPosition to check if this RadioButton should be checked
        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return; // Safety check

            selectedPosition = adapterPos;
            notifyDataSetChanged();

            TimeSlot selectedSlot = timeSlots.get(adapterPos);
            listener.onSlotSelected(selectedSlot);
        });
    }


    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView tvSlotTime;
        RadioButton radioButton;

        TimeSlotViewHolder(View itemView) {
            super(itemView);
            tvSlotTime = itemView.findViewById(R.id.tvSlotTime);
            radioButton = itemView.findViewById(R.id.rbSelectSlot);
        }
    }
}
