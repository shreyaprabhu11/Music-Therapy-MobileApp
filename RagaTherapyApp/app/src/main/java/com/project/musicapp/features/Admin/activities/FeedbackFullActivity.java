package com.project.musicapp.features.Admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.musicapp.R;
import com.project.musicapp.core.models.Feedback;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.FeedbackService;

/**
 * Activity to display full feedback details
 * ✅ Fully integrated with async Firebase services
 */
public class FeedbackFullActivity extends AppCompatActivity {

    // UI Elements for displaying feedback
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView messageTextView;
    private TextView timeTextView;

    // Navigation buttons
    private FrameLayout personButton;
    private FrameLayout addButton;
    private FrameLayout musicButton;
    private FrameLayout consultantButton;
    private ImageView backArrow;

    // Service and data
    private FeedbackService feedbackService;
    private int feedbackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.feedback_full);

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get feedback ID from intent
        Intent intent = getIntent();
        feedbackId = intent.getIntExtra("ID", -1);

        if (feedbackId == -1) {
            Toast.makeText(this, "Invalid feedback ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize service
        feedbackService = FeedbackService.getInstance();

        // Initialize UI elements
        initializeViews();

        // Load and display feedback data
        loadFeedbackData();

        // Setup navigation buttons
        setupNavigationButtons();
    }

    /**
     * Initialize all UI elements
     */
    private void initializeViews() {
        // Feedback display elements (update IDs to match your XML)
        nameTextView = findViewById(R.id.sender_name);
        emailTextView = findViewById(R.id.sender_email);
        messageTextView = findViewById(R.id.message_bubble);
        timeTextView = findViewById(R.id.timestamp);

        // Navigation buttons
        personButton = findViewById(R.id.nav_person_button);
        addButton = findViewById(R.id.nav_add_button);
        musicButton = findViewById(R.id.nav_music_button);
        consultantButton = findViewById(R.id.nav_group_button);
        backArrow = findViewById(R.id.back_arrow);
    }

    /**
     * ✅ Load feedback data using SingleItemCallback
     * Uses the specialized fetchFeedbackById() method that returns a single Feedback object
     */
    private void loadFeedbackData() {
        feedbackService.fetchFeedbackById(feedbackId, new BaseService.SingleItemCallback<Feedback>() {
            @Override
            public void onSuccess(Feedback feedback) {
                if (feedback != null) {
                    displayFeedback(feedback);
                } else {
                    Toast.makeText(FeedbackFullActivity.this,
                            "Feedback not found",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(FeedbackFullActivity.this,
                        "Error loading feedback: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Display feedback data in UI with null safety
     */
    private void displayFeedback(Feedback feedback) {
        if (feedback != null) {
            nameTextView.setText(feedback.getName() != null ? feedback.getName() : "Unknown");
            emailTextView.setText(feedback.getEmail() != null ? feedback.getEmail() : "No email");
            messageTextView.setText(feedback.getMessage() != null ? feedback.getMessage() : "No message");
            timeTextView.setText(feedback.getTime() != null ? feedback.getTime() : "");
        }
    }

    /**
     * Setup all navigation buttons
     */
    private void setupNavigationButtons() {
        // Navigate to User List
        personButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FeedbackFullActivity.this, UserListActivity.class);
                startActivity(i);
            }
        });

        // Add User or Song dialog
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        // Navigate to Music List
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FeedbackFullActivity.this, MusicListActivity.class);
                startActivity(i);
            }
        });

        // Navigate to Consultant List
        consultantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FeedbackFullActivity.this, ConsultantListActivity.class);
                startActivity(i);
            }
        });

        // Back to Feedback List
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedbackFullActivity.this, FeedBackActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    /**
     * Show dialog to choose between adding user or song
     */
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FeedbackFullActivity.this);
        builder.setTitle("Choose an Action");
        builder.setMessage("What would you like to do?");

        builder.setPositiveButton("Add User", (dialog, which) -> {
            Intent i = new Intent(FeedbackFullActivity.this, FormActivityPart1.class);
            startActivity(i);
        });

        builder.setNegativeButton("Add Song", (dialog, which) -> {
            Intent i = new Intent(FeedbackFullActivity.this, UploadSongScreenActivity.class);
            startActivity(i);
        });

        builder.create().show();
    }

    public void onBackPressedDispatcher() {
        super.onBackPressed();
        Intent intent = new Intent(FeedbackFullActivity.this, FeedBackActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
