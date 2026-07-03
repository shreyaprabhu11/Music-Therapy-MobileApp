package com.project.musicapp.features.consultant.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.project.musicapp.R;
import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.models.Holiday;
import com.project.musicapp.core.models.TimeSlot;
import com.project.musicapp.core.viewmodels.ConsultantViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreateTimeSlotActivity extends AppCompatActivity {

    private static final String TAG = "CreateTimeSlotActivity";

    private Button btnCreateTimeSlot;
    private TextView tvDate;
    private TextView tvFromTime;
    private TextView tvToTime;

    private Calendar selectedDate;
    private Calendar selectedFromTime;
    private Calendar selectedToTime;

    private ConsultantViewModel consultantViewModel;
    private final String DEFAULT_DATE_PLACEHOLDER = "DD-MM-YYYY";
    private String DEFAULT_TIME_PLACEHOLDER;

    private int userId = 1; // TODO: Replace with actual logged-in user ID
    private Consultant currentConsultant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_time_slot);

        consultantViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ConsultantViewModel.class);
        DEFAULT_TIME_PLACEHOLDER = getString(R.string.hh_mm_ampm);

        btnCreateTimeSlot = findViewById(R.id.btn_create_time_slot);
        tvDate = findViewById(R.id.tv_date);
        tvFromTime = findViewById(R.id.tv_from_time);
        tvToTime = findViewById(R.id.tv_to_time);

        tvDate.setText(DEFAULT_DATE_PLACEHOLDER);
        tvFromTime.setText(DEFAULT_TIME_PLACEHOLDER);
        tvToTime.setText(DEFAULT_TIME_PLACEHOLDER);

        consultantViewModel.getConsultants().observe(this, consultants -> {
            if (consultants != null) {
                currentConsultant = consultants.stream()
                        .filter(c -> c.getUser() != null && c.getUser().getId() == userId)
                        .findFirst()
                        .orElse(null);
                if (currentConsultant == null) {
                    Log.w(TAG, "Current consultant not found for user ID: " + userId);
                }
            }
        });

        tvDate.setOnClickListener(v -> showDatePicker());
        tvFromTime.setOnClickListener(v -> showTimePicker(tvFromTime, true));
        tvToTime.setOnClickListener(v -> showTimePicker(tvToTime, false));

        btnCreateTimeSlot.setOnClickListener(v -> handleCreateTimeSlot());
    }

    private void handleCreateTimeSlot() {
        if (currentConsultant == null) {
            Toast.makeText(this, "Consultant data not loaded. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null || selectedFromTime == null || selectedToTime == null) {
            Toast.makeText(this, "Please select the date, FROM time, and TO time.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!selectedToTime.after(selectedFromTime)) {
            Toast.makeText(this, "The TO time must be later than the FROM time.", Toast.LENGTH_LONG).show();
            return;
        }

        if (isDateOnHoliday(selectedDate)) {
            Toast.makeText(this, "The selected date is a holiday. Please choose a different day.", Toast.LENGTH_LONG).show();
            return;
        }

        String dateStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate.getTime());
        String fromTimeStr = tvFromTime.getText().toString();
        String toTimeStr = tvToTime.getText().toString();

        TimeSlot newTimeSlot = new TimeSlot();
        newTimeSlot.setSlotId(UUID.randomUUID().toString());
        newTimeSlot.setFromTime(dateStr + " " + fromTimeStr);
        newTimeSlot.setToTime(dateStr + " " + toTimeStr);
        newTimeSlot.setLocation("Virtual");
        newTimeSlot.setEnabled(true);

        List<TimeSlot> timeSlots = currentConsultant.getTimeSlots() != null ? new ArrayList<>(currentConsultant.getTimeSlots()) : new ArrayList<>();
        timeSlots.add(newTimeSlot);
        currentConsultant.setTimeSlots(timeSlots);

        consultantViewModel.updateConsultant(currentConsultant.getConsultantId(), currentConsultant);

        showSuccessDialog();
    }

    private boolean isDateOnHoliday(Calendar date) {
        if (currentConsultant.getHolidays() == null || currentConsultant.getHolidays().isEmpty()) {
            return false;
        }

        // ** THE FIX IS HERE **
        // Filter out any null Holiday objects before processing to prevent crashes.
        List<Holiday.DayOfWeek> holidayDays = currentConsultant.getHolidays().stream()
                .filter(Objects::nonNull)
                .map(Holiday::getDay)
                .collect(Collectors.toList());

        int dayOfWeekInt = date.get(Calendar.DAY_OF_WEEK);
        Holiday.DayOfWeek dayOfWeekEnum = mapIntToDayOfWeek(dayOfWeekInt);

        return holidayDays.contains(dayOfWeekEnum);
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
    
    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    String selectedDateStr = String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year);
                    tvDate.setText(selectedDateStr);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(final TextView timeTextView, final boolean isFromTime) {
        final Calendar c = Calendar.getInstance();
        int initialHour = c.get(Calendar.HOUR_OF_DAY);
        int initialMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);

                    if (isFromTime) {
                        selectedFromTime = selectedTime;
                    } else {
                        selectedToTime = selectedTime;
                    }

                    String amPm = (hourOfDay < 12) ? "AM" : "PM";
                    int displayHour = (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12;

                    String selectedTimeStr = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
                    timeTextView.setText(selectedTimeStr);
                },
                initialHour,
                initialMinute,
                false // 12-hour format
        );
        timePickerDialog.show();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Time Slot Created")
                .setMessage("The time slot has been successfully saved.")
                .setPositiveButton("OK", (dialog, id) -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}