package com.project.musicapp.features.patient.adapters;

import static com.project.musicapp.core.utils.DateUtils.formatTime;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.musicapp.R;
import com.project.musicapp.features.patient.activities.MusicPlayerActivity;
import com.project.musicapp.core.models.Music;

import java.util.ArrayList;

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<Music> musicList;
    private ArrayList<Music> allMusicList; // 🔹 For search reference

    public MusicRecyclerViewAdapter(Context context, ArrayList<Music> musicList) {
        this.context = context;
        this.musicList = musicList;
        this.allMusicList = new ArrayList<>(musicList);
    }

    public void setMusicList(ArrayList<Music> musicList) {
        this.musicList = musicList;
        this.allMusicList = new ArrayList<>(musicList);
        notifyDataSetChanged();
    }

    // 🔍 Filter method
    public void filterList(String query) {
        ArrayList<Music> filteredList = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(allMusicList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Music music : allMusicList) {
                if (music.getMusicName() != null &&
                        music.getMusicName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(music);
                }
            }
        }
        this.musicList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_recycle_view_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Music music = musicList.get(position);
        holder.tvDuration.setText(formatTime(music.getDuration() * 1000L));
        holder.textView.setText(music.getMusicName());

        Glide.with(context)
                .load(music.getMusicImage())
                .placeholder(R.drawable.default_image)
                .error(R.drawable.gradient_bg_dark)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MusicPlayerActivity.class);
            intent.putExtra(MusicPlayerActivity.MUSIC_LIST, musicList);
            intent.putExtra(MusicPlayerActivity.SONG_POSITION, position);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_card_music);
            textView = itemView.findViewById(R.id.tv_card_music);
            tvDuration = itemView.findViewById(R.id.tv_music_duration);
        }
    }
}

