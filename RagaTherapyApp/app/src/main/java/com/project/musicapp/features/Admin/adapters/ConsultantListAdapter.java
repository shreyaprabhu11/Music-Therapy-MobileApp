package com.project.musicapp.features.Admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.R;

import java.util.ArrayList;
import java.util.List;

public class ConsultantListAdapter extends ArrayAdapter<Consultant> {

    public ConsultantListAdapter(@NonNull Context context, List<Consultant> consultantList) {
        super(context, 0, consultantList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // ViewHolder pattern for performance
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_consultant_access, parent, false);
            holder = new ViewHolder();
            holder.consultantImage = convertView.findViewById(R.id.consultant_image);
            holder.consultantUserId = convertView.findViewById(R.id.consultant_user_id);
            holder.consultantName = convertView.findViewById(R.id.consultant_name);
            holder.consultantRole = convertView.findViewById(R.id.consultant_role);
            holder.consultantEmail = convertView.findViewById(R.id.consultant_email);
            holder.accessSwitch = convertView.findViewById(R.id.access_switch);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data item for this position
        Consultant consultant = getItem(position);

        // Populate the data into the template view using the data object
        if (consultant != null) {
            holder.consultantName.setText(consultant.getUser().getName());
            holder.consultantUserId.setText("ID: "+consultant.getUser().getId());
            holder.consultantRole.setText(consultant.getDesignation());
            holder.consultantEmail.setText(consultant.getUser().getEmail());

            // Set the switch state without triggering the listener
            holder.accessSwitch.setOnCheckedChangeListener(null);
            holder.accessSwitch.setChecked(consultant.getAccessInfo());

            // Re-attach the listener with the two-step dialog confirmation
            holder.accessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Show the first confirmation dialog only when turning the switch ON
                    new AlertDialog.Builder(getContext())
                            .setTitle("Confirm Access")
                            .setMessage("Are you sure you want to allow all the Patient's Data Access?")
                            .setPositiveButton("ALLOW", (dialog, which) -> {
                                // User clicked Allow, show the second dialog
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Access Granted")
                                        .setMessage("Patient's Data Access Granted")
                                        .setPositiveButton("OK", (dialog2, which2) -> {
                                            // User clicked OK, now update the model
                                            consultant.setAccessInfo(true);
                                            Toast.makeText(getContext(), "Access granted for " + consultant.getUser().getName(), Toast.LENGTH_SHORT).show();
                                        })
                                        .setOnCancelListener(dialog2 -> {
                                            // Also handle if they dismiss this second dialog
                                            consultant.setAccessInfo(true);
                                            Toast.makeText(getContext(), "Access granted for " + consultant.getUser().getName(), Toast.LENGTH_SHORT).show();
                                        })
                                        .show();
                            })
                            .setNegativeButton("CANCEL", (dialog, which) -> {
                                // User clicked Cancel, revert the switch and show a toast
                                buttonView.setChecked(false); // Revert the switch to OFF state
                                Toast.makeText(getContext(), "Access grant cancelled.", Toast.LENGTH_SHORT).show();
                            })
                            .setOnCancelListener(dialog -> {
                                // Also handle if they dismiss the dialog (e.g., by pressing back)
                                buttonView.setChecked(false); // Revert the switch
                            })
                            .show();
                } else {
                    // If the switch is being turned OFF, just update the model directly
                    consultant.setAccessInfo(false);
                    Toast.makeText(getContext(), "Access revoked for " + consultant.getUser().getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView consultantImage;
        TextView consultantUserId;
        TextView consultantName;
        TextView consultantRole;
        TextView consultantEmail;
        Switch accessSwitch;
    }
}

