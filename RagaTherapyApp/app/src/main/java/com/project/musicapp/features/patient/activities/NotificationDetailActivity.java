package com.project.musicapp.features.patient.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.project.musicapp.MainActivity;
import com.project.musicapp.R;
import com.project.musicapp.core.models.Notification;
import com.project.musicapp.core.models.Appointment;
import com.project.musicapp.core.viewmodels.NotificationListViewModel;

public class NotificationDetailActivity extends AppCompatActivity {

    private TextView titleText, messageText, linkText;
    private NotificationListViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        titleText = findViewById(R.id.tv_title_notification);
        messageText = findViewById(R.id.textDetailMessage);
        linkText = findViewById(R.id.textDetailLink);

        viewModel = new ViewModelProvider(this).get(NotificationListViewModel.class);

        int notificationId = getIntent().getIntExtra("notification_id", -1);
        if (notificationId != -1) {
            viewModel.getNotificationByIdAsync(notificationId, notification -> {
                runOnUiThread(() -> {
                    if (notification != null) {
                        titleText.setText(notification.getTitle());

                        StringBuilder messageBuilder = new StringBuilder();
                        messageBuilder.append(notification.getMessage());

                        Appointment appointment = notification.getAppointment();
                        if (appointment != null) {
                            if (appointment.getTimeSlot() != null) {
                                messageBuilder.append("\n\n")
                                        .append("Time: ")
                                        .append(appointment.getTimeSlot().getFromTime())
                                        .append(" - ")
                                        .append(appointment.getTimeSlot().getToTime());
                            }

                            if (appointment.getConsultant() != null) {
                                messageBuilder.append("\n")
                                        .append("Consultant: ")
                                        .append(appointment.getConsultant().getUser().getName());
                            }

                            if (appointment.getPatientProfile() != null) {
                                messageBuilder.append("\n")
                                        .append("Patient: ")
                                        .append(appointment.getPatientProfile().getUser().getName());
                            }
                        }

                        messageText.setText(messageBuilder.toString());

                        if (appointment != null && appointment.getMeetLink() != null && !appointment.getMeetLink().isEmpty()) {
                            linkText.setVisibility(View.VISIBLE);
                            linkText.setText("Join Appointment");
                            linkText.setTextColor(getResources().getColor(R.color.link));
                            linkText.setPaintFlags(linkText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            linkText.setOnClickListener(v -> {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appointment.getMeetLink())));
                                } catch (Exception e) {
                                    Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            linkText.setVisibility(View.GONE);
                        }

                        // Mark as read asynchronously
                        viewModel.markAsReadAsync(notificationId);
                    }
                });
            });
        }



        findViewById(R.id.btn_notification_to_notification_list).setOnClickListener(view -> {
            Intent intent = new Intent(NotificationDetailActivity.this, NotificationActivity.class);
            startActivity(intent);
        });
    }
}
