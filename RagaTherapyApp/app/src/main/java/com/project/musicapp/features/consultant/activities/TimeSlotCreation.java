package com.project.musicapp.features.consultant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.musicapp.R;
import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.models.TimeSlot;
import com.project.musicapp.core.viewmodels.ConsultantViewModel;
import com.project.musicapp.features.consultant.adapters.TimeSlotAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeSlotCreation extends AppCompatActivity implements TimeSlotAdapter.TimeSlotActionListener {

    private static final int REQUEST_CODE_CREATE_SLOT = 101;
    private static final String TAG = "TimeSlotCreation";

    private Button btnSelectHoliday;
    private Button btnAddTimeSlot;
    private TextView timeSlotStatus;

    private RecyclerView timeSlotsRecyclerView;
    private TimeSlotAdapter timeSlotAdapter;

    // TODO: Replace with actual logged-in user ID mechanism
    private int userId = 1;

    private ConsultantViewModel consultantViewModel;
    // Removed TimeSlotViewModel as its usage conflicts with the Consultant model structure
    private Consultant currentConsultant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_slot_creation);

        btnSelectHoliday = findViewById(R.id.btn_select_holiday);
        btnAddTimeSlot = findViewById(R.id.btn_add_time_slot);
        timeSlotStatus = findViewById(R.id.text_no_time_slot);

        timeSlotsRecyclerView = findViewById(R.id.recycler_time_slots);
        timeSlotsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        timeSlotAdapter = new TimeSlotAdapter(new ArrayList<>(), this);
        timeSlotsRecyclerView.setAdapter(timeSlotAdapter);

        consultantViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ConsultantViewModel.class);

        // Observer watches for data changes on the list of consultants
        consultantViewModel.getConsultants().observe(this, consultants -> {
            Log.d(TAG, "Consultants LiveData updated. Size: " + (consultants != null ? consultants.size() : 0));

            // Find the current consultant by user ID
            currentConsultant = consultants.stream()
                    .filter(c -> c.getUser() != null && c.getUser().getId() == userId)
                    .findFirst()
                    .orElse(null);

            if (currentConsultant != null) {
                // Use the getter from the model
                List<TimeSlot> consultantSlots = currentConsultant.getTimeSlots();
                handleTimeSlotUpdate(consultantSlots);
            } else {
                Log.w(TAG, "Current Consultant (User ID: " + userId + ") not found.");
                handleTimeSlotUpdate(Collections.emptyList());
            }
        });

        btnSelectHoliday.setOnClickListener(v -> {
            if (currentConsultant == null) {
                Toast.makeText(this, "Consultant data not loaded yet.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(TimeSlotCreation.this, HolidaySelectionActivity.class);
            // Use the getter from the model
            intent.putExtra(HolidaySelectionActivity.EXTRA_CONSULTANT_ID, currentConsultant.getConsultantId());
            startActivity(intent);
        });

        // Use startActivityForResult to know when to refresh data
        btnAddTimeSlot.setOnClickListener(v -> {
            Intent intent = new Intent(TimeSlotCreation.this, CreateTimeSlotActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CREATE_SLOT);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CREATE_SLOT && resultCode == RESULT_OK) {
            Log.d(TAG, "New slot created. Forcing consultant list refresh.");
            // Call the refresh method on the ViewModel to re-trigger the LiveData observer
            consultantViewModel.refreshConsultants();
        }
    }

    private void handleTimeSlotUpdate(List<TimeSlot> slots) {
        // Ensure slots is a mutable list to avoid issues with adapter manipulation
        List<TimeSlot> finalSlots = (slots != null) ? new ArrayList<>(slots) : new ArrayList<>();
        timeSlotAdapter.setSlots(finalSlots);

        if (finalSlots.isEmpty()) {
            timeSlotsRecyclerView.setVisibility(View.GONE);
            timeSlotStatus.setVisibility(View.VISIBLE);
        } else {
            timeSlotsRecyclerView.setVisibility(View.VISIBLE);
            timeSlotStatus.setVisibility(View.GONE);
        }
    }

    // --- TimeSlotAdapter.TimeSlotActionListener Implementation ---

    @Override
    public void onSlotEnableToggled(TimeSlot slot, boolean isChecked) {
        if (currentConsultant == null) return;

        // 1. Update the local model object
        slot.setEnabled(isChecked);

        // 2. The TimeSlot is part of the Consultant; update the entire Consultant object.
        // The list is already updated because 'slot' is a reference to an object in the list.
        consultantViewModel.updateConsultant(
                currentConsultant.getConsultantId(),
                currentConsultant
        );

        Toast.makeText(this,
                isChecked ? "Time slot enabled: " + slot.getFromTime() : "Time slot disabled: " + slot.getFromTime(),
                Toast.LENGTH_SHORT).show();

        // The LiveData observer will trigger refresh, but a manual notify is faster for responsiveness
        timeSlotAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSlotDelete(TimeSlot slot) {
        if (currentConsultant == null) return;

        List<TimeSlot> currentSlots = currentConsultant.getTimeSlots();
        if (currentSlots != null) {
            // 1. Find and remove the slot from the consultant's list
            currentSlots.remove(slot);

            // 2. Update the Consultant object in Firebase
            consultantViewModel.updateConsultant(
                    currentConsultant.getConsultantId(),
                    currentConsultant
            );

            // 3. Update the UI directly for immediate feedback
            handleTimeSlotUpdate(currentSlots);

            Toast.makeText(this,
                    "Time slot deleted: " + slot.getFromTime(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}