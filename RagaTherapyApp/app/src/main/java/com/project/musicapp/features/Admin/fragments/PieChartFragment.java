package com.project.musicapp.features.Admin.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project.musicapp.R;
import com.project.musicapp.core.models.ListeningSession;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.ListeningSessionService;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.project.musicapp.core.viewmodels.MusicListViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ✅ Updated PieChartFragment with async Firebase callbacks
 * Uses singleton pattern for services and proper async handling
 */
public class PieChartFragment extends Fragment implements OnChartValueSelectedListener {

    private static final String ARG_PATIENT_ID = "patient_id";

    private PieChart pieChart;
    private OnPieSliceSelectedListener mListener;
    private int patientId;

    // ✅ Use singleton instances
    private ListeningSessionService listeningSessionService;
    private MusicListViewModel musicViewModel;

    // --- Communication interface ---
    public interface OnPieSliceSelectedListener {
        void onSliceSelected(String categoryName);
    }

    /**
     * Factory method to create a new instance of this fragment using the provided parameters.
     * @param patientId The patient ID to load data for
     * @return A new instance of fragment PieChartFragment.
     */
    public static PieChartFragment newInstance(int patientId) {
        PieChartFragment fragment = new PieChartFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PATIENT_ID, patientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patientId = getArguments().getInt(ARG_PATIENT_ID, -1);
        }

        // ✅ Initialize singleton services
        listeningSessionService = ListeningSessionService.getInstance();
        musicViewModel = new MusicListViewModel();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Attach the listener to the hosting Activity
        if (context instanceof OnPieSliceSelectedListener) {
            mListener = (OnPieSliceSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPieSliceSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        pieChart = view.findViewById(R.id.pieChart);

        // Load real patient data
        loadPatientData();

        return view;
    }

    /**
     * ✅ Load real patient listening data from Firebase with async callbacks
     */
    private void loadPatientData() {
        if (patientId == -1) {
            // Load default/sample data if no patient ID
            loadSampleData();
            return;
        }

        // ✅ Use async callback to fetch sessions by patient ID
        listeningSessionService.getSessionsByPatientId(patientId, new BaseService.DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> patientSessions) {
                if (patientSessions.isEmpty()) {
                    // No data, load sample
                    loadSampleData();
                    return;
                }

                // Calculate category durations
                Map<String, Integer> categoryDurations = new HashMap<>();
                int totalDuration = 0;

                for (ListeningSession session : patientSessions) {
                    if (session.getMusic() != null && session.getMusic().getCategory() != null) {
                        String categoryName = session.getMusic().getCategory().getName();
                        int duration = session.getDuration();

                        categoryDurations.put(categoryName,
                                categoryDurations.getOrDefault(categoryName, 0) + duration);

                        totalDuration += duration;
                    }
                }

                // Handle case where no valid sessions with music/category
                if (totalDuration == 0) {
                    loadSampleData();
                    return;
                }

                // Prepare the data entries with percentages
                ArrayList<PieEntry> entries = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : categoryDurations.entrySet()) {
                    float percentage = (entry.getValue() * 100.0f) / totalDuration;
                    entries.add(new PieEntry(percentage, entry.getKey()));
                }

                setupPieChart(entries, "Music Categories");
            }

            @Override
            public void onError(String error) {
                // Show error and load sample data
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading data: " + error, Toast.LENGTH_SHORT).show();
                }
                loadSampleData();
            }
        });
    }

    /**
     * Load sample data if no patient data is available
     */
    private void loadSampleData() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(45f, "Rock"));
        entries.add(new PieEntry(80f, "Pop"));
        entries.add(new PieEntry(62f, "Jazz"));
        entries.add(new PieEntry(25f, "Classical"));
        entries.add(new PieEntry(55f, "Hip Hop"));

        setupPieChart(entries, "Music Genres");
    }

    /**
     * Setup and style the pie chart with data
     */
    private void setupPieChart(ArrayList<PieEntry> entries, String label) {
        // Create and style the DataSet
        PieDataSet dataSet = new PieDataSet(entries, label);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);

        // Customize the chart
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setCenterText("Categories");
        pieChart.setCenterTextSize(20f);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setExtraOffsets(10f, 10f, 10f, 10f);

        // Set data and listener
        pieChart.setData(pieData);
        pieChart.animateY(1400);
        pieChart.invalidate();
        pieChart.setOnChartValueSelectedListener(this);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null) {
            return;
        }

        PieEntry pieEntry = (PieEntry) e;
        String categoryName = pieEntry.getLabel();

        // Trigger the interface method
        if (mListener != null) {
            mListener.onSliceSelected(categoryName);
        }
    }

    @Override
    public void onNothingSelected() {
        // You could potentially reset the bar chart here if needed
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null; // Clean up the listener
    }
}
