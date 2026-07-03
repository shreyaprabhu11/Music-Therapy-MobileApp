package com.project.musicapp.features.Admin.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.viewmodels.PatientProfileViewModel;
import com.project.musicapp.features.Admin.activities.ConsultantListActivity;
import com.project.musicapp.R;
import com.project.musicapp.features.Admin.adapters.AssignedConsultantAdapter;
import com.project.musicapp.core.models.Consultant;

import java.util.ArrayList;
import java.util.List;

/**
 * ✅ Updated ListViewClickActivity with async Firebase callbacks
 * Uses PatientProfileViewModel with proper callback handling
 */
public class ListViewClickActivity extends AppCompatActivity {

    private PatientProfileViewModel viewModel;
    private int userId;
    private PatientProfile patientProfile;

    // Views
    private TextView name;
    private TextView userIdTextView;
    private TextView phone;
    private TextView email;
    private TextView stageLabel;
    private TextView listeningTime;
    private ListView consultantsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.listview_click);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the intent that started this activity
        Intent intent = getIntent();

        // Retrieve the PATIENT_ID (as an int)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            patientProfile = (PatientProfile) intent.getSerializableExtra("PATIENT", PatientProfile.class);
        } else {
            patientProfile = (PatientProfile) intent.getSerializableExtra("PATIENT");
        }



        userId = patientProfile.getUser().getId();
        // Check if the userId was received successfully
        if (userId == -1) {
            Toast.makeText(this, "Error: No patient ID received", Toast.LENGTH_SHORT).show();
            finish(); // Close this activity and return
            return;
        }

        // ✅ Initialize ViewModel with singleton pattern
        viewModel = new PatientProfileViewModel();

        // Initialize views
        initializeViews();

        // ✅ Fetch patient profile asynchronously
        loadPatientProfile();
    }

    /**
     * Initialize all UI views
     */
    private void initializeViews() {
        // TextViews
        name = findViewById(R.id.name_Surname);
        userIdTextView = findViewById(R.id.user_id);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        stageLabel = findViewById(R.id.stage_label);
        listeningTime = findViewById(R.id.listening_time);
        consultantsListView = findViewById(R.id.consultants_list_view);

        // Navigation buttons
        FrameLayout personButton = findViewById(R.id.nav_person_button);
        FrameLayout addButton = findViewById(R.id.nav_add_button);
        FrameLayout musicButton = findViewById(R.id.nav_music_button);
        FrameLayout consultantButton = findViewById(R.id.nav_group_button);

        // Icons
        ImageView backArrow = findViewById(R.id.back_arrow);
        ImageView chartViewIcon = findViewById(R.id.chart_icon);
        ImageView personIcon = findViewById(R.id.person_icon_top);

        // Set up click listeners
        setupClickListeners(personButton, addButton, musicButton, consultantButton,
                backArrow, chartViewIcon, personIcon);
    }

    /**
     * Setup all click listeners
     */
    private void setupClickListeners(FrameLayout personButton, FrameLayout addButton,
                                     FrameLayout musicButton, FrameLayout consultantButton,
                                     ImageView backArrow, ImageView chartViewIcon,
                                     ImageView personIcon) {

        // Person button listener
        personButton.setOnClickListener(v -> {
            Intent i = new Intent(ListViewClickActivity.this, UserListActivity.class);
            startActivity(i);
        });

        // Add button listener
        addButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ListViewClickActivity.this);
            builder.setTitle("Choose an Action");
            builder.setMessage("What would you like to do?");
            builder.setPositiveButton("Add User", (dialog, which) -> {
                Intent i = new Intent(ListViewClickActivity.this, FormActivityPart1.class);
                startActivity(i);
            });
            builder.setNegativeButton("Add Song", (dialog, which) -> {
                Intent i = new Intent(ListViewClickActivity.this, UploadSongScreenActivity.class);
                startActivity(i);
            });
            builder.create().show();
        });

        // Music button listener
        musicButton.setOnClickListener(v -> {
            Intent i = new Intent(ListViewClickActivity.this, MusicListActivity.class);
            startActivity(i);
        });

        // Consultant button listener
        consultantButton.setOnClickListener(v -> {
            Intent i = new Intent(ListViewClickActivity.this, ConsultantListActivity.class);
            startActivity(i);
        });

        // Back arrow listener
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(ListViewClickActivity.this, UserListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        // Chart view icon listener
        chartViewIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ListViewClickActivity.this, DataViewActivity.class);
            intent.putExtra("PATIENT", patientProfile);
            startActivity(intent);
        });

        // Person icon listener
        personIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ListViewClickActivity.this, ListViewClickActivity.class);
            intent.putExtra("PATIENT", patientProfile);
            startActivity(intent);
        });
    }

    /**
     * ✅ Load patient profile from Firebase using async callback
     */
    private void loadPatientProfile() {
        viewModel.getProfileByUserId(userId, new BaseService.SingleItemCallback<PatientProfile>() {
            @Override
            public void onSuccess(PatientProfile patient) {
                if (patient == null) {
                    Toast.makeText(ListViewClickActivity.this,
                            "Error: Patient not found",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Update UI with patient data
                updateUIWithPatientData(patient);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ListViewClickActivity.this,
                        "Error loading patient: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Update all UI elements with patient data
     */
    private void updateUIWithPatientData(PatientProfile patient) {
        // Set text values
        name.setText(patient.getUser().getName());
        userIdTextView.setText("User-ID: " + String.valueOf(patient.getId()));
        phone.setText("Phone: " + patient.getUser().getPhone());
        email.setText("Email: " + patient.getUser().getEmail());
        stageLabel.setText("Stage: " + patient.getSeverity());
        listeningTime.setText("Total Listening Time: " + patient.getTotalListeningDuration());

        // Set up consultants ListView with the current patient's consultants
        List<Consultant> consultantList = patient.getConsultants();
        if (consultantList == null) {
            consultantList = new ArrayList<>();
        }

        AssignedConsultantAdapter adapter = new AssignedConsultantAdapter(this, consultantList);
        consultantsListView.setAdapter(adapter);
    }
}
