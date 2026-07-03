package com.project.musicapp.features.patient.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.project.musicapp.R;
import com.project.musicapp.core.models.Appointment;
import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.models.Notification;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.models.TimeSlot;
import com.project.musicapp.core.services.AppointmentService;
import com.project.musicapp.core.services.NotificationService;
import com.project.musicapp.core.services.PatientProfileService;
import com.project.musicapp.core.utils.NotificationUtils;
import com.project.musicapp.core.viewmodels.ConsultantViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConsultantRecyclerViewAdapter extends RecyclerView.Adapter<ConsultantRecyclerViewAdapter.ConsultantViewHolder> {

    private Context context;
    private List<Consultant> consultantList;

    public ConsultantRecyclerViewAdapter(Context context, List<Consultant> list) {
        this.context = context;
        this.consultantList = list;
    }

    @NonNull
    @Override
    public ConsultantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.consultant_recycle_view_row, parent, false);
        return new ConsultantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsultantViewHolder holder, int position) {
        Consultant consultant = consultantList.get(position);

        holder.name.setText(consultant.getUser().getName());
        holder.job.setText(consultant.getDesignation());
        holder.location.setText(consultant.getQualifications());
        Glide.with(context).load(consultant.getUser().getProfilePictureUrl()).into(holder.img);

        final TimeSlot[] selectedSlot = new TimeSlot[1];

        Runnable refreshSpinner = () -> {
            List<TimeSlot> enabledSlots = new ArrayList<>();
            for (TimeSlot slot : consultant.getTimeSlots()) {
                // ✅ Correct logging to see real enabled status
                Log.d("ConsultantAdapter", "SlotId: " + slot.getSlotId() +
                        ", From: " + slot.getFromTime() +
                        ", To: " + slot.getToTime() +
                        ", Enabled: " + slot.isEnabled() +
                        ", DefaultLink: " + slot.getDefaultMeetLink());

                if (slot.isEnabled()) {
                    enabledSlots.add(slot);
                }
            }

            List<String> slotLabels = new ArrayList<>();
            for (TimeSlot slot : enabledSlots) {
                slotLabels.add(slot.getFromTime() + " - " + slot.getToTime());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    slotLabels
            );
            holder.spinnerTimeslots.setAdapter(adapter);

            selectedSlot[0] = enabledSlots.isEmpty() ? null : enabledSlots.get(0);

            holder.spinnerTimeslots.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    selectedSlot[0] = (pos >= 0 && pos < enabledSlots.size()) ? enabledSlots.get(pos) : null;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedSlot[0] = null;
                }
            });
        };

        // --- Initial spinner setup ---
        refreshSpinner.run();

        // --- Book Now action ---
        holder.bookNow.setOnClickListener(v -> {
            if (selectedSlot[0] == null) {
                Toast.makeText(context, "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }

            String consultantName = consultant.getUser().getName();
            String slotTime = selectedSlot[0].getFromTime();

            new MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                    .setTitle("Confirm Appointment")
                    .setMessage("Do you want to book an appointment with "
                            + consultantName + " at " + slotTime + "?")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        PatientProfileService patientService = new PatientProfileService();

                        // ✅ fetchProfileById via callback
                        patientService.fetchProfileById(501, patient -> {
                            if (patient == null) {
                                Log.e("ConsultantAdapter", "❌ Patient with ID 501 not found!");
                                Toast.makeText(context, "Patient profile not found!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                return;
                            }

                            Appointment appointment = new Appointment(
                                    0,
                                    selectedSlot[0],
                                    patient,
                                    consultant,
                                    "www.google.com",
                                    System.currentTimeMillis()
                            );
                            new AppointmentService().addAppointment(appointment);

                            if (patient.getConsultants() == null)
                                patient.setConsultants(new ArrayList<>());

                            if (!patient.getConsultants().contains(consultant)) {
                                patient.getConsultants().add(consultant);
                                patientService.updateProfile(patient.getId(), patient);
                            }

                            Notification notification = new Notification(
                                    0,
                                    "Appointment Booked",
                                    "You have successfully booked an appointment with " +
                                            consultantName + " at " + slotTime,
                                    System.currentTimeMillis(),
                                    false,
                                    patient.getUser(),
                                    appointment
                            );
                            new NotificationService().addNotification(notification);

                            NotificationUtils.showNotification(
                                    context,
                                    "Appointment Booked",
                                    "Booked with " + consultantName + " at " + slotTime
                            );


                            // ✅ Disable booked slot and push update to Firebase safely
                            selectedSlot[0].setEnabled(false);
                            consultant.getTimeSlots()
                                    .replaceAll(slot -> slot.getSlotId() == selectedSlot[0].getSlotId() ? selectedSlot[0] : slot);

// Update the consultant in Firebase using ViewModel pattern (no new method)
                            ConsultantViewModel consultantVM = new ConsultantViewModel();
                            consultantVM.updateConsultant(consultant.getConsultantId(), consultant);

                            refreshSpinner.run();


                            Toast.makeText(context,
                                    "Booked with " + consultantName,
                                    Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }




    @Override
    public int getItemCount() {
        return consultantList.size();
    }

    static class ConsultantViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView img;
        TextView name, job, location;
        Spinner spinnerTimeslots;
        Button bookNow;

        public ConsultantViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.consultant_profile);
            name = itemView.findViewById(R.id.tv_consultant_name);
            job = itemView.findViewById(R.id.tv_consultant_job);
            location = itemView.findViewById(R.id.tv_consultant_location);
            spinnerTimeslots = itemView.findViewById(R.id.spinner_timeslots);
            bookNow = itemView.findViewById(R.id.btn_book_time_slot);
        }
    }
}

