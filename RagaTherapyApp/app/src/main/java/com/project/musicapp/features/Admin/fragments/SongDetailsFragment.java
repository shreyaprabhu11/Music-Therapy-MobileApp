package com.project.musicapp.features.Admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.project.musicapp.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SongDetailsFragment extends Fragment {

    // Keys for passing data to the fragment
    private static final String ARG_SONG_NAME = "song_name";
    private static final String ARG_SONG_DURATION_SECONDS = "song_duration_seconds";
    private static final String ARG_ALBUM_ART_URL = "album_art_url";

    public SongDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * A factory method to create a new instance of this fragment with all necessary song details.
     * @param songName The name of the song.
     * @param durationInSeconds The duration of the song in total seconds.
     * @param albumArtUrl The URL for the album art image.
     * @return A new instance of fragment SongDetailsFragment.
     */
    public static SongDetailsFragment newInstance(String songName, int durationInSeconds, String albumArtUrl) {
        SongDetailsFragment fragment = new SongDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SONG_NAME, songName);
        args.putInt(ARG_SONG_DURATION_SECONDS, durationInSeconds);
        args.putString(ARG_ALBUM_ART_URL, albumArtUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.song_details_fragment_layout, container, false);

        // Find the views inside the fragment's layout
        ImageView albumArtImageView = view.findViewById(R.id.album_art);
        TextView songTitleTextView = view.findViewById(R.id.song_title);
        TextView songDurationTextView = view.findViewById(R.id.song_duration);

        // Get the data from the arguments and display it
        if (getArguments() != null) {
            String songName = getArguments().getString(ARG_SONG_NAME, "Unknown Song");
            int durationInSeconds = getArguments().getInt(ARG_SONG_DURATION_SECONDS, 0);
            String albumArtUrl = getArguments().getString(ARG_ALBUM_ART_URL);

            // Set the data on the views
            songTitleTextView.setText(songName);
            songDurationTextView.setText(formatDuration(durationInSeconds));

            // Load image from URL using Glide
            if (albumArtUrl != null && !albumArtUrl.isEmpty()) {
                Glide.with(this)
                        .load(albumArtUrl)
                        .placeholder(R.drawable.ic_album_art_placeholder)
                        .error(R.drawable.ic_album_art_placeholder)
                        .into(albumArtImageView);
            } else {
                // Fallback to placeholder if URL is null or empty
                albumArtImageView.setImageResource(R.drawable.ic_album_art_placeholder);
            }
        }

        return view;
    }

    /**
     * Helper method to convert seconds into a "mm:ss" formatted string.
     * @param seconds The total duration in seconds.
     * @return A formatted string like "03:42".
     */
    private String formatDuration(int seconds) {
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "Duration: %02d:%02d", minutes, remainingSeconds);
    }
}
