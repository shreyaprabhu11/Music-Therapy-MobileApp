package com.project.musicapp.features.patient.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.musicapp.MainActivity;
import com.project.musicapp.R;
import com.project.musicapp.features.patient.adapters.NotificationAdapter;
import com.project.musicapp.core.models.Notification;
import com.project.musicapp.core.viewmodels.NotificationListViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private NotificationListViewModel viewModel;

    private static int DEMO_USER_ID ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        DEMO_USER_ID = sharedPref.getInt("USER_ID", -1); // -1 means not found

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(this);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(NotificationListViewModel.class);

        // Observe notifications
        viewModel.getAllNotifications().observe(this, new Observer<List<Notification>>() {
            @Override
            public void onChanged(List<Notification> notifications) {
                List<Notification> filtered = new ArrayList<>();
                for (Notification n : notifications) {
                    if (n.getReceiver() != null && n.getReceiver().getId() == DEMO_USER_ID) {
                        filtered.add(n);
                    }
                }

                filtered.sort(new Comparator<Notification>() {
                    @Override
                    public int compare(Notification n1, Notification n2) {
                        return Long.compare(n2.getSentTime(), n1.getSentTime());
                    }
                });
                adapter.setNotifications(filtered);
            }
        });

        // Refresh notifications from the service
        viewModel.refreshNotifications();

        findViewById(R.id.btn_notification_to_home).setOnClickListener(view -> {
            Intent intent = new Intent(NotificationActivity.this, MusicList.class);
            startActivity(intent);
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read if not already read
        if (!notification.isRead()) {
            viewModel.markAsRead(notification.getId());
        }

        // Open detail screen
        Intent intent = new Intent(this, NotificationDetailActivity.class);
        intent.putExtra("notification_id", notification.getId());
        startActivity(intent);
    }
}
