package com.project.musicapp.features.Admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.project.musicapp.R;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.models.User;
import com.project.musicapp.core.services.PatientProfileService;
import com.project.musicapp.core.services.UserService;
import com.project.musicapp.core.viewmodels.PatientProfileViewModel;
import com.project.musicapp.core.viewmodels.UserViewModel;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatientFormPart2 extends AppCompatActivity {

    private ImageView profileImage;
    private EditText emailEditText;
    private TextView diseaseTextView;
    private Spinner stageSpinner;
    private Button createAccountButton;
    private String selectedStage = "";

    // Data from Part 1
    private String userName;
    private String userPhone;
    private String userRole;
    private String photoUrl;

    // Services
    private UserViewModel userService;
    private PatientProfileViewModel patientProfileService;

    // Regular Expression for email validation
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    // For password generation
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_form_part2);

        // Initialize services
        userService = new UserViewModel();
        patientProfileService = new PatientProfileViewModel();

        // Retrieve data from Part 1
        retrieveDataFromPart1();

        // Initialize Views
        initializeViews();

        // Load profile picture
        loadProfilePicture();

        // Set the fixed disease name
        diseaseTextView.setText("BREAST CANCER");

        // Set up the Stage Spinner
        setupStageSpinner();

        // Set up Button Click Listener
        setupClickListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Patient Registration");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Retrieve data passed from FormActivityPart1
     */
    private void retrieveDataFromPart1() {
        Intent intent = getIntent();
        userName = intent.getStringExtra("USER_NAME");
        userPhone = intent.getStringExtra("USER_PHONE");
        userRole = intent.getStringExtra("USER_ROLE");
        photoUrl = intent.getStringExtra("PROFILE_PIC_URL");

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userPhone)) {
            Toast.makeText(this, "Error: Missing user data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        emailEditText = findViewById(R.id.email_input);
        diseaseTextView = findViewById(R.id.disease_input);
        stageSpinner = findViewById(R.id.stage_spinner);
        createAccountButton = findViewById(R.id.create_button);
    }

    /**
     * Load profile picture using Glide
     */
    private void loadProfilePicture() {
        if (!TextUtils.isEmpty(photoUrl)) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_user_placeholder);
        }
    }

    /**
     * Populates the Spinner with disease stages.
     */
    private void setupStageSpinner() {
        String[] stages = new String[]{"--- Select Stage ---", "Stage I", "Stage II", "Stage III", "Stage IV"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                stages
        );
        stageSpinner.setAdapter(adapter);

        stageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStage = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStage = "--- Select Stage ---";
            }
        });
    }

    /**
     * Setup click listeners with double-click prevention
     */
    private void setupClickListeners() {
        createAccountButton.setOnClickListener(v -> {
            createAccountButton.setEnabled(false);
            if (validateInput()) {
                createAndSavePatient();
            } else {
                createAccountButton.setEnabled(true);
            }
        });
    }

    /**
     * Performs input validation checks.
     */
    private boolean validateInput() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            emailEditText.requestFocus();
            return false;
        }

        if (!isEmailValid(email)) {
            emailEditText.setError("Enter a valid email address.");
            emailEditText.requestFocus();
            return false;
        }

        if (selectedStage.equals("--- Select Stage ---") || TextUtils.isEmpty(selectedStage)) {
            Toast.makeText(this, "Please select the disease stage.", Toast.LENGTH_SHORT).show();
            stageSpinner.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Helper method to validate email format using regex.
     */
    private boolean isEmailValid(final String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Generates a random 6-character alphanumeric password
     */
    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(ALPHANUMERIC.length());
            password.append(ALPHANUMERIC.charAt(index));
        }
        return password.toString();
    }

    /**
     * Creates User and PatientProfile objects and saves them to respective services
     */
    private void createAndSavePatient() {
        String email = emailEditText.getText().toString().trim().toLowerCase();

        // Generate random password
        String generatedPassword = generateRandomPassword();

        // Generate unique IDs
        int newUserId = java.util.UUID.randomUUID().hashCode();
        int newPatientId = java.util.UUID.randomUUID().hashCode();

        // Create User object first
        // Constructor: (id, name, email, password, role, phone, profilePictureUrl)
        User newUser = new User(
                newUserId,
                userName,
                email,
                generatedPassword,
                User.Role.PATIENT,
                userPhone,
                photoUrl,
                true
        );

        // Save User to UserService
        userService.addItem(newUser);

        // Create PatientProfile object
        // Constructor from your service: (id, user, stage, consultants, state, city, areaType, address, phone)
        PatientProfile newPatient = new PatientProfile(
                newPatientId,
                newUser,
                selectedStage,  // Stage I, II, III, or IV
                new ArrayList<>(),  // Empty consultants list for now
                "Karnataka",  // Default state (you can add fields for this)
                "Bangalore",  // Default city (you can add fields for this)
                "Urban",  // Default area type
                "To be updated",  // Default address (you can add field for this)
                userPhone  // Phone from Part 1
        );

        // Save PatientProfile to PatientProfileService
        patientProfileService.addItem(newPatient);

        Toast.makeText(this, "Patient registered successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to credentials screen
        Intent intent = new Intent(PatientFormPart2.this, GenerateCredentialsActivity.class);
        intent.putExtra("USER_ID", newUserId);
        intent.putExtra("PATIENT_ID", newPatientId);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("USER_EMAIL", email);
        intent.putExtra("USER_PASSWORD", generatedPassword);
        intent.putExtra("USER_ROLE", userRole);
        intent.putExtra("DISEASE_STAGE", selectedStage);
        startActivity(intent);

        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        createAccountButton.setEnabled(true);
    }
}
