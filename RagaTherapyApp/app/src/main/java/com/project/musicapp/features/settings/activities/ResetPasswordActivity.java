package com.project.musicapp.features.settings.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.project.musicapp.R;
import com.project.musicapp.core.models.User;
import com.project.musicapp.core.viewmodels.UserViewModel;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;
    private UserViewModel userViewModel;

    // -----------------------------
    // HIGHLIGHT: Dummy userId = 7
    // -----------------------------
    private int userId ;

    private User currentUser; // holds the fetched user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);


        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        userId= sharedPref.getInt("USER_ID", -1); // -1 means not found


        ShapeableImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        userViewModel = new UserViewModel();

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        // -----------------------------
        // Fetch user asynchronously
        // -----------------------------
        userViewModel.getAllUsers().observe(this, new Observer<java.util.List<User>>() {
            @Override
            public void onChanged(java.util.List<User> users) {
                if (users == null || users.isEmpty()) return;

                for (User u : users) {
                    if (u.getId() == userId) {
                        currentUser = u;
                        break;
                    }
                }
            }
        });

        userViewModel.refreshUsers(); // trigger data fetch

        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        if (currentUser == null) {
            showToast("User not loaded yet, please wait...");
            return;
        }

        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showToast("Passwords do not match");
            return;
        }

        if (!currentUser.getPassword().equals(oldPass)) {
            showToast("Old password is incorrect");
            return;
        }

        // Update password and first login
        currentUser.setPassword(newPass);
        currentUser.setFirstLogin(false);

        userViewModel.updateUser(userId, currentUser);

        showToast("Password updated successfully!");
        finish();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
