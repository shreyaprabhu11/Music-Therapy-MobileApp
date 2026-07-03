package com.project.musicapp.features.consultant.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.project.musicapp.R;
import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.models.Holiday;
import com.project.musicapp.core.models.TimeSlot;
import com.project.musicapp.core.viewmodels.ConsultantViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class HolidaySelectionActivity extends AppCompatActivity {

    public static final String EXTRA_CONSULTANT_ID = "consultant_id";
    private static final String TAG = "HolidaySelection";
    private int consultantId;

    private Button btnSet;
    private Button btnReset;
    private List<CheckBox> dayCheckBoxes;

    private ConsultantViewModel consultantViewModel;
    private Consultant currentConsultant;

    private Holiday.DayOfWeek mapCheckBoxIdToDayOfWeek(int id) {
        if (id == R.id.cb_sunday) return Holiday.DayOfWeek.SUN;
        if (id == R.id.cb_monday) return Holiday.DayOfWeek.MON;
        if (id == R.id.cb_tuesday) return Holiday.DayOfWeek.TUE;
        if (id == R.id.cb_wednesday) return Holiday.DayOfWeek.WED;
        if (id == R.id.cb_thursday) return Holiday.DayOfWeek.THU;
        if (id == R.id.cb_friday) return Holiday.DayOfWeek.FRI;
        if (id == R.id.cb_saturday) return Holiday.DayOfWeek.SAT;
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holiday_selection);

        consultantViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ConsultantViewModel.class);

        consultantId = getIntent().getIntExtra(EXTRA_CONSULTANT_ID, -1);
        if (consultantId == -1) {
            Toast.makeText(this, "Error: Consultant ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnSet = findViewById(R.id.btn_set);
        btnReset = findViewById(R.id.btn_reset);

        dayCheckBoxes = Arrays.asList(
                findViewById(R.id.cb_sunday),
                findViewById(R.id.cb_monday),
                findViewById(R.id.cb_tuesday),
                findViewById(R.id.cb_wednesday),
                findViewById(R.id.cb_thursday),
                findViewById(R.id.cb_friday),
                findViewById(R.id.cb_saturday)
        );

        consultantViewModel.getConsultants().observe(this, consultants -> {
            if (consultants != null) {
                currentConsultant = consultants.stream()
                        .filter(c -> c.getConsultantId() == consultantId)
                        .findFirst()
                        .orElse(null);

                if (currentConsultant != null) {
                    loadExistingHolidays();
                } else {
                    Log.e(TAG, "Consultant with ID " + consultantId + " not found in the list.");
                    Toast.makeText(this, "Could not load consultant data.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSet.setOnClickListener(v -> handleSet());
        btnReset.setOnClickListener(v -> handleReset());
    }

    private void loadExistingHolidays() {
        if (currentConsultant != null && currentConsultant.getHolidays() != null) {
            List<Holiday.DayOfWeek> holidayDays = currentConsultant.getHolidays().stream()
                    .filter(Objects::nonNull)
                    .map(Holiday::getDay)
                    .collect(Collectors.toList());

            for (CheckBox cb : dayCheckBoxes) {
                if(cb == null) continue;
                Holiday.DayOfWeek day = mapCheckBoxIdToDayOfWeek(cb.getId());
                cb.setChecked(day != null && holidayDays.contains(day));
            }
        }
    }

    private void handleSet() {
        if (currentConsultant == null) {
            Toast.makeText(this, "Consultant data not loaded. Please wait.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Holiday> holidaysToSet = new ArrayList<>();
        List<Holiday.DayOfWeek> selectedDays = new ArrayList<>();
        int holidayIdCounter = 0;

        for (CheckBox cb : dayCheckBoxes) {
            if (cb != null && cb.isChecked()) {
                Holiday.DayOfWeek day = mapCheckBoxIdToDayOfWeek(cb.getId());
                if (day != null) {
                    Holiday holiday = new Holiday();
                    holiday.setId(++holidayIdCounter);
                    holiday.setConsultantId(consultantId);
                    holiday.setDay(day);
                    holidaysToSet.add(holiday);
                    selectedDays.add(day);
                }
            }
        }

        disableTimeSlotsOnHolidays(selectedDays);
        
        currentConsultant.setHolidays(holidaysToSet);
        consultantViewModel.updateConsultant(consultantId, currentConsultant);

        if (!holidaysToSet.isEmpty()) {
            Toast.makeText(this, "Holidays set and conflicting slots disabled.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "All recurring holidays removed.", Toast.LENGTH_LONG).show();
        }
        
        showSuccessDialog();
    }

    private void disableTimeSlotsOnHolidays(List<Holiday.DayOfWeek> holidayDays) {
        if (currentConsultant.getTimeSlots() == null || holidayDays == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar slotCal = Calendar.getInstance();

        for (TimeSlot slot : currentConsultant.getTimeSlots()) {
            if (slot == null || slot.getFromTime() == null || slot.getFromTime().trim().isEmpty() || !slot.getFromTime().contains(" ")) {
                Log.w(TAG, "Skipping malformed or null time slot during disable.");
                continue;
            }

            try {
                String dateStr = slot.getFromTime().split(" ")[0];
                slotCal.setTime(sdf.parse(dateStr));
                int dayOfWeekInt = slotCal.get(Calendar.DAY_OF_WEEK);
                Holiday.DayOfWeek dayOfWeekEnum = mapIntToDayOfWeek(dayOfWeekInt);

                if (holidayDays.contains(dayOfWeekEnum)) {
                    slot.setEnabled(false);
                }
            } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Error processing time slot during disable: " + slot.getSlotId(), e);
            }
        }
    }

    private void handleReset() {
        if (currentConsultant == null) {
            Toast.makeText(this, "Consultant data not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        enableTimeSlotsOnClearedHolidays();

        currentConsultant.setHolidays(new ArrayList<>());
        consultantViewModel.updateConsultant(consultantId, currentConsultant);
        
        for (CheckBox cb : dayCheckBoxes) {
            if(cb != null) {
                cb.setChecked(false);
            }
        }
        
        Toast.makeText(this, "Selection reset. All holidays removed and slots re-enabled.", Toast.LENGTH_LONG).show();
    }

    private void enableTimeSlotsOnClearedHolidays() {
        if (currentConsultant.getTimeSlots() == null || currentConsultant.getHolidays() == null) {
            return;
        }

        List<Holiday.DayOfWeek> clearedHolidayDays = currentConsultant.getHolidays().stream()
                .filter(Objects::nonNull)
                .map(Holiday::getDay)
                .collect(Collectors.toList());

        if(clearedHolidayDays.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar slotCal = Calendar.getInstance();

        for (TimeSlot slot : currentConsultant.getTimeSlots()) {
            if (slot == null || slot.getFromTime() == null || slot.getFromTime().trim().isEmpty() || !slot.getFromTime().contains(" ")) {
                Log.w(TAG, "Skipping malformed or null time slot during re-enable.");
                continue;
            }

            try {
                String dateStr = slot.getFromTime().split(" ")[0];
                slotCal.setTime(sdf.parse(dateStr));
                int dayOfWeekInt = slotCal.get(Calendar.DAY_OF_WEEK);
                Holiday.DayOfWeek dayOfWeekEnum = mapIntToDayOfWeek(dayOfWeekInt);

                if (clearedHolidayDays.contains(dayOfWeekEnum)) {
                    slot.setEnabled(true);
                }
            } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Error processing time slot for re-enabling: " + slot.getSlotId(), e);
            }
        }
    }

    private Holiday.DayOfWeek mapIntToDayOfWeek(int calendarDay) {
        switch (calendarDay) {
            case Calendar.SUNDAY: return Holiday.DayOfWeek.SUN;
            case Calendar.MONDAY: return Holiday.DayOfWeek.MON;
            case Calendar.TUESDAY: return Holiday.DayOfWeek.TUE;
            case Calendar.WEDNESDAY: return Holiday.DayOfWeek.WED;
            case Calendar.THURSDAY: return Holiday.DayOfWeek.THU;
            case Calendar.FRIDAY: return Holiday.DayOfWeek.FRI;
            case Calendar.SATURDAY: return Holiday.DayOfWeek.SAT;
            default: return null;
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Your holiday preferences have been saved.")
                .setPositiveButton("OK", (dialog, id) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}