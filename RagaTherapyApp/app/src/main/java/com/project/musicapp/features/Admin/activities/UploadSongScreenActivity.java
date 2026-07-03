package com.project.musicapp.features.Admin.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.project.musicapp.R;
import com.project.musicapp.core.models.Category;
import com.project.musicapp.core.models.Music;
import com.project.musicapp.core.viewmodels.CategoryViewModel;
import com.project.musicapp.core.viewmodels.MusicListViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ✅ Updated to use ViewModels with proper lifecycle management
 */
public class UploadSongScreenActivity extends AppCompatActivity {

    // File picking variables
    private TextView songUploadInput;
    private TextView imageUploadInput;
    private Uri selectedSongUri;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> songPickerLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;

    // Form input views
    private EditText songNameInput;
    private Spinner categorySpinner;

    // ✅ ViewModels with lifecycle awareness
    private MusicListViewModel musicViewModel;
    private CategoryViewModel categoryViewModel;

    // Categories loaded from ViewModel
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_song_screen_ui);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Initialize ViewModels properly with ViewModelProvider
        musicViewModel = new ViewModelProvider(this).get(MusicListViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Initialize all views
        initializeViews();

        // Initialize file picker launchers
        initializeFilePickers();

        // ✅ Observe categories from ViewModel
        observeCategories();

        // Set ActionBar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Upload Song");
        }
    }

    /**
     * ✅ Observe categories LiveData from CategoryViewModel
     */
    private void observeCategories() {
        categoryViewModel.getAllCategories().observe(this, categoriesList -> {
            if (categoriesList != null && !categoriesList.isEmpty()) {
                categories = categoriesList;
                setupCategorySpinner();
            } else {
                Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initialize all views from the XML layout
     */
    private void initializeViews() {
        // Navigation buttons
        FrameLayout personButton = findViewById(R.id.nav_person_button);
        FrameLayout addButton = findViewById(R.id.nav_add_button);
        FrameLayout musicButton = findViewById(R.id.nav_music_button);
        FrameLayout consultantButton = findViewById(R.id.nav_group_button);
        ImageView backArrow = findViewById(R.id.back_arrow);

        // Form inputs
        songNameInput = findViewById(R.id.song_name_input);
        categorySpinner = findViewById(R.id.category_spinner);

        // File upload views
        songUploadInput = findViewById(R.id.song_upload_input);
        imageUploadInput = findViewById(R.id.image_upload_input);

        // Clickable rows (entire containers)
        RelativeLayout songUploadRow = findViewById(R.id.song_upload_row);
        RelativeLayout imageUploadRow = findViewById(R.id.image_upload_row);

        // Upload button
        Button uploadButton = findViewById(R.id.upload_button);

        // Setup navigation click listeners
        setupNavigationListeners(personButton, addButton, musicButton, consultantButton, backArrow);

        // Setup file upload row listeners (click anywhere on the row)
        songUploadRow.setOnClickListener(v -> songPickerLauncher.launch("audio/*"));
        imageUploadRow.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Setup main upload button
        uploadButton.setOnClickListener(v -> handleUpload());
    }

    /**
     * Setup category spinner with categories from ViewModel
     */
    private void setupCategorySpinner() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "Loading categories...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    /**
     * Initialize file picker launchers for song and image
     */
    private void initializeFilePickers() {
        // Song file picker
        songPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedSongUri = uri;
                        String fileName = getFileNameFromUri(this, uri);
                        songUploadInput.setText(fileName);
                        Toast.makeText(this, "Song file selected: " + fileName, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Image file picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        String fileName = getFileNameFromUri(this, uri);
                        imageUploadInput.setText(fileName);
                        Toast.makeText(this, "Cover image selected: " + fileName, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Setup navigation button listeners
     */
    private void setupNavigationListeners(FrameLayout personButton, FrameLayout addButton,
                                          FrameLayout musicButton, FrameLayout consultantButton,
                                          ImageView backArrow) {
        personButton.setOnClickListener(v -> {
            Intent i = new Intent(UploadSongScreenActivity.this, UserListActivity.class);
            startActivity(i);
        });

        addButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UploadSongScreenActivity.this);
            builder.setTitle("Choose an Action")
                    .setMessage("What would you like to do?")
                    .setPositiveButton("Add User", (dialog, which) -> {
                        Intent i = new Intent(UploadSongScreenActivity.this, FormActivityPart1.class);
                        startActivity(i);
                    })
                    .setNegativeButton("Add Song", (dialog, which) -> {
                        Toast.makeText(this, "Already on Add Song screen", Toast.LENGTH_SHORT).show();
                    })
                    .create().show();
        });

        musicButton.setOnClickListener(v -> {
            Intent i = new Intent(UploadSongScreenActivity.this, MusicListActivity.class);
            startActivity(i);
        });

        consultantButton.setOnClickListener(v -> {
            Intent i = new Intent(UploadSongScreenActivity.this, ConsultantListActivity.class);
            startActivity(i);
        });

        backArrow.setOnClickListener(v -> finish());
    }

    /**
     * Handle the upload button click - validate and create Music object
     */
    private void handleUpload() {
        if (validateAndCreateMusic()) {
            Toast.makeText(this, "Music uploaded successfully!", Toast.LENGTH_LONG).show();
            // Navigate to MusicListActivity to see the newly uploaded song
            Intent intent = new Intent(UploadSongScreenActivity.this, MusicListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Validates all inputs and creates Music object if valid
     */
    private boolean validateAndCreateMusic() {
        String songName = songNameInput.getText().toString().trim();

        // 1. Validate Song Name
        if (TextUtils.isEmpty(songName)) {
            songNameInput.setError("Song name is required");
            songNameInput.requestFocus();
            return false;
        }

        // 2. Validate Song File
        if (selectedSongUri == null) {
            Toast.makeText(this, "Please select a song file", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 3. Validate Image File
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a cover image", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 4. Check if categories are loaded
        if (categories.isEmpty()) {
            Toast.makeText(this, "Categories not loaded yet. Please wait.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 5. Get selected category
        int selectedCategoryPosition = categorySpinner.getSelectedItemPosition();
        Category selectedCategory = categories.get(selectedCategoryPosition);

        // 6. Extract duration from audio file
        int duration = extractAudioDuration(selectedSongUri);
        if (duration == -1) {
            Toast.makeText(this, "Warning: Could not determine song duration", Toast.LENGTH_SHORT).show();
        }

        // 7. Generate new Music ID
        int newMusicId = Math.abs(java.util.UUID.randomUUID().hashCode());

        // 8. Create Music object with extracted duration
        Music newMusic = new Music(
                newMusicId,
                selectedCategory,
                songName,
                selectedImageUri.toString(), // Image URI
                selectedSongUri.toString(),   // Song URI
                duration                      // Auto-extracted duration (or -1 if failed)
        );

        // 9. ✅ Save using ViewModel (not service)
        musicViewModel.addMusic(newMusic);

        String durationText = duration > 0 ? formatDuration(duration) : "unknown";
        Toast.makeText(this,
                "Song '" + songName + "' added!\nDuration: " + durationText,
                Toast.LENGTH_LONG).show();

        // 10. Clear all fields
        clearAllFields();
        return true;
    }

    /**
     * Extracts audio duration from URI using MediaMetadataRetriever
     * @param audioUri URI of the audio file
     * @return Duration in seconds, or -1 if extraction fails
     */
    private int extractAudioDuration(Uri audioUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, audioUri);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                long durationMs = Long.parseLong(durationStr);
                int durationSeconds = (int) (durationMs / 1000); // Convert to seconds
                return durationSeconds;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error extracting audio duration", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1; // Return -1 if extraction fails
    }

    /**
     * Formats duration in seconds to MM:SS format
     * @param seconds Duration in seconds
     * @return Formatted string (e.g., "3:45")
     */
    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    /**
     * Clears all input fields and resets URIs
     */
    private void clearAllFields() {
        songNameInput.setText("");
        songUploadInput.setText("");
        imageUploadInput.setText("");
        categorySpinner.setSelection(0);
        selectedSongUri = null;
        selectedImageUri = null;
    }

    /**
     * Helper method to get the display name of a file from its content URI
     */
    private String getFileNameFromUri(Context context, Uri uri) {
        String fileName = "Unknown file";
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
