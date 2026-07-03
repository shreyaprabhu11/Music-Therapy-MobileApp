package com.project.musicapp.features.Admin.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
// Import the necessary LiveData components
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.project.musicapp.R;
import com.project.musicapp.core.viewmodels.ConsultantViewModel;
import com.project.musicapp.features.Admin.adapters.ConsultantListAdapter;
import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.services.ConsultantService;

import java.util.ArrayList;
import java.util.List;

public class ConsultantListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.consultant_list_screen_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView consultantListView = findViewById(R.id.consultant_list_view);

        ConsultantViewModel viewModel = new ViewModelProvider(this).get(ConsultantViewModel.class);


        LiveData<List<Consultant>> consultantListLiveData = viewModel.getConsultants();



        consultantListLiveData.observe(this, new Observer<List<Consultant>>() {
            @Override
            public void onChanged(List<Consultant> consultants) {


                if (consultants != null) {
                    ConsultantListAdapter adapter = new ConsultantListAdapter(ConsultantListActivity.this, consultants);
                    consultantListView.setAdapter(adapter);
                }
            }
        });




        // Initialize Views
        ImageView userIconTop = findViewById(R.id.user_icon_top);
        ImageView chatIcon = findViewById(R.id.chat_icon);
        FrameLayout personButton = findViewById(R.id.nav_person_button);
        FrameLayout addButton = findViewById(R.id.nav_add_button);
        FrameLayout musicButton = findViewById(R.id.nav_music_button);
        FrameLayout consultantButton = findViewById(R.id.nav_group_button);


        personButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add your functionality here
                Intent i=new Intent(ConsultantListActivity.this,UserListActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);             }
        });

        // Set an OnClickListener for the Add icon
        addButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ConsultantListActivity.this);
            builder.setTitle("Choose an Action");
            builder.setMessage("What would you like to do?");
            builder.setPositiveButton("Add User", (dialog, which) -> {
                Intent i = new Intent(ConsultantListActivity.this, FormActivityPart1.class);
                startActivity(i);
            });
            builder.setNegativeButton("Add Song", (dialog, which) -> {
                Intent i = new Intent(ConsultantListActivity.this,UploadSongScreenActivity.class);
                startActivity(i);
            });
            builder.create().show();
        });

        // Set an OnClickListener for the Music icon
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(ConsultantListActivity.this,MusicListActivity.class);
                startActivity(i);
            }
        });

        // Set an OnClickListener for the Group icon
        consultantButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                Intent i=new Intent(ConsultantListActivity.this,ConsultantListActivity.class);
                startActivity(i);
            }
        });

        userIconTop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Add your functionality here
                Toast.makeText(ConsultantListActivity.this, "User Icon Clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        chatIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i=new Intent(ConsultantListActivity.this,FeedBackActivity.class);
                startActivity(i);
            }
        });
    }
}