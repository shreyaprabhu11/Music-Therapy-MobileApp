package com.project.musicapp.features.Admin.adapters; // Use your app's package name

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.R;
import com.project.musicapp.core.models.PatientProfile;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends ArrayAdapter<PatientProfile> {

    public UserAdapter(@NonNull Context context, List<PatientProfile> userList) {
        super(context, 0, userList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_user, parent, false);
            holder = new ViewHolder();
            holder.userIcon = convertView.findViewById(R.id.item_user_icon);
            holder.userName = convertView.findViewById(R.id.item_user_name);
            holder.userId = convertView.findViewById(R.id.item_user_id);
            holder.userStage = convertView.findViewById(R.id.item_stage);
            // --- UPDATED: Find the new TextView for state ---
            holder.userState = convertView.findViewById(R.id.item_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PatientProfile currentPatient = getItem(position);

        if (currentPatient != null) {
            // Populate all the views with data from the Patient object
            holder.userName.setText(currentPatient.getUser().getName());
            holder.userId.setText(String.valueOf("ID: "+currentPatient.getId()));
            holder.userStage.setText("Stage: " + currentPatient.getSeverity());
            // --- UPDATED: Set the patient's state text ---

            holder.userState.setText(currentPatient.getState());

            // --- Set text colors explicitly to avoid theme overrides ---
            holder.userName.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            holder.userStage.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            holder.userId.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            // --- UPDATED: Set the state text color ---
            holder.userState.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        }

        return convertView;
    }

    /**
     * The ViewHolder class now includes a TextView for the state.
     */
    private static class ViewHolder {
        ImageView userIcon;
        TextView userName;
        TextView userId;
        TextView userStage;
        TextView userState; // --- UPDATED ---
    }
}

