package com.project.musicapp.features.patient.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.project.musicapp.MainActivity;
import com.project.musicapp.R;
import com.project.musicapp.core.models.Notification;
import com.project.musicapp.core.models.User;
import com.project.musicapp.core.viewmodels.MusicListViewModel;
import com.project.musicapp.core.viewmodels.NotificationListViewModel;
import com.project.musicapp.core.viewmodels.UserViewModel;
import com.project.musicapp.features.patient.adapters.MusicRecyclerViewAdapter;
import com.project.musicapp.features.settings.activities.ResetPasswordActivity;
import com.project.musicapp.features.settings.activities.SettingActivity;

import java.util.ArrayList;

/**
 * MusicList Activity
 * Features:
 * - First login password change prompt
 * - Notification permission request
 * - Modern Material dialogs
 * - Clean modular structure
 */
public class MusicList extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_NOTIF_REQUESTED = "notif_requested";

    private MusicListViewModel musicViewModel;
    private MusicRecyclerViewAdapter adapter;
    private UserViewModel userViewModel;
    private NotificationListViewModel notificationViewModel;
    private View notificationDot;

    // -----------------------------
    // HIGHLIGHT: Keep userId as 7
    // -----------------------------
    private int userId; // logged-in user ID

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", -1); // -1 means not found


        if(userId==-1){
            Intent i=new Intent(MusicList.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        }

        initializeNotificationPermission();
        handleFirstLoginPasswordPrompt(); // check if first login
        setupBottomNavigation();
        setupRecyclerView();
        setupSearchView();
        setupNotificationObserver();
        setupProfileButton();
    }

    // -----------------------------
    // Notification Permission
    // -----------------------------
    private void initializeNotificationPermission() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Optional: show welcome notification or log
                    } else {
                        Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean alreadyRequested = prefs.getBoolean(KEY_NOTIF_REQUESTED, false);

            if (!alreadyRequested) {
                prefs.edit().putBoolean(KEY_NOTIF_REQUESTED, true).apply();
                requestNotificationPermissionWithRationale();
            }
        }
    }

    private void requestNotificationPermissionWithRationale() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Enable Notifications")
                    .setMessage("We would like to send you notifications for appointments and important updates.")
                    .setCancelable(false)
                    .setPositiveButton("Allow", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                    .setNegativeButton("Not Now", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    // -----------------------------
    // First Login Password Prompt
    // -----------------------------
    // -----------------------------
// First Login Password Prompt with Logging
// -----------------------------
    private void handleFirstLoginPasswordPrompt() {
        userViewModel = new UserViewModel();

        final int currentUserId = userId; // 7
        final User[] currentUser = { null }; // array workaround

        userViewModel.getAllUsers().observe(this, users -> {
            if (users == null || users.isEmpty()) {
                Log.e("FirstLoginCheck", "No users loaded yet.");
                return;
            }

            for (User u : users) {
                if (u.getId() == currentUserId) {
                    currentUser[0] = u; // assign to array element
                    break;
                }
            }

            if (currentUser[0] == null) {
                Log.e("FirstLoginCheck", "User with ID " + currentUserId + " not found in LiveData!");
                return;
            }

            Log.d("FirstLoginCheck", "User fetched: " + currentUser[0].getName() + ", isFirstLogin: " + currentUser[0].isFirstLogin());

            if (currentUser[0].isFirstLogin()) {
                new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                        .setTitle("Security Notice")
                        .setMessage("Since this is your first login, please change your password for better security.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> {
                            startActivity(new Intent(this, ResetPasswordActivity.class));

                            userViewModel.setFirstLogin(currentUser[0].getId(), false);
                            dialog.dismiss();
                        })
                        .show();
            }
        });


        // Force refresh to trigger LiveData if needed
        userViewModel.refreshUsers();
    }



    // -----------------------------
    // Bottom Navigation
    // -----------------------------
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_music);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_music) return true;
            if (itemId == R.id.nav_consultant) {
                startActivity(new Intent(this, ConsultantListActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    // -----------------------------
    // RecyclerView Setup
    // -----------------------------
    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyc_music_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MusicRecyclerViewAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        musicViewModel = new ViewModelProvider(this).get(MusicListViewModel.class);
        musicViewModel.getAllMusic().observe(this, musics -> {
            if (musics != null) {
                adapter.setMusicList(new ArrayList<>(musics));
            }
        });
    }

    // -----------------------------
    // Search Functionality
    // -----------------------------
    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.tv_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filterList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterList(newText);
                return false;
            }
        });
    }

    // -----------------------------
    // Notification Dot & Observers
    // -----------------------------
    private void setupNotificationObserver() {
        notificationDot = findViewById(R.id.notification_dot);
        notificationViewModel = new ViewModelProvider(this).get(NotificationListViewModel.class);

        // -----------------------------
        // HIGHLIGHT: Using userId = 7
        // -----------------------------
        int currentUserId = 7;

        notificationViewModel.getAllNotifications().observe(this, notifications -> {
            if (notifications == null) {
                Log.d("NotificationKL", "Observer received null list");
                notificationDot.setVisibility(View.GONE);
                return;
            }

            boolean hasUnread = false;

            for (Notification n : notifications) {
                if (n == null) continue;
                User receiver = n.getReceiver();
                if (receiver != null && receiver.getId() == currentUserId && !n.isRead()) {
                    hasUnread = true;
                    break;
                }
            }

            notificationDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
        });

        notificationViewModel.refreshNotifications();

        findViewById(R.id.notification).setOnClickListener(view ->
                startActivity(new Intent(MusicList.this, NotificationActivity.class))
        );
    }

    // -----------------------------
    // Profile Button
    // -----------------------------
    private void setupProfileButton() {
        findViewById(R.id.patient_profile).setOnClickListener(view ->
                startActivity(new Intent(MusicList.this, SettingActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationViewModel.refreshNotifications();
    }
}
