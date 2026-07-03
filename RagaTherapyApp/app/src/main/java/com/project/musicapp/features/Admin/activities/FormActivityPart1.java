package com.project.musicapp.features.Admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.project.musicapp.R;

public class FormActivityPart1 extends AppCompatActivity {

    private ImageView profileImage; // 3. Declare ImageView
    private Spinner roleSpinner;
    private EditText fullNameInput, phoneInput;
    private Button nextButton;

    // Define the key for passing the profile picture URL
    private static final String PROFILE_PIC_URL_KEY = "PROFILE_PIC_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_part1);

        // Initialize views
        profileImage = findViewById(R.id.profile_image); // 4. Initialize ImageView
        roleSpinner = findViewById(R.id.role_spinner);
        fullNameInput = findViewById(R.id.full_name_input);
        phoneInput = findViewById(R.id.phone_input);
        nextButton = findViewById(R.id.next_button);

        // 5. Call the new method to load the profile picture
        loadUserProfilePicture();

        // Populate Role Spinner
        String[] roles = {"Select Role", "Patient", "Consultant","Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Next Button Click Listener
        nextButton.setOnClickListener(v -> validateAndProceed());
    }

    /**
     * Retrieves the profile picture URL from the Intent and loads it into the ImageView.
     */
    private void loadUserProfilePicture() {
        // Get the photo URL passed from the login activity
        String photoUrl = getIntent().getStringExtra(PROFILE_PIC_URL_KEY);

        if (!TextUtils.isEmpty(photoUrl)) {
            // Use Glide to fetch and display the image from the URL
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_user_placeholder) // Image while loading
                    .error(R.drawable.ic_user_placeholder) // Image if loading fails
                    .circleCrop() // Optional: Makes the image circular
                    .into(profileImage);
        } else {
            // If no URL is available, keep the default placeholder
            profileImage.setImageResource(R.drawable.ic_user_placeholder);
        }
    }


    private void validateAndProceed() {
        String selectedRole = roleSpinner.getSelectedItem().toString().trim();
        String fullName = fullNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Role Validation
        if (selectedRole.equals("Select Role")) {
            Toast.makeText(this, "Please select a valid role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Full Name Validation
        if (TextUtils.isEmpty(fullName)) {
            fullNameInput.setError("Full name is required");
            fullNameInput.requestFocus();
            return;
        }
        if (!fullName.matches("^[a-zA-Z\\s]+$")) {
            fullNameInput.setError("Name should only contain letters");
            fullNameInput.requestFocus();
            return;
        }

        // Phone Number Validation
        // Phone Number Validation
        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return;
        }

// Remove the +91 prefix and spaces for validation
        String phoneNumber = phone.replace("+91", "").trim();

        if (!Patterns.PHONE.matcher(phoneNumber).matches() || phoneNumber.length() != 10) {
            phoneInput.setError("Enter a valid 10-digit phone number");
            phoneInput.requestFocus();
            return;
        }


        // If all validations pass
        Toast.makeText(this, "Details saved successfully!", Toast.LENGTH_SHORT).show();

        // Pass data to next screen
        Intent intent;

        switch (selectedRole) {
            case "Admin":
                intent = new Intent(FormActivityPart1.this, AdminFormPart2.class);
                break;

            case "Patient":
                intent = new Intent(FormActivityPart1.this, PatientFormPart2.class);
                break;

            case "Consultant":
                intent = new Intent(FormActivityPart1.this, ConsultantFormPart2.class);
                break;

            default:
                // Fallback to UserListActivity if role doesn't match
                intent = new Intent(FormActivityPart1.this, UserListActivity.class);
                break;
        }

        // Get the current photo URL to pass it along (optional but good practice)
        String photoUrl = getIntent().getStringExtra(PROFILE_PIC_URL_KEY);

        intent.putExtra("USER_ROLE", selectedRole);
        intent.putExtra("USER_NAME", fullName);
        intent.putExtra("USER_PHONE", phone);
        intent.putExtra(PROFILE_PIC_URL_KEY, photoUrl); // Pass the URL to the next activity

        startActivity(intent);
    }
}