package com.project.musicapp.features.Admin.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.project.musicapp.R;

public class GenerateCredentialsActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button copyButton;

    // Data from Part2 forms
    private String userEmail;
    private String generatedPassword;
    private String userName;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_credentials);

        // 1. Initialize Views
        // Note: The XML uses 'email_input' for Username and 'disease_input' for Password.
        usernameEditText = findViewById(R.id.email_input);
        passwordEditText = findViewById(R.id.disease_input);
        copyButton = findViewById(R.id.copy_button);

        // 2. Retrieve data from Intent
        retrieveCredentialsFromIntent();

        // 3. Display credentials in EditText fields
        displayCredentials();

        // 4. Make EditText fields non-editable
        // Setting input type to 'none' and focusable to false prevents keyboard and editing.
        usernameEditText.setInputType(0); // InputType.TYPE_NULL
        usernameEditText.setFocusable(false);
        usernameEditText.setLongClickable(false);

        passwordEditText.setInputType(0); // InputType.TYPE_NULL
        passwordEditText.setFocusable(false);
        passwordEditText.setLongClickable(false);

        // 5. Set up the Copy Button Click Listener
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyCredentialsToClipboard();

                // Navigate back to user list
                Intent i = new Intent(GenerateCredentialsActivity.this, UserListActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish(); // Close this activity
            }
        });

        // Optional: Set a proper title for the screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Account Created Successfully");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Retrieves credentials data passed from the Part2 forms via Intent
     */
    private void retrieveCredentialsFromIntent() {
        Intent intent = getIntent();

        // Get email (username) and password from intent
        userEmail = intent.getStringExtra("USER_EMAIL");
        generatedPassword = intent.getStringExtra("USER_PASSWORD");
        userName = intent.getStringExtra("USER_NAME");
        userRole = intent.getStringExtra("USER_ROLE");

        // Validate that we received the required data
        if (TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(generatedPassword)) {
            Toast.makeText(this, "Error: Missing credentials data", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if data is missing
        }
    }

    /**
     * Displays the retrieved credentials in the EditText fields
     */
    private void displayCredentials() {
        if (!TextUtils.isEmpty(userEmail)) {
            usernameEditText.setText(userEmail);
        }

        if (!TextUtils.isEmpty(generatedPassword)) {
            passwordEditText.setText(generatedPassword);
        }

        // Show success message with user name
        if (!TextUtils.isEmpty(userName)) {
            Toast.makeText(this,
                    "Account created for " + userName + "!",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Copies the Username and Password, in a formatted string, to the device's clipboard.
     */
    private void copyCredentialsToClipboard() {
        // Get the values from the EditText fields
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // 1. Create a formatted string to copy
        String credentialsFormat = "Username: " + username + "\nPassword: " + password;

        // 2. Get the system's clipboard manager
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard != null) {
            // 3. Create a new ClipData object
            // Label is what the OS might show (e.g., "Copied text"), Item is the actual data.
            ClipData clip = ClipData.newPlainText("Credentials", credentialsFormat);

            // 4. Set the clip data to the clipboard
            clipboard.setPrimaryClip(clip);

            // 5. Provide user feedback
            Toast.makeText(this, "Credentials copied to clipboard!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Clipboard service not available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
