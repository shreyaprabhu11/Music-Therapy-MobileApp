package com.project.musicapp.features.Admin.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.project.musicapp.R;
import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.models.User;
import com.project.musicapp.core.services.ConsultantService;
import com.project.musicapp.core.services.UserService;
import com.project.musicapp.core.viewmodels.ConsultantViewModel;
import com.project.musicapp.core.viewmodels.UserViewModel;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsultantFormPart2 extends AppCompatActivity {

    private ImageView profileImage;
    private Spinner roleSpinner;
    private EditText emailInput;
    private Button nextButton;
    private String selectedRole = "";

    // Data from Part 1
    private String userName;
    private String userPhone;
    private String userRole;
    private String photoUrl;

    // Services
    private UserViewModel userService;
    private ConsultantViewModel consultantService;

    // Enhanced email regex pattern
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

    private static final String[] DISPOSABLE_EMAIL_DOMAINS = {
            "tempmail.com", "throwaway.email", "guerrillamail.com",
            "10minutemail.com", "mailinator.com"
    };

    private static final String[] VALID_ROLES = {
            "Therapist", "Oncologist"
    };

    // For password generation
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    private boolean isImageLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultant_form_part2);

        // Initialize services
        userService = new UserViewModel();
        consultantService = new ConsultantViewModel();

        // Retrieve data from Part 1
        retrieveDataFromPart1();

        // Initialize Views
        initializeViews();

        // Load profile picture from Part 1
        loadProfilePictureFromPart1();

        // Set up the Role Spinner
        setupRoleSpinner();

        // Set Focus Change Listener for profile picture loading
        setupProfilePictureLoading();

        // Set up Button Click Listener
        setupClickListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Consultant Registration");
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

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        roleSpinner = findViewById(R.id.role_spinner);
        emailInput = findViewById(R.id.full_name_input);
        nextButton = findViewById(R.id.next_button);
    }

    /**
     * Load profile picture from Part 1
     */
    private void loadProfilePictureFromPart1() {
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

    private void setupRoleSpinner() {
        String[] roles = new String[]{
                "--- Select Role ---",
                "Therapist",
                "Oncologist"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                roles
        );
        roleSpinner.setAdapter(adapter);
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = parent.getItemAtPosition(position).toString();
                if (!selectedRole.equals("--- Select Role ---")) {
                    Toast.makeText(ConsultantFormPart2.this,
                            "Role selected: " + selectedRole, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = "--- Select Role ---";
            }
        });
    }

    private void setupClickListeners() {
        nextButton.setOnClickListener(v -> {
            nextButton.setEnabled(false);
            if (validateInput()) {
                createAndSaveConsultant();
            } else {
                nextButton.setEnabled(true);
            }
        });
    }

    private void setupProfilePictureLoading() {
        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !isImageLoading) {
                String email = emailInput.getText().toString().trim();
                if (isEmailValid(email)) {
                    String imageUrl = getProfileImageUrl(email);
                    loadProfilePicture(imageUrl);
                } else {
                    profileImage.setImageResource(R.drawable.ic_user_placeholder);
                }
            }
        });
    }

    private void loadProfilePicture(String url) {
        if (url == null || (!url.startsWith("https://") && !url.startsWith("http://"))) {
            profileImage.setImageResource(R.drawable.ic_user_placeholder);
            return;
        }

        isImageLoading = true;
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .circleCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        isImageLoading = false;
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        isImageLoading = false;
                        return false;
                    }
                })
                .into(profileImage);
    }

    private String getProfileImageUrl(String email) {
        if (email.toLowerCase().startsWith("s")) {
            return "https://i.imgur.com/K1L08fC.png";
        } else if (email.toLowerCase().startsWith("a")) {
            return "https://i.imgur.com/y0P89B3.png";
        }
        return "https://i.imgur.com/rM8R9cO.png";
    }

    private boolean validateInput() {
        if (!validateRole()) return false;
        if (!validateEmail()) return false;
        return true;
    }

    private boolean validateRole() {
        if (selectedRole.equals("--- Select Role ---") || TextUtils.isEmpty(selectedRole)) {
            Toast.makeText(this, "Please select a valid role", Toast.LENGTH_SHORT).show();
            roleSpinner.requestFocus();
            return false;
        }

        boolean isValidRole = false;
        for (String validRole : VALID_ROLES) {
            if (selectedRole.equals(validRole)) {
                isValidRole = true;
                break;
            }
        }

        if (!isValidRole) {
            Toast.makeText(this, "Invalid role selected", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateEmail() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (!isEmailValid(email)) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return false;
        }

        if (isDisposableEmail(email)) {
            emailInput.setError("Temporary email addresses are not allowed");
            emailInput.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isEmailValid(final String email) {
        if (email == null) return false;
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    private boolean isDisposableEmail(String email) {
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        for (String disposableDomain : DISPOSABLE_EMAIL_DOMAINS) {
            if (domain.equals(disposableDomain)) {
                return true;
            }
        }
        return false;
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
     * Creates User and Consultant objects and saves them to respective services
     */
    private void createAndSaveConsultant() {
        String email = emailInput.getText().toString().trim().toLowerCase();

        // Generate random password
        String generatedPassword = generateRandomPassword();

        // Generate unique IDs
        int newUserId = UUID.randomUUID().hashCode();
        int newConsultantId = UUID.randomUUID().hashCode();

        // Create User object first
        // Constructor: (id, name, email, password, role, phone, profilePictureUrl)
        User newUser = new User(
                newUserId,
                userName,
                email,
                generatedPassword,
                User.Role.CONSULTANT,  // Set role as CONSULTANT enum
                userPhone,
                photoUrl,
                true
        );

        // Save User to UserService
        userService.addItem(newUser);

        // Create Consultant object
        // Constructor: (consultantId, user, bio, designation, qualifications, timeSlots)
        Consultant newConsultant = new Consultant(
                newConsultantId,
                newUser,
                "Experienced " + selectedRole + " specializing in music therapy.",  // Bio
                selectedRole,  // Designation (Therapist or Oncologist)
                "Board Certified " + selectedRole,  // Qualifications
                new ArrayList<>()  // Empty timeSlots list for now
        );

        // Save Consultant to ConsultantService
        consultantService.addItem(newConsultant);

        Toast.makeText(this, "Consultant registered successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to credentials screen
        Intent intent = new Intent(ConsultantFormPart2.this, GenerateCredentialsActivity.class);
        intent.putExtra("USER_ID", newUserId);
        intent.putExtra("CONSULTANT_ID", newConsultantId);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("USER_EMAIL", email);
        intent.putExtra("USER_PASSWORD", generatedPassword);
        intent.putExtra("USER_ROLE", userRole);
        intent.putExtra("CONSULTANT_ROLE", selectedRole);
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
        nextButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
    }
}
