package com.project.musicapp.features.Admin.adapters;

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

import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.R;

import java.util.ArrayList;
import java.util.List;

public class AssignedConsultantAdapter extends ArrayAdapter<Consultant> {

    public AssignedConsultantAdapter(@NonNull Context context, List<Consultant> consultantList) {
        super(context, 0, consultantList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // Inflate your new custom layout (list_item_consultant.xml)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_consultant, parent, false);
            holder = new ViewHolder();
            holder.doctorImage = convertView.findViewById(R.id.doctor_image);
            holder.consultantId = convertView.findViewById(R.id.consultant_id);
            holder.consultantName = convertView.findViewById(R.id.consultant_name);
            holder.consultantRole = convertView.findViewById(R.id.consultant_role);
            holder.consultantEmail = convertView.findViewById(R.id.consultant_email);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data for the current item
        Consultant consultant = getItem(position);

        // Populate the views with data
        if (consultant != null) {
            holder.consultantId.setText(String.valueOf("ID :"+consultant.getUser().getId()));
            holder.consultantName.setText(consultant.getUser().getName());
            holder.consultantRole.setText(consultant.getDesignation());
            holder.consultantEmail.setText(consultant.getUser().getEmail());

            // Make the email link color black so it is visible
            holder.consultantEmail.setLinkTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        }

        return convertView;
    }

    // ViewHolder caches view lookups for smooth scrolling
    private static class ViewHolder {
        ImageView doctorImage;
        TextView consultantId;
        TextView consultantName;
        TextView consultantRole;
        TextView consultantEmail;
    }
}
