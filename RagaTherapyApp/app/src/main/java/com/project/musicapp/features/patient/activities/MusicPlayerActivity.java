package com.project.musicapp.features.patient.activities;

import static com.project.musicapp.core.utils.DateUtils.formatTime;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;
import com.project.musicapp.R;
import com.project.musicapp.core.models.ListeningSession;
import com.project.musicapp.core.models.Music;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.services.ListeningSessionService;
import com.project.musicapp.core.services.MusicSessionService;
import com.project.musicapp.core.utils.DateUtils;
import com.project.musicapp.core.viewmodels.PatientProfileViewModel;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class MusicPlayerActivity extends AppCompatActivity {

    public static final String MUSIC_LIST = "MUSIC_LIST";
    public static final String SONG_POSITION = "SONG_POSITION";

    private ImageView bgImage, musicImage, btnPlayPause, btnNext, btnPrev;
    private TextView titleView, currentTimeView, totalTimeView;
    private SeekBar seekBar;

    private ArrayList<Music> musicList;
    private int currentIndex;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaController mediaController;
    private ListenableFuture<MediaController> controllerFuture;

    private final HashMap<String, Long> timeSpentMap = new HashMap<>();
    private long lastUpdateTime = 0;

    private PatientProfileViewModel profileViewModel;
    private PatientProfile patient; // will be loaded asynchronously

    int userId; // replace with actual logged-in user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_music_player);

        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", -1); // -1 means not found

        // Initialize ViewModel
        profileViewModel = new PatientProfileViewModel();
        observePatientProfile(); // fetch patient asynchronously

        initViews();
        handleIntent(getIntent());
        startMusicService();
        connectMediaController();
        setupButtonsAndSeekBar();

        findViewById(R.id.btn_drop).setOnClickListener(view -> navigateBackToList());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToList();
            }
        });
    }

    /**
     * Observe patient profile asynchronously via LiveData
     */
    private void observePatientProfile() {
        profileViewModel.fetchProfileById(userId); // async fetch from service

        profileViewModel.getSelectedProfile().observe(this, fetchedPatient -> {
            if (fetchedPatient != null) {
                patient = fetchedPatient;
                Log.d("PatientLoader", "✅ Patient loaded: ID=" + patient.getId());
            } else {
                Log.e("PatientLoader", "❌ Patient with User ID " + userId + " not found.");
            }
        });
    }

    private void initViews() {
        bgImage = findViewById(R.id.bg_image);
        musicImage = findViewById(R.id.image_music);
        btnPlayPause = findViewById(R.id.btn_pause_play);
        btnNext = findViewById(R.id.btn_next);
        btnPrev = findViewById(R.id.btn_previous);
        titleView = findViewById(R.id.text_music_title);
        seekBar = findViewById(R.id.seek_bar);
        currentTimeView = findViewById(R.id.tv_current_time);
        totalTimeView = findViewById(R.id.tv_total_time);
    }

    private void handleIntent(Intent intent) {
        musicList = (ArrayList<Music>) intent.getSerializableExtra(MUSIC_LIST);
        currentIndex = intent.getIntExtra(SONG_POSITION, 0);
    }

    private void startMusicService() {
        Intent startIntent = new Intent(this, MusicSessionService.class);
        startIntent.setAction(MusicSessionService.ACTION_PLAY);
        startIntent.putExtra(MusicSessionService.EXTRA_PLAYLIST, musicList);
        startIntent.putExtra(MusicSessionService.EXTRA_INDEX, currentIndex);
        ContextCompat.startForegroundService(this, startIntent);
    }

    private void connectMediaController() {
        SessionToken token = new SessionToken(this, new ComponentName(this, MusicSessionService.class));
        controllerFuture = new MediaController.Builder(this, token).buildAsync();

        controllerFuture.addListener(() -> {
            try {
                mediaController = controllerFuture.get();
                mediaController.addListener(new Player.Listener() {

                    @OptIn(markerClass = UnstableApi.class)
                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        btnPlayPause.setImageResource(isPlaying ? R.drawable.outline_pause_circle_64
                                : R.drawable.outline_play_circle_64);
                        if (isPlaying) {
                            lastUpdateTime = System.currentTimeMillis();
                            updateSeekBarLoop();
                        } else {
                            trackPlaybackTime();
                        }
                    }

                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (playbackState == Player.STATE_READY) {
                            long duration = mediaController.getDuration();
                            if (duration > 0) {
                                seekBar.setMax((int) (duration / 1000));
                                totalTimeView.setText(formatTime(duration));
                            }
                        }
                    }

                    @Override
                    public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                        updateUIFromMediaItem(mediaItem);
                    }
                });

                MediaItem currentItem = mediaController.getCurrentMediaItem();
                if (currentItem != null) updateUIFromMediaItem(currentItem);

                syncUIFromController();

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void setupButtonsAndSeekBar() {
        btnPlayPause.setOnClickListener(v -> {
            if (mediaController == null) return;
            if (mediaController.isPlaying()) mediaController.pause();
            else mediaController.play();
        });

        btnNext.setOnClickListener(v -> {
            if (mediaController == null) return;
            mediaController.seekToNextMediaItem();
        });

        btnPrev.setOnClickListener(v -> {
            if (mediaController == null) return;
            mediaController.seekToPreviousMediaItem();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser && mediaController != null) {
                    mediaController.seekTo(progress * 1000L);
                    lastUpdateTime = System.currentTimeMillis();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateUIFromMediaItem(MediaItem item) {
        if (item == null || item.mediaMetadata == null) return;
        MediaMetadata md = item.mediaMetadata;

        String title = md.title != null ? md.title.toString() : "Unknown";
        titleView.setText(title);

        if (md.artworkUri != null) {
            String art = md.artworkUri.toString();
            Glide.with(this).load(art)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.gradient_bg_dark)
                    .into(musicImage);

            Glide.with(this)
                    .load(art)
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25)))
                    .into(bgImage);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void updateSeekBarLoop() {
        if (mediaController == null || !mediaController.isPlaying()) return;

        long pos = mediaController.getCurrentPosition();
        seekBar.setProgress((int) (pos / 1000));
        currentTimeView.setText(formatTime(pos));

        trackPlaybackTime();

        handler.postDelayed(this::updateSeekBarLoop, 1000);
    }

    @UnstableApi
    private void trackPlaybackTime() {
        MediaItem item = mediaController.getCurrentMediaItem();
        if (item != null && item.playbackProperties != null) {
            String key = item.playbackProperties.uri.toString();
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - lastUpdateTime;
            long spent = timeSpentMap.getOrDefault(key, 0L);
            timeSpentMap.put(key, spent + elapsed);
            lastUpdateTime = currentTime;
        }
    }

    private void syncUIFromController() {
        if (mediaController == null) return;
        MediaItem item = mediaController.getCurrentMediaItem();
        updateUIFromMediaItem(item);

        btnPlayPause.setImageResource(mediaController.isPlaying() ? R.drawable.outline_pause_circle_64
                : R.drawable.outline_play_circle_64);

        long duration = mediaController.getDuration();
        if (duration > 0) {
            seekBar.setMax((int) (duration / 1000));
            totalTimeView.setText(formatTime(duration));
        }

        handler.removeCallbacksAndMessages(null);
        if (mediaController.isPlaying()) updateSeekBarLoop();
    }

    private void navigateBackToList() {
        Intent intent = new Intent(MusicPlayerActivity.this, MusicList.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (patient != null) {
            logDailyMusicTimes();
        } else {
            Log.w("ListeningLogger", "Patient not loaded. Skipping daily music logging.");
        }

        if (mediaController != null) {
            mediaController.stop();
            mediaController.release();
            mediaController = null;
        }

        Intent stopIntent = new Intent(this, MusicSessionService.class);
        stopService(stopIntent);

        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Logs and updates today's listening sessions asynchronously
     */
    private void logDailyMusicTimes() {
        if (patient == null) {
            Log.e("ListeningLogger", "❌ Patient not loaded. Skipping daily music logging.");
            return;
        }

        if (musicList == null || musicList.isEmpty() || timeSpentMap.isEmpty()) {
            Log.w("ListeningLogger", "⚠ No music or time data to log.");
            return;
        }

        ListeningSessionService sessionService = ListeningSessionService.getInstance();

        for (String musicUrl : timeSpentMap.keySet()) {
            long secondsPlayed = timeSpentMap.get(musicUrl) / 1000;

            // Find Music object
            Music currentMusic = null;
            for (Music m : musicList) {
                if (m.getMusicUrl().equals(musicUrl)) {
                    currentMusic = m;
                    break;
                }
            }

            if (currentMusic == null) {
                Log.w("ListeningLogger", "⚠ Music not found for URL: " + musicUrl);
                continue;
            }

            // Async add or update session
            sessionService.addOrUpdateTodaySessionAsync(patient, currentMusic, (int) secondsPlayed);
        }

        // After all updates, log all today's sessions
        sessionService.fetchAllSessionsAsync(sessions -> {
            if (sessions == null || sessions.isEmpty()) {
                Log.d("ListeningLogger", "No listening sessions found for today.");
                return;
            }

            Log.d("ListeningLogger", "🎧 --- Today's Listening Sessions ---");
            long now = System.currentTimeMillis();

            for (ListeningSession session : sessions) {
                if (session.getPatientProfile() == null || session.getMusic() == null) continue;

                if (ListeningSessionService.isSameDay(session.getDate(), now)
                        && session.getPatientProfile().getId() == patient.getId()) {

                    Log.d("ListeningLogger",
                            "🎵 Music: " + session.getMusic().getMusicName() +
                                    ", 🔗 URL: " + session.getMusic().getMusicUrl() +
                                    ", ⏱ Duration: " + session.getDuration() + " sec" +
                                    ", 📅 Date: " + new Date(session.getDate()) +
                                    ", 👤 Patient: " + session.getPatientProfile().getId() + " - "
                                    + session.getPatientProfile().getState()
                    );
                }
            }
        });
    }



    /**
     * Print all today's listening sessions in Logcat
     */
    private void logTodaySessionsDirect() {
        ListeningSessionService service = ListeningSessionService.getInstance();
        Date today = new Date();
        List<ListeningSession> allSessions = service.fetchAllSessions();

        if (allSessions == null || allSessions.isEmpty()) {
            Log.d("MusicTracker", "No listening sessions found for today.");
            return;
        }

        Log.d("MusicTracker", "🎧 --- Today's Listening Sessions ---");

        for (ListeningSession session : allSessions) {
            long sessionMillis = session.getDate();
            long todayMillis = System.currentTimeMillis();

            if (DateUtils.isSameDay(sessionMillis, todayMillis)) {
                Music music = session.getMusic();
                PatientProfile patientProfile = session.getPatientProfile();
                Log.d("MusicTracker",
                        "🎵 Music: " + (music != null ? music.getMusicName() : "Unknown") +
                                ", 🔗 URL: " + (music != null ? music.getMusicUrl() : "Unknown") +
                                ", ⏱ Duration: " + session.getDuration() +
                                " sec, 📅 Date: " + session.getDate() +
                                ", 👤 Patient: " + (patientProfile != null ? patientProfile.getId() + " - " + patientProfile.getState() : "Unknown")
                );
            }
        }
    }
}
