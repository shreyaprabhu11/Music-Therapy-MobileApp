package com.project.musicapp.features.Admin.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.project.musicapp.R;
import com.project.musicapp.core.models.ListeningSession;
import com.project.musicapp.core.services.ListeningSessionService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ExcelFormGeneration extends AppCompatActivity {

    private TextView startDateDisplay;
    private TextView endDateDisplay;
    private Button downloadButton;
    private ImageButton backButton;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATETIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excel_form_generation);

        // Initialize Views
        startDateDisplay = findViewById(R.id.start_date_display);
        endDateDisplay = findViewById(R.id.end_date_display);
        downloadButton = findViewById(R.id.next_button);
        backButton = findViewById(R.id.back_button);

        // Set Click Listeners for Date Pickers
        startDateDisplay.setOnClickListener(v -> showDatePicker(startDateDisplay));
        endDateDisplay.setOnClickListener(v -> showDatePicker(endDateDisplay));

        // Download Button
        downloadButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                handleDownload();
            } else {
                requestPermissions();
            }
        });

        // Back Button
        backButton.setOnClickListener(v -> {
            Intent i = new Intent(ExcelFormGeneration.this, UserListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        });
    }

    /**
     * Displays the DatePickerDialog and updates the specified TextView with the selected date.
     */
    private void showDatePicker(final TextView textView) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(),
                            "%02d-%02d-%d", dayOfMonth, monthOfYear + 1, year1);
                    textView.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    /**
     * Handles the download logic - generates CSV file with listening session data
     */
    private void handleDownload() {
        String startDateStr = startDateDisplay.getText().toString();
        String endDateStr = endDateDisplay.getText().toString();

        // Validate dates are selected
        if (startDateStr.equals("DD-MM-YYYY") || endDateStr.equals("DD-MM-YYYY")) {
            Toast.makeText(this, "Please select both Start Date and End Date.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Parse dates
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);

            // Validate date range
            if (startDate.after(endDate)) {
                Toast.makeText(this, "Start date must be before or equal to end date.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Set end date to end of day (23:59:59)
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            endDate = cal.getTime();

            // Generate CSV file
            generateCSVFile(startDate, endDate);

        } catch (ParseException e) {
            Toast.makeText(this, "Error parsing dates. Please try again.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Generates CSV file with listening session data
     */
    /**
     * Generates CSV file with listening session data using ASYNC service
     */
    private void generateCSVFile(Date startDate, Date endDate) {
        ListeningSessionService sessionService = ListeningSessionService.getInstance();

        // Convert Date to long timestamps (milliseconds)
        long startTimestamp = startDate.getTime();
        long endTimestamp = endDate.getTime();

        // ✅ NOW USING ASYNC: Get all sessions within date range
        sessionService.getSessionsByDateRange(startTimestamp, endTimestamp,
                new com.project.musicapp.core.services.BaseService.DataCallback<ListeningSession>() {
                    @Override
                    public void onSuccess(List<ListeningSession> sessions) {
                        if (sessions.isEmpty()) {
                            Toast.makeText(ExcelFormGeneration.this,
                                    "No listening sessions found in the selected date range.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Generate CSV file with the fetched sessions
                        writeCSVFile(sessions);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ExcelFormGeneration.this,
                                "Error loading sessions: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Write sessions to CSV file
     */
    private void writeCSVFile(List<ListeningSession> sessions) {
        try {
            // Create file
            File file = createCSVFile();
            FileWriter writer = new FileWriter(file);

            // Write CSV header
            writer.append("Patient Name,Music Name,Duration (minutes),Date,Category\n");

            // Write data rows
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());

            for (ListeningSession session : sessions) {
                String patientName = getPatientName(session);
                String musicName = getMusicName(session);
                String duration = formatDuration(session.getDuration());

                // ✅ Convert long timestamp to Date for formatting
                String date = dateFormat.format(new Date(session.getDate()));
                String category = getCategory(session);

                writer.append(escapeCSV(patientName)).append(",");
                writer.append(escapeCSV(musicName)).append(",");
                writer.append(duration).append(",");
                writer.append(date).append(",");
                writer.append(escapeCSV(category)).append("\n");
            }

            writer.flush();
            writer.close();

            // Show success message
            Toast.makeText(this,
                    "CSV file downloaded successfully!\nLocation: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

            // Navigate back
            Intent i = new Intent(ExcelFormGeneration.this, UserListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);

        } catch (IOException e) {
            Toast.makeText(this, "Error generating CSV file: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    /**
     * Creates CSV file in PUBLIC Downloads directory (works for all Android versions)
     */
    private File createCSVFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String fileName = "listening_sessions_" + timestamp + ".csv";

        // Always use public Downloads folder
        File downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        // Ensure directory exists
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        return new File(downloadsDir, fileName);
    }

    /**
     * Helper methods to extract data safely
     */
    private String getPatientName(ListeningSession session) {
        if (session.getPatientProfile() != null &&
                session.getPatientProfile().getUser() != null) {
            return session.getPatientProfile().getUser().getName();
        }
        return "Unknown Patient";
    }

    private String getMusicName(ListeningSession session) {
        if (session.getMusic() != null) {
            return session.getMusic().getMusicName();
        }
        return "Unknown Music";
    }

    private String getCategory(ListeningSession session) {
        if (session.getMusic() != null && session.getMusic().getCategory() != null) {
            return session.getMusic().getCategory().getName();
        }
        return "Unknown Category";
    }

    /**
     * Format duration from seconds to minutes (with 2 decimal places)
     */
    private String formatDuration(int durationInSeconds) {
        double minutes = durationInSeconds / 60.0;
        return String.format(Locale.getDefault(), "%.2f", minutes);
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    /**
     * Check if storage permissions are granted
     */
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) - No permission needed with requestLegacyExternalStorage
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11-12 (API 30-32)
            return Environment.isExternalStorageManager();
        } else {
            // Android 10 and below
            int write = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Request storage permissions
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleDownload();
            } else {
                Toast.makeText(this,
                        "Storage permission is required to download the file.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
