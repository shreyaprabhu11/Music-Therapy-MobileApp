package com.project.musicapp.features.Admin.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.musicapp.R;
import com.project.musicapp.core.models.Category;
import com.project.musicapp.core.models.ListeningSession;
import com.project.musicapp.core.models.Music;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.ListeningSessionService;
import com.project.musicapp.core.services.MusicService;
import com.project.musicapp.core.viewmodels.ListeningSessionViewModel;
import com.project.musicapp.core.services.CategoryService;
import java.util.HashMap;
import com.project.musicapp.core.viewmodels.MusicListViewModel;
import com.project.musicapp.features.Admin.fragments.PieChartFragment;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataViewActivity extends AppCompatActivity implements PieChartFragment.OnPieSliceSelectedListener {

    private HorizontalBarChart barChart;
    private int patientId;
    private PatientProfile patientProfile;
    private ListeningSessionService listeningSessionService;
    private MusicListViewModel musicService;

    private List<Category> cachedCategories = new ArrayList<>();
    private List<Music> cachedMusic = new ArrayList<>();
    private boolean categoriesLoaded = false;
    private boolean musicLoaded = false;
    private boolean dataLoading = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize services
        listeningSessionService = ListeningSessionService.getInstance();
        musicService = new MusicListViewModel();

        // Get the intent that started this activity
        Intent intent = getIntent();

        // Retrieve the PATIENT_ID (as an int)
        PatientProfile patientProfile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            patientProfile = (PatientProfile) intent.getSerializableExtra("PATIENT", PatientProfile.class);
        } else {
            patientProfile = (PatientProfile) intent.getSerializableExtra("PATIENT");
        }

        Toast.makeText(this, "Error: No patient ID received"+patientProfile, Toast.LENGTH_SHORT).show();

        patientId = patientProfile.getId();

        // Check if the patientId was received successfully
        if (patientId == -1) {
            Toast.makeText(this, "Error: No patient ID received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize the barChart
        barChart = findViewById(R.id.barChart);

        // Set up all click listeners
        setupNavigationListeners();

        // ✅ LOAD CACHED DATA FIRST, THEN SETUP CHART
        loadCachedData();


        // Load the PieChartFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.pie_chart_fragment_container, PieChartFragment.newInstance(patientId))
                    .commit();
        }
    }

    /**
     * This method is required by the interface. It's called from the fragment
     * when a pie slice is selected.
     */
    @Override
    public void onSliceSelected(String categoryName) {
        // Trigger the bar chart update with the selected category
        updateBarChartForCategory(categoryName);
    }

    /**
     * Sets up the initial state of the bar chart - shows percentage contribution of each category.
     */
    private void setupInitialBarChart() {

        // Get all sessions for this patient (ASYNC)
        listeningSessionService.getSessionsByPatientId(patientId, new BaseService.DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> patientSessions) {
                if (patientSessions.isEmpty()) {
                    Toast.makeText(DataViewActivity.this, "No listening data available", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get duration per category for this patient
                Map<Integer, Integer> categoryDurations = new java.util.HashMap<>();
                int totalDuration = 0;

                for (ListeningSession session : patientSessions) {
                    totalDuration += session.getDuration();
                    if (session.getMusic() != null && session.getMusic().getCategory() != null) {
                        int categoryId = session.getMusic().getCategory().getId();
                        categoryDurations.put(categoryId,
                                    categoryDurations.getOrDefault(categoryId, 0) + session.getDuration());
                    }
                }

                // Prepare data for bar chart
                displayCategoryBarChart(categoryDurations, totalDuration);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DataViewActivity.this, "Error loading data: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ STEP 1: Load all categories and music into cache
     */
    private void loadCachedData() {
        if (dataLoading) return; // Prevent multiple loads
        dataLoading = true;

        // Load categories
        CategoryService.getInstance().fetchAllCategories(new BaseService.DataCallback<Category>() {
            @Override
            public void onSuccess(List<Category> categories) {
                cachedCategories = categories;
                categoriesLoaded = true;
                checkDataLoadedAndSetup();
        }

        @Override
            public void onError(String error) {
                Toast.makeText(DataViewActivity.this, "Error loading categories: " + error, Toast.LENGTH_SHORT).show();
                categoriesLoaded = true; // Mark as done even on error
                checkDataLoadedAndSetup();
            }
        });

        // Load music
        MusicService.getInstance().fetchAllMusic(new BaseService.DataCallback<Music>() {
            @Override
            public void onSuccess(List<Music> musicList) {
                cachedMusic = musicList;
                musicLoaded = true;
                checkDataLoadedAndSetup();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DataViewActivity.this, "Error loading music: " + error, Toast.LENGTH_SHORT).show();
                musicLoaded = true; // Mark as done even on error
                checkDataLoadedAndSetup();
            }
        });
    }

    /**
     * ✅ STEP 2: Wait for both categories and music to load, then setup chart
     */
    private void checkDataLoadedAndSetup() {
        if (categoriesLoaded && musicLoaded) {
            dataLoading = false;
            // Both data sources loaded - now we can setup the chart
            setupInitialBarChart();
        }
    }


    // Helper method to display the bar chart
    private void displayCategoryBarChart(Map<Integer, Integer> categoryDurations, int totalDuration) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<Integer, Integer> entry : categoryDurations.entrySet()) {
            // Get category by finding it from music
            Category category = getCategoryById(entry.getKey());
            if (category != null) {
                // Calculate percentage
                float percentage = (entry.getValue() * 100.0f) / totalDuration;
                entries.add(new BarEntry(index, percentage));
                labels.add(category.getName());
                index++;
            }
        }

        if (entries.isEmpty()) {
            Toast.makeText(this, "No category data available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create bar chart dataset
        BarDataSet dataSet = new BarDataSet(entries, "Category Listening Percentage");
        dataSet.setColor(Color.parseColor("#6A475A"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);

        // Chart styling
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDrawValueAboveBar(false);
        barChart.setFitBars(true);
        barChart.setExtraOffsets(0f, 0f, 20f, 0f);

        // X-Axis Styling
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        // Y-Axis Styling
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(110f);
        leftAxis.setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);

        // Load data and refresh
        barChart.setData(barData);
        barChart.animateY(1500);
        barChart.invalidate();
    }


    /**
     * Updates the bar chart to show music durations for a specific category.
     * @param categoryName The name of the selected category
     */
    private void updateBarChartForCategory(String categoryName) {
        // Find the category by name
        Category selectedCategory = findCategoryByName(categoryName);

        if (selectedCategory == null) {
            Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get all sessions for this patient (ASYNC)
        listeningSessionService.getSessionsByPatientId(patientId, new BaseService.DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> patientSessions) {
                // Filter sessions by category and group by music
                Map<Integer, Integer> musicDurations = new java.util.HashMap<>();

                for (ListeningSession session : patientSessions) {
                    if (session.getMusic() != null &&
                            session.getMusic().getCategory() != null &&
                            session.getMusic().getCategory().getId() == selectedCategory.getId()) {

                        int musicId = session.getMusic().getId();
                        musicDurations.put(musicId,
                                musicDurations.getOrDefault(musicId, 0) + session.getDuration());
                    }
                }

                if (musicDurations.isEmpty()) {
                    Toast.makeText(DataViewActivity.this, "No music data for " + categoryName, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Display the bar chart
                displayMusicBarChart(musicDurations, categoryName);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DataViewActivity.this, "Error loading data: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to display music bar chart
    private void displayMusicBarChart(Map<Integer, Integer> musicDurations, String categoryName) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<Integer, Integer> entry : musicDurations.entrySet()) {
            Music music = getMusicById(entry.getKey());
            if (music != null) {
                // Duration in seconds
                entries.add(new BarEntry(index, entry.getValue()));
                labels.add(music.getMusicName());
                index++;
            }
        }

        // Create bar chart dataset
        BarDataSet dataSet = new BarDataSet(entries, "Music Duration in " + categoryName + " (seconds)");
        dataSet.setColor(Color.parseColor("#8E44AD"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setDrawValueAboveBar(false);
        barChart.setExtraOffsets(0f, 0f, 20f, 0f);

        // Update the axis labels with the music titles
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Update Y-axis for duration (not percentage)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.resetAxisMaximum();
        leftAxis.setSpaceTop(10f);

        // Set the new data and refresh the chart
        barChart.setData(barData);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    // Helper method to find category by name (you'll need to implement this based on your CategoryService)
    private Category findCategoryByName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return null;
        }

        for (Category category : cachedCategories) {
            if (category.getName() != null &&
                    category.getName().equalsIgnoreCase(categoryName.trim())) {
                return category;
            }
        }

        return null;
    }



    private Music getMusicById(int musicId) {
        for (Music music : cachedMusic) {
            if (music.getId() == musicId) {
                return music;
            }
        }
        return null;
    }




    /**
     * Helper method to get a Category by its ID using MusicService.
     * @param categoryId The category ID
     * @return Category object or null if not found
     */
    private Category getCategoryById(int categoryId) {
        for (Category category : cachedCategories) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }



    private void setupNavigationListeners() {
        FrameLayout personButton = findViewById(R.id.nav_person_button);
        FrameLayout addButton = findViewById(R.id.nav_add_button);
        FrameLayout musicButton = findViewById(R.id.nav_music_button);
        FrameLayout consultantButton = findViewById(R.id.nav_group_button);
        ImageView chartViewIcon = findViewById(R.id.chart_icon);
        ImageView personIcon = findViewById(R.id.person_icon_top);

        personButton.setOnClickListener(v -> {
            Intent i = new Intent(DataViewActivity.this, UserListActivity.class);
            startActivity(i);
        });

        addButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(DataViewActivity.this);
            builder.setTitle("Choose an Action");
            builder.setMessage("What would you like to do?");
            builder.setPositiveButton("Add User", (dialog, which) -> {
                Intent i = new Intent(DataViewActivity.this, FormActivityPart1.class);
                startActivity(i);
            });
            builder.setNegativeButton("Add Song", (dialog, which) -> {
                Intent i = new Intent(DataViewActivity.this,UploadSongScreenActivity.class);
                startActivity(i);
            });
            builder.create().show();
        });

        musicButton.setOnClickListener(v -> {
            Intent i = new Intent(DataViewActivity.this, MusicListActivity.class);
            startActivity(i);
        });

        consultantButton.setOnClickListener(v -> {
            Intent i = new Intent(DataViewActivity.this, ConsultantListActivity.class);
            startActivity(i);
        });

        chartViewIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Chart view selected", Toast.LENGTH_SHORT).show();
        });

        personIcon.setOnClickListener(v -> {
            Intent intent = new Intent(DataViewActivity.this, ListViewClickActivity.class);
            intent.putExtra("PATIENT_ID", patientId);
            startActivity(intent);
        });

        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(DataViewActivity.this, UserListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });
    }
}
