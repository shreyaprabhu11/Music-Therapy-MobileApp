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
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.project.musicapp.R;
import com.project.musicapp.core.models.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Adapter for displaying Music items in a ListView.
 * ✅ NOW SUPPORTS LiveData<List<Music>> - automatically updates when data changes
 */
public class MusicAdapter extends ArrayAdapter<Music> {

    private Context context;
    private List<Music> musicList;

    /**
     * ✅ NEW: Constructor that accepts LiveData and observes changes
     */
    public MusicAdapter(@NonNull Context context, LiveData<List<Music>> liveDataMusicList) {
        super(context, 0, new ArrayList<>());
        this.context = context;
        this.musicList = new ArrayList<>();

        // ✅ Observe LiveData and update adapter when data changes
        if (context instanceof LifecycleOwner) {
            liveDataMusicList.observe((LifecycleOwner) context, new Observer<List<Music>>() {
                @Override
                public void onChanged(List<Music> music) {
                    if (music != null) {
                        updateData(music);
                    }
                }
            });
        }
    }

    /**
     * ✅ LEGACY: Constructor that accepts regular List (for backward compatibility)
     */
    public MusicAdapter(@NonNull Context context, List<Music> musicList) {
        super(context, 0, musicList);
        this.context = context;
        this.musicList = musicList != null ? musicList : new ArrayList<>();
    }

    /**
     * ✅ Update adapter data and notify changes
     */
    public void updateData(List<Music> newMusicList) {
        this.musicList.clear();
        if (newMusicList != null) {
            this.musicList.addAll(newMusicList);
        }
        clear();
        addAll(this.musicList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return musicList != null ? musicList.size() : 0;
    }

    @Override
    public Music getItem(int position) {
        return musicList != null && position < musicList.size() ? musicList.get(position) : null;
    }

    /**
     * Format duration from seconds to MM:SS format
     */
    private String formatDuration(int durationInSeconds) {
        if (durationInSeconds < 0) {
            return "Unknown"; // Handle -1 or invalid durations
        }

        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Use ViewHolder pattern for smooth scrolling
        ViewHolder holder;

        if (convertView == null) {
            // Inflate custom layout file
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_music_list_row, parent, false);

            holder = new ViewHolder();
            holder.albumArt = convertView.findViewById(R.id.img_card_music);
            holder.songTitle = convertView.findViewById(R.id.tv_card_music);
            holder.songDuration = convertView.findViewById(R.id.tv_music_duration);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data for the current position
        Music currentSong = getItem(position);

        // Populate the views with data
        if (currentSong != null) {
            holder.songTitle.setText(currentSong.getMusicName() != null ? currentSong.getMusicName() : "Unknown");
            holder.songDuration.setText(formatDuration(currentSong.getDuration()));

            // Load album art with Glide
            Glide.with(holder.albumArt.getContext())
                    .load(currentSong.getMusicImage())
                    .placeholder(R.drawable.ic_user_placeholder) // Shows while loading
                    .error(R.drawable.ic_user_placeholder) // Shows if load fails
                    .centerCrop()
                    .into(holder.albumArt);
        }

        return convertView;
    }

    /**
     * ViewHolder pattern for better performance
     */
    private static class ViewHolder {
        ImageView albumArt;
        TextView songTitle;
        TextView songDuration;
    }
}
