package com.project.musicapp.features.Admin.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;

import com.project.musicapp.core.services.FeedbackService;
import com.project.musicapp.core.viewmodels.FeedbackViewModel;
import com.project.musicapp.features.Admin.activities.ConsultantListActivity;
import com.project.musicapp.R;
import com.project.musicapp.features.Admin.adapters.FeedbackCustomAdapter;
import com.project.musicapp.core.models.Feedback;

import java.util.ArrayList;
import java.util.List;

public class FeedBackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.feedback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Find Views ---
        FrameLayout personButton = findViewById(R.id.nav_person_button);
        FrameLayout addButton = findViewById(R.id.nav_add_button);
        FrameLayout musicButton = findViewById(R.id.nav_music_button);
        FrameLayout consultantButton = findViewById(R.id.nav_group_button);
        ListView chatListView = findViewById(R.id.chat_list_view);
        ImageView backArrow = findViewById(R.id.back_arrow);

        // --- Create Data Source ---
        FeedbackViewModel obj=new FeedbackViewModel();

        LiveData<List<Feedback>> feedbackList = obj.getFeedbacks();

        // --- Create and Set the CUSTOM Adapter (The only one you need) ---
        FeedbackCustomAdapter feedbackAdapter = new FeedbackCustomAdapter(this, feedbackList);
        chatListView.setAdapter(feedbackAdapter);

        // --- Set Click Listeners ---
        setupListViewClickListener(chatListView, feedbackAdapter);

        personButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FeedBackActivity.this, UserListActivity.class);
                startActivity(i);
            }
        });

        addButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(FeedBackActivity.this);
            builder.setTitle("Choose an Action");
            builder.setMessage("What would you like to do?");
            builder.setPositiveButton("Add User", (dialog, which) -> {
                Intent i = new Intent(FeedBackActivity.this, FormActivityPart1.class);
                startActivity(i);
            });
            builder.setNegativeButton("Add Song", (dialog, which) -> {
                Intent i = new Intent(FeedBackActivity.this,UploadSongScreenActivity.class);
                startActivity(i);
            });
            builder.create().show();
        });
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FeedBackActivity.this, MusicListActivity.class);
                startActivity(i);
            }
        });

        consultantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FeedBackActivity.this, ConsultantListActivity.class);
                startActivity(i);
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedBackActivity.this, UserListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    /**
     * Sets up the click listener for the ListView to open the detail screen.
     */
    /**
     * Sets up the click listener for the ListView to open the detail screen.
     * ✅ UPDATED: Works with adapter's internal list
     */
    /**
     * Sets up the click listener for the ListView to open the detail screen.
     * ✅ UPDATED: Works with adapter's internal list
     */
    private void setupListViewClickListener(ListView chatListView, final FeedbackCustomAdapter adapter) {
        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Feedback selectedChat = adapter.getItem(position);  // ✅ Use parameter, not class field
                if (selectedChat != null) {
                    Intent intent = new Intent(FeedBackActivity.this, FeedbackFullActivity.class);
                    intent.putExtra("ID", selectedChat.getId());
                    startActivity(intent);
                }
            }
        });
    }


}

