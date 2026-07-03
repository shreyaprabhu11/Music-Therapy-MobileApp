package com.project.musicapp.features.Admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.project.musicapp.R;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.viewmodels.PatientProfileViewModel;
import com.project.musicapp.features.Admin.adapters.UserAdapter;
import com.project.musicapp.features.consultant.activities.TimeSlotCreation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * ✅ Updated to use ViewModel with proper LiveData observation
 */
public class UserListActivity extends AppCompatActivity {

    // ✅ ViewModel with lifecycle awareness
    private PatientProfileViewModel patientViewModel;

    // ✅ Regular List for caching and filtering
    private List<PatientProfile> fullUserList = new ArrayList<>();
    private UserAdapter listAdapter;
    Spinner sortBySpinner;
    private ListView userListView;
    private EditText searchEditText;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent i=getIntent();
        role=i.getStringExtra("USERROLE");



        // ✅ Initialize ViewModel properly with ViewModelProvider
        patientViewModel = new ViewModelProvider(this).get(PatientProfileViewModel.class);

        // Initialize all views
        initializeViews();

        // Setup UI components
        setupNavigationListeners();
        setupSearchFunctionality();
        setupListView();
        setupSpinner(sortBySpinner);

        // ✅ Observe LiveData for patient profiles
        observePatientProfiles();
    }

    /**
     * Initialize all views from the XML layout
     */
    private void initializeViews() {
        ImageView userIconTop = findViewById(R.id.user_icon_top);
        ImageView chatIcon = findViewById(R.id.chat_icon);
        FrameLayout personButton = findViewById(R.id.nav_person_button);
        FrameLayout addButton = findViewById(R.id.nav_add_button);
        FrameLayout musicButton = findViewById(R.id.nav_music_button);
        FrameLayout consultantButton = findViewById(R.id.nav_group_button);
        sortBySpinner = findViewById(R.id.sort_by_button);
        RelativeLayout searchContainer = findViewById(R.id.search_container);
        searchEditText = (EditText) searchContainer.getChildAt(0);
        ImageView searchIcon = findViewById(R.id.search_icon);
        Button excelButton = findViewById(R.id.excel_data_button);
        userListView = findViewById(R.id.user_list_view);

        searchIcon.setOnTouchListener((v, event) -> {
            searchEditText.clearFocus();
            return false;
        });

        excelButton.setOnClickListener(v -> {
            Intent i = new Intent(UserListActivity.this, ExcelFormGeneration.class);
            startActivity(i);
        });

        // Setup navigation listeners
        setupNavigationListeners(userIconTop, chatIcon, personButton, addButton, musicButton, consultantButton);

        // Setup spinner
        setupSpinner(sortBySpinner);
    }

    /**
     * ✅ Observe LiveData from ViewModel
     */
    private void observePatientProfiles() {
        patientViewModel.getAllProfiles().observe(this, profiles -> {
            if (profiles != null) {
                fullUserList = new ArrayList<>(profiles);

                // Update adapter with new data
                if (listAdapter == null) {
                    // First time - create adapter
                    listAdapter = new UserAdapter(this, fullUserList);
                    userListView.setAdapter(listAdapter);
                } else {
                    // Update existing adapter
                    listAdapter.clear();
                    listAdapter.addAll(fullUserList);
                    listAdapter.notifyDataSetChanged();
                }

                // Reapply search filter if active
                String currentQuery = searchEditText.getText().toString();
                if (!currentQuery.isEmpty()) {
                    filterPatientList(currentQuery);
                }
            }
        });
    }

    /**
     * Initialize the ListView and set up item click listeners
     */
    private void setupListView() {
        // ListView setup will complete in observePatientProfiles()
        userListView.setOnItemClickListener((parent, view, position, id) -> {
            PatientProfile clickedUser = listAdapter.getItem(position);
            if (clickedUser != null) {
                Intent intent = new Intent(UserListActivity.this, ListViewClickActivity.class);
                intent.putExtra("PATIENT", clickedUser);
                startActivity(intent);
            }
        });

        // Clear focus from search bar when list is touched
        userListView.setOnTouchListener((v, event) -> {
            searchEditText.clearFocus();
            return false;
        });
    }

    /**
     * Sets up the search functionality with real-time filtering
     */
    private void setupSearchFunctionality() {
        // Remove hint on focus
        CharSequence originalHint = searchEditText.getHint();
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                searchEditText.setHint("");
            } else {
                if (searchEditText.getText().toString().isEmpty()) {
                    searchEditText.setHint(originalHint);
                }
            }
        });

        // Add TextWatcher to filter list as user types
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatientList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * ✅ Filters the patient list based on search query
     */
    private void filterPatientList(String query) {
        if (listAdapter == null || fullUserList == null) return;

        ArrayList<PatientProfile> filteredList = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            // Show full list
            filteredList.addAll(fullUserList);
        } else {
            // Filter by name
            String lowerQuery = query.toLowerCase(Locale.ROOT);
            for (PatientProfile patient : fullUserList) {
                if (patient.getUser() != null &&
                        patient.getUser().getName() != null &&
                        patient.getUser().getName().toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                    filteredList.add(patient);
                }
            }
        }

        // Update adapter
        listAdapter.clear();
        listAdapter.addAll(filteredList);
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Sets up the Spinner for sorting options
     */
    private void setupSpinner(Spinner sortBySpinner) {
        String[] sortOptions = {"Sort by State", "Sort by Time"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, sortOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortBySpinner.setAdapter(adapter);

        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listAdapter == null || listAdapter.getCount() == 0) return;

                // Get current list from adapter
                ArrayList<PatientProfile> currentList = new ArrayList<>();
                for (int i = 0; i < listAdapter.getCount(); i++) {
                    currentList.add(listAdapter.getItem(i));
                }

                if (position == 0) {
                    // Sort by State (Alphabetically)
                    Collections.sort(currentList, (p1, p2) -> {
                        if (p1 == null || p1.getState() == null) return 1;
                        if (p2 == null || p2.getState() == null) return -1;
                        return p1.getState().compareToIgnoreCase(p2.getState());
                    });
                } else if (position == 1) {
                    // Sort by Total Listening Time (Descending)
                    Collections.sort(currentList, (p1, p2) -> {
                        if (p1 == null && p2 == null) return 0;
                        if (p1 == null) return 1;
                        if (p2 == null) return -1;
                        return Integer.compare(
                                p2.getTotalListeningDuration(),
                                p1.getTotalListeningDuration()
                        );
                    });
                }

                // Update adapter with sorted list
                listAdapter.clear();
                listAdapter.addAll(currentList);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Helper method to setup navigation listeners (for clean code)
     */
    private void setupNavigationListeners() {
        // Will be called with views from initializeViews()
    }

    /**
     * Sets up click listeners for all navigation buttons
     */
    private void setupNavigationListeners(ImageView userIconTop, ImageView chatIcon,
                                          FrameLayout personButton, FrameLayout addButton,
                                          FrameLayout musicButton, FrameLayout consultantButton) {
        // Person button - stay on same activity
        personButton.setOnClickListener(v -> {
            Toast.makeText(this, "Already on User List screen", Toast.LENGTH_SHORT).show();
        });

        // Add button - show dialog
        addButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserListActivity.this);
            builder.setTitle("Choose an Action")
                    .setMessage("What would you like to do?")
                    .setPositiveButton("Add User", (dialog, which) -> {
                        Intent i = new Intent(UserListActivity.this, FormActivityPart1.class);
                        startActivity(i);
                    })
                    .setNegativeButton("Add Song", (dialog, which) -> {
                        Intent i = new Intent(UserListActivity.this, UploadSongScreenActivity.class);
                        startActivity(i);
                    })
                    .create().show();
        });

        // Music button
        musicButton.setOnClickListener(v -> {
            Intent i = new Intent(UserListActivity.this, MusicListActivity.class);
            startActivity(i);
        });

        // Consultant button
        consultantButton.setOnClickListener(v -> {
            Intent i=null;
            if(!role.equals("Consultant"))
                i = new Intent(UserListActivity.this, ConsultantListActivity.class);
            else if(role.equals("Consultant"))
                i = new Intent(UserListActivity.this, TimeSlotCreation.class);
            startActivity(i);
        });

        // User icon
        userIconTop.setOnClickListener(v -> {
            Toast.makeText(UserListActivity.this, "User Icon Clicked!", Toast.LENGTH_SHORT).show();
        });

        // Chat/Feedback icon
        chatIcon.setOnClickListener(v -> {
            Intent i = new Intent(UserListActivity.this, FeedBackActivity.class);
            startActivity(i);
        });
    }
}
