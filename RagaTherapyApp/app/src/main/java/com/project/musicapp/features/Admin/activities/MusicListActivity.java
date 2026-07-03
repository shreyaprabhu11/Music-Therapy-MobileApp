package com.project.musicapp.features.Admin.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;

import com.project.musicapp.R;
import com.project.musicapp.core.models.Music;
import com.project.musicapp.core.viewmodels.MusicListViewModel;
import com.project.musicapp.features.Admin.adapters.MusicAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display and manage the music list
 * ✅ Fully integrated with LiveData for automatic UI updates
 */
public class MusicListActivity extends AppCompatActivity {

    // UI Components
    private ListView musicListView;
    private SearchView searchView;

    // Data and ViewModel
    private MusicListViewModel musicViewModel;
    private MusicAdapter adapter;
    private List<Music> fullMusicList = new ArrayList<>();

    // Search state
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list_admin);

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ViewModel
        musicViewModel = new MusicListViewModel();

        // Find all views
        initializeViews();

        // Setup navigation listeners
        setupNavigationListeners();

        // ✅ Setup ListView with LiveData-enabled adapter
        setupListView();

        // Setup search functionality
        setupSearch();

        // Clear search focus on list touch
        musicListView.setOnTouchListener((v, event) -> {
            searchView.clearFocus();
            return false;
        });
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        musicListView = findViewById(R.id.music_list_view);
        searchView = findViewById(R.id.search_view);
    }

    /**
     * ✅ Setup ListView with LiveData-observing adapter
     */
    private void setupListView() {
        // Get LiveData from ViewModel
        LiveData<List<Music>> musicLiveData = musicViewModel.getAllMusic();

        // Create adapter with LiveData
        adapter = new MusicAdapter(this, musicLiveData);
        musicListView.setAdapter(adapter);

        // ✅ Observe LiveData to maintain fullMusicList for search
        musicLiveData.observe(this, musicList -> {
            if (musicList != null) {
                fullMusicList = new ArrayList<>(musicList);

                // If there's an active search, reapply it
                if (!currentSearchQuery.isEmpty()) {
                    filterMusicList(currentSearchQuery);
                }
            }
        });

        // Setup click listener
        musicListView.setOnItemClickListener((parent, view, position, id) -> {
            Music clickedSong = adapter.getItem(position);
            if (clickedSong != null) {
                Intent intent = new Intent(MusicListActivity.this, SongDetailsScreenUIActivity.class);
                intent.putExtra("MUSIC_ID", clickedSong.getId());
                startActivity(intent);
            }
        });
    }

    /**
     * Setup search functionality
     */
    private void setupSearch() {
        // Configure SearchView appearance
        searchView.setIconifiedByDefault(false);
        int searchEditTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = searchView.findViewById(searchEditTextId);

        if (searchEditText != null) {
            searchEditText.setHint("Search Music");
            searchEditText.setHintTextColor(Color.DKGRAY);
        }

        // Show/hide cursor on focus change
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (searchEditText != null) {
                searchEditText.setCursorVisible(hasFocus);
            }
        });

        // Setup search query listeners
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                filterMusicList(newText);
                return true;
            }
        });
    }

    /**
     * ✅ Filter music list based on search query
     */
    private void filterMusicList(String query) {
        if (adapter == null) return;

        List<Music> filteredList = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            // Show all music
            filteredList.addAll(fullMusicList);
        } else {
            // Filter by music name
            String lowerQuery = query.toLowerCase().trim();
            for (Music music : fullMusicList) {
                if (music.getMusicName() != null &&
                        music.getMusicName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(music);
                }
            }
        }

        // Update adapter with filtered list
        adapter.updateData(filteredList);
    }

    /**
     * Setup all navigation click listeners
     */
    private void setupNavigationListeners() {
        ImageView userIconTop = findViewById(R.id.user_icon_top);
        ImageView chatIcon = findViewById(R.id.chat_icon);
        FrameLayout personButton = findViewById(R.id.nav_person_button);
        FrameLayout addButton = findViewById(R.id.nav_add_button);
        FrameLayout musicButton = findViewById(R.id.nav_music_button);
        FrameLayout consultantButton = findViewById(R.id.nav_group_button);

        // Navigate to User List
        personButton.setOnClickListener(v -> {
            Intent i = new Intent(MusicListActivity.this, UserListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        });

        // Show Add User/Song dialog
        addButton.setOnClickListener(v -> showAddDialog());

        // Already on Music List
        musicButton.setOnClickListener(v -> {
            Toast.makeText(MusicListActivity.this,
                    "Already on Music List screen",
                    Toast.LENGTH_SHORT).show();
        });

        // Navigate to Consultant List
        consultantButton.setOnClickListener(v -> {
            Intent i = new Intent(MusicListActivity.this, ConsultantListActivity.class);
            startActivity(i);
        });

        // User icon click
        userIconTop.setOnClickListener(v -> {
            Toast.makeText(MusicListActivity.this,
                    "User Icon Clicked!",
                    Toast.LENGTH_SHORT).show();
        });

        // Navigate to Feedback
        chatIcon.setOnClickListener(v -> {
            Intent i = new Intent(MusicListActivity.this, FeedBackActivity.class);
            startActivity(i);
        });
    }

    /**
     * Show dialog to add user or song
     */
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an Action")
                .setMessage("What would you like to do?")
                .setPositiveButton("Add User", (dialog, which) -> {
                    Intent i = new Intent(MusicListActivity.this, FormActivityPart1.class);
                    startActivity(i);
                })
                .setNegativeButton("Add Song", (dialog, which) -> {
                    Intent i = new Intent(MusicListActivity.this, UploadSongScreenActivity.class);
                    startActivity(i);
                })
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // LiveData automatically refreshes, but we can clear search if needed
        if (currentSearchQuery.isEmpty()) {
            // Data will auto-update via LiveData observer
        }
    }
}
