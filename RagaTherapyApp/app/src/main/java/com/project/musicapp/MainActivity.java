package com.project.musicapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.musicapp.core.viewmodels.UserViewModel;
import com.project.musicapp.features.Admin.activities.UserListActivity;
import com.project.musicapp.features.patient.activities.ConsultantListActivity;
import com.project.musicapp.features.patient.activities.MusicList;
import com.project.musicapp.features.settings.activities.ResetPasswordActivity;

public class MainActivity extends AppCompatActivity {

    private Spinner roleSpinner;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        roleSpinner = findViewById(R.id.role_spinner);
        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordTextView = findViewById(R.id.forgot_password_textview);

        // Populate Role Spinner
        String[] roles = {"Select Role", "Admin", "Consultant", "Patient"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Login Button Click
        loginButton.setOnClickListener(v -> validateAndLogin());
    }

    private void validateAndLogin() {
        String selectedRole = roleSpinner.getSelectedItem().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate Role
        if (selectedRole.equals("Select Role")) {
            Toast.makeText(this, "Please select your role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            emailEditText.requestFocus();
            return;
        }

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        UserViewModel userViewModel = new UserViewModel();
        userViewModel.validateLogin(email, password, selectedRole, (user, error) -> {
            runOnUiThread(() -> {
                if (user != null && error == null) {
                    Toast.makeText(this, "Login successful as " + selectedRole, Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("USER_ID", user.getId());
                    editor.apply();
                    Intent intent = null;
                    if(selectedRole.equals("Admin")) {
                        intent = new Intent(MainActivity.this, UserListActivity.class);
                    } else if(selectedRole.equals("Patient")) {
                        intent = new Intent(MainActivity.this, MusicList.class);
                    } else if(selectedRole.equals("Consultant")) {
                        intent = new Intent(MainActivity.this, UserListActivity.class);
                    }



                    if (intent != null) {
                        intent.putExtra("USERROLE", selectedRole);
                        intent.putExtra("USEREMAIL", email);
                        intent.putExtra("USERID", user.getId());
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                }



            });
        });
       // Close login screen
    }

}