package com.project.musicapp.features.patient.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.musicapp.R;
import com.project.musicapp.features.patient.adapters.ConsultantRecyclerViewAdapter;
import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.viewmodels.ConsultantViewModel;

import java.util.List;

public class ConsultantListActivity extends AppCompatActivity {

    private ConsultantRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private ConsultantViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultant_list);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_consultant); // highlight Therapist

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_music) {
                startActivity(new Intent(this, MusicList.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_consultant) {
                return true; // already here
            }

            return false;
        });


        recyclerView = findViewById(R.id.recyc_consultant_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(ConsultantViewModel.class);
        viewModel.getConsultants().observe(this, this::displayConsultants);
    }

    private void displayConsultants(List<Consultant> consultantList) {
        adapter = new ConsultantRecyclerViewAdapter(this, consultantList);
        recyclerView.setAdapter(adapter);
    }
}
