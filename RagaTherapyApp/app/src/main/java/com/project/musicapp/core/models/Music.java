package com.project.musicapp.core.models;

public class Music implements java.io.Serializable{
    int id;
    String musicName;
    String musicImage;
    String musicUrl;
    String musicArtist;
    int duration;
    Category category;

    public Music() {
    }

    public Music(String musicName, String musicImage) {
        this.musicName = musicName;
        this.musicImage = musicImage;
    }

    public Music(int id, Category category, String musicName, String musicImage, String musicUrl, int duration) {
        this.id = id;
        this.category = category;
        this.musicName = musicName;
        this.musicImage = musicImage;
        this.musicUrl = musicUrl;
        this.musicArtist =null;
        this.duration = duration;
    }

    public Music(int id, Category category, String musicName, String musicImage, String musicUrl, String musicArtist, int duration) {
        this.id = id;
        this.category = category;
        this.musicName = musicName;
        this.musicImage = musicImage;
        this.musicUrl = musicUrl;
        this.musicArtist = musicArtist;
        this.duration = duration;
    }


    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicImage() {
        return musicImage;
    }

    public void setMusicImage(String musicImage) {
        this.musicImage = musicImage;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
