package com.project.musicapp.features.Admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.project.musicapp.R;
import com.project.musicapp.core.models.User;
import com.project.musicapp.core.services.UserService;
import com.project.musicapp.core.viewmodels.UserViewModel;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminFormPart2 extends AppCompatActivity {

    private ImageView profileImage;
    private EditText emailInput;
    private Button createAccountButton;

    // Data from Part 1
    private String userName;
    private String userPhone;
    private String userRole;
    private String photoUrl;

    // Services
    private UserViewModel userService;

    // Enhanced email regex pattern
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

    private static final String[] DISPOSABLE_EMAIL_DOMAINS = {
            "tempmail.com", "throwaway.email", "guerrillamail.com",
            "10minutemail.com", "mailinator.com"
    };

    // For password generation
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_form_part2);

        // Initialize service
        userService = new UserViewModel();

        // Retrieve data from Part 1
        retrieveDataFromPart1();

        // Initialize Views
        initializeViews();

        // Load profile picture
        loadProfilePicture();

        // Set up Button Click Listener
        setupClickListeners();

        // Set ActionBar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Registration");
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

        // Validate that we received the data
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
        emailInput = findViewById(R.id.role_spinner);
        createAccountButton = findViewById(R.id.next_button);
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
     * Setup click listeners with double-click prevention
     */
    private void setupClickListeners() {
        createAccountButton.setOnClickListener(v -> {
            createAccountButton.setEnabled(false);
            if (validateInput()) {
                createAndSaveUser();
            } else {
                createAccountButton.setEnabled(true);
            }
        });
    }

    /**
     * Comprehensive input validation for the Email field.
     */
    private boolean validateInput() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (email.length() < 5) {
            emailInput.setError("Email is too short");
            emailInput.requestFocus();
            return false;
        }

        if (email.length() > 254) {
            emailInput.setError("Email is too long");
            emailInput.requestFocus();
            return false;
        }

        if (email.contains(" ")) {
            emailInput.setError("Email cannot contain spaces");
            emailInput.requestFocus();
            return false;
        }

        if (!isEmailValid(email)) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return false;
        }

        if (email.contains("..")) {
            emailInput.setError("Email cannot contain consecutive dots");
            emailInput.requestFocus();
            return false;
        }

        if (isDisposableEmail(email)) {
            emailInput.setError("Temporary email addresses are not allowed");
            emailInput.requestFocus();
            return false;
        }

        String suggestion = getEmailSuggestion(email);
        if (suggestion != null) {
            Toast.makeText(this, "Did you mean: " + suggestion + "?", Toast.LENGTH_LONG).show();
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

    private String getEmailSuggestion(String email) {
        String lowerEmail = email.toLowerCase();
        if (lowerEmail.contains("@gmial.com") || lowerEmail.contains("@gmai.com")) {
            return email.replaceAll("@gmial\\.com|@gmai\\.com", "@gmail.com");
        }
        if (lowerEmail.contains("@yaho.com") || lowerEmail.contains("@yahooo.com")) {
            return email.replaceAll("@yaho\\.com|@yahooo\\.com", "@yahoo.com");
        }
        if (lowerEmail.contains("@outlok.com") || lowerEmail.contains("@outloo.com")) {
            return email.replaceAll("@outlok\\.com|@outloo\\.com", "@outlook.com");
        }
        return null;
    }

    /**
     * Generates a random 6-character alphanumeric password
     * @return Random password string
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
     * Creates User object and saves it to UserViewModel
     */
    private void createAndSaveUser() {
        String email = emailInput.getText().toString().trim().toLowerCase();

        // Generate random 6-character alphanumeric password
        String generatedPassword = generateRandomPassword();

        // Generate a unique ID (you can improve this logic)
        int newUserId = UUID.randomUUID().hashCode();

        // Convert role string to User.Role enum
        User.Role roleEnum = User.Role.ADMIN; // Default to ADMIN

        // Create User object with correct constructor parameter order
        // Constructor: (int id, String name, String email, String password, Role role, String phone, String profilePictureUrl)
        User newUser = new User(
                newUserId,
                userName,
                email,
                generatedPassword,  // Generated password
                roleEnum,           // Role enum
                userPhone,
                photoUrl,
                true
        );

        // Save to UserService
        userService.addItem(newUser);

        // Show success message
        Toast.makeText(this, "Admin account created successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to credentials screen
        Intent intent = new Intent(AdminFormPart2.this, GenerateCredentialsActivity.class);
        intent.putExtra("USER_ID", newUserId);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("USER_EMAIL", email);
        intent.putExtra("USER_PASSWORD", generatedPassword);  // Pass generated password
        intent.putExtra("USER_ROLE", userRole);
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
