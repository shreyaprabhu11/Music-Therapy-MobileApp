package com.project.musicapp.features.Admin.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.musicapp.R;
import com.project.musicapp.core.models.ListeningSession;
import com.project.musicapp.core.models.Music;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.ListeningSessionService;
import com.project.musicapp.core.services.MusicService;
import com.project.musicapp.features.Admin.fragments.SongDetailsFragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Activity to display song details and listening trends
 * ✅ Fully integrated with async Firebase services
 */
public class SongDetailsScreenUIActivity extends AppCompatActivity {

    private MusicService musicService;
    private ListeningSessionService listeningSessionService;
    private int musicId;

    // UI Components
    private LineChart areaChart;
    private TextView chartTitle;
    private FrameLayout personButton;
    private FrameLayout addButton;
    private FrameLayout musicButton;
    private FrameLayout consultantButton;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_details_screen_ui);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Initialize Services (use getInstance() for singleton pattern)
        musicService = MusicService.getInstance();
        listeningSessionService = ListeningSessionService.getInstance();

        // Retrieve Song ID from Intent
        Intent intent = getIntent();
        musicId = intent.getIntExtra("MUSIC_ID", -1);

        // Check if music ID is valid
        if (musicId == -1) {
            Toast.makeText(this, "Error: No music ID received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        initializeViews();

        // Setup Navigation Listeners
        setupNavigationListeners();

        // ✅ Load music data async and setup UI
        loadMusicData();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        areaChart = findViewById(R.id.areaCharts);
        chartTitle = findViewById(R.id.chart_titles);
        personButton = findViewById(R.id.nav_person_button);
        addButton = findViewById(R.id.nav_add_button);
        musicButton = findViewById(R.id.nav_music_button);
        consultantButton = findViewById(R.id.nav_group_button);
        backArrow = findViewById(R.id.back_arrow);
    }

    /**
     * ✅ Load music data using async Firebase service
     */
    private void loadMusicData() {
        musicService.fetchMusicById(musicId, new BaseService.SingleItemCallback<Music>() {
            @Override
            public void onSuccess(Music music) {
                if (music != null) {
                    // Extract music details
                    String songName = music.getMusicName();
                    int songDuration = music.getDuration();
                    String albumArtUrl = music.getMusicImage();

                    // Set the chart title
                    if (songName != null && !songName.isEmpty()) {
                        chartTitle.setText(songName + "'s Listening Trend");
                    }

                    // Setup chart with listening sessions
                    setupSequentialSessionsChart(musicId);

                    // Inflate the fragment with music details
                    if (getSupportFragmentManager().findFragmentById(R.id.song_details_fragment_containers) == null) {
                        SongDetailsFragment fragment = SongDetailsFragment.newInstance(
                                songName,
                                songDuration,
                                albumArtUrl
                        );
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.song_details_fragment_containers, fragment)
                                .commit();
                    }
                } else {
                    Toast.makeText(SongDetailsScreenUIActivity.this,
                            "Error: Music not found",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SongDetailsScreenUIActivity.this,
                        "Error loading music: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * ✅ Setup area chart with async listening sessions data
     * X-axis = Session number (1st, 2nd, 3rd, etc.)
     * Y-axis = Duration in seconds for that session
     */
    private void setupSequentialSessionsChart(int musicId) {
        // Create a Music object with just the ID for filtering
        Music music = new Music();
        music.setId(musicId);

        // ✅ Get sessions async using the new service method
        listeningSessionService.getSessionsByMusic(music, new BaseService.DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> sessions) {
                if (sessions == null || sessions.isEmpty()) {
                    Toast.makeText(SongDetailsScreenUIActivity.this,
                            "No listening data available for this song",
                            Toast.LENGTH_SHORT).show();
                    areaChart.setNoDataText("No listening sessions recorded yet");
                    areaChart.invalidate();
                    return;
                }

                // Sort sessions by date (chronological order)
                Collections.sort(sessions, (s1, s2) -> Long.compare(s1.getDate(), s2.getDate()));

                // Create chart entries
                ArrayList<Entry> entries = new ArrayList<>();
                for (int i = 0; i < sessions.size(); i++) {
                    // X-axis = session number (0, 1, 2, 3...)
                    // Y-axis = duration for that session
                    entries.add(new Entry(i, sessions.get(i).getDuration()));
                }

                // Display the chart
                displayChart(entries, sessions.size());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SongDetailsScreenUIActivity.this,
                        "Error loading sessions: " + error,
                        Toast.LENGTH_SHORT).show();
                areaChart.setNoDataText("Error loading data");
                areaChart.invalidate();
            }
        });
    }

    /**
     * Display the area chart with session data
     */
    private void displayChart(ArrayList<Entry> entries, int sessionCount) {
        // Create the area chart dataset
        LineDataSet dataSet = new LineDataSet(entries, "Duration per Session (seconds)");
        dataSet.setColor(Color.parseColor("#00BCD4"));
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawFilled(true); // Makes it an area chart
        dataSet.setFillColor(Color.parseColor("#80DEEA"));
        dataSet.setDrawCircles(true); // Show points for each session
        dataSet.setCircleRadius(5f);
        dataSet.setCircleColor(Color.parseColor("#00BCD4"));
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);

        // Configure chart appearance
        areaChart.getDescription().setEnabled(false);
        areaChart.getLegend().setEnabled(true);
        areaChart.setTouchEnabled(true);
        areaChart.setDragEnabled(true);
        areaChart.setScaleEnabled(true);
        areaChart.setPinchZoom(true);

        // Configure X-axis
        XAxis xAxis = areaChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(true);
        xAxis.setLabelCount(Math.min(sessionCount, 10)); // Max 10 labels

        // Custom labels: "S1", "S2", "S3", etc.
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "S" + ((int) value + 1);
            }
        });

        // Configure Y-axis
        YAxis leftAxis = areaChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        areaChart.getAxisRight().setEnabled(false);

        // Set the data and refresh the chart
        areaChart.setData(lineData);
        areaChart.animateY(1500);
        areaChart.invalidate();
    }

    /**
     * Setup all navigation button listeners
     */
    private void setupNavigationListeners() {
        personButton.setOnClickListener(v -> {
            Intent i = new Intent(SongDetailsScreenUIActivity.this, UserListActivity.class);
            startActivity(i);
        });

        addButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SongDetailsScreenUIActivity.this);
            builder.setTitle("Choose an Action");
            builder.setMessage("What would you like to do?");
            builder.setPositiveButton("Add User", (dialog, which) -> {
                Intent i = new Intent(SongDetailsScreenUIActivity.this, FormActivityPart1.class);
                startActivity(i);
            });
            builder.setNegativeButton("Add Song", (dialog, which) -> {
                Intent i = new Intent(SongDetailsScreenUIActivity.this, UploadSongScreenActivity.class);
                startActivity(i);
            });
            builder.create().show();
        });

        musicButton.setOnClickListener(v -> {
            Intent i = new Intent(SongDetailsScreenUIActivity.this, MusicListActivity.class);
            startActivity(i);
        });

        consultantButton.setOnClickListener(v -> {
            Intent i = new Intent(SongDetailsScreenUIActivity.this, ConsultantListActivity.class);
            startActivity(i);
        });

        backArrow.setOnClickListener(v -> {
            finish();
        });
    }
}
