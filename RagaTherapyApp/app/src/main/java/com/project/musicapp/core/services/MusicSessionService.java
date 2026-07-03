package com.project.musicapp.core.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.AudioAttributes;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.project.musicapp.MainActivity;
import com.project.musicapp.R;
import com.project.musicapp.features.patient.activities.MusicPlayerActivity;
import com.project.musicapp.core.models.Music;

import java.util.ArrayList;
import java.util.List;

public class
MusicSessionService extends MediaSessionService {

    public static final String ACTION_PLAY = "com.project.musicapp.action.PLAY";
    public static final String EXTRA_PLAYLIST = "com.project.musicapp.extra.PLAYLIST";
    public static final String EXTRA_INDEX = "com.project.musicapp.extra.INDEX";

    private ExoPlayer player;
    private MediaSession mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();

        // Audio attributes & ExoPlayer setup (your existing code)
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();

        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .build();

        // Notification setup
        String channelId = "music_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Playing music")
                .setContentText("Your music is playing")
                .setSmallIcon(R.drawable.default_image)
                .setContentIntent(PendingIntent.getActivity(
                        this, 0, new Intent(this, MainActivity.class),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        // ⚡ This is mandatory
        startForeground(1, notification);
        Intent activityIntent = new Intent(this, MusicPlayerActivity.class);
        activityIntent.setAction(Intent.ACTION_MAIN);
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent sessionActivity = PendingIntent.getActivity(
                this, 0, activityIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        mediaSession = new MediaSession.Builder(this, player)
                .setSessionActivity(sessionActivity)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null && ACTION_PLAY.equals(intent.getAction())) {
            ArrayList<Music> playlist = (ArrayList<Music>) intent.getSerializableExtra(EXTRA_PLAYLIST);
            int index = intent.getIntExtra(EXTRA_INDEX, 0);

            if (playlist != null && !playlist.isEmpty()) {
                List<MediaItem> items = new ArrayList<>();
                for (Music m : playlist) {
                    MediaMetadata metadata = new MediaMetadata.Builder()
                            .setTitle(m.getMusicName())
                            .setArtist(m.getMusicArtist())
                            .setArtworkUri(Uri.parse(m.getMusicImage()))
                            .build();

                    MediaItem mediaItem = new MediaItem.Builder()
                            .setUri(Uri.parse(m.getMusicUrl()))
                            .setMediaMetadata(metadata)
                            .build();
                    items.add(mediaItem);
                }

                player.setMediaItems(items, index, C.TIME_UNSET);
                player.prepare();
                player.play();
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        // Stop foreground and remove notification
        stopForeground(true);
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
}