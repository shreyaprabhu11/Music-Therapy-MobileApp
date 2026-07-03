package com.project.musicapp.core.models;

import java.io.Serializable;
import java.util.Date;

public class ListeningSession implements Serializable {
    int id;
    PatientProfile patientProfile;
    Music music;
    int duration;
    long date;

    public ListeningSession() {
    }

    public ListeningSession(int id, PatientProfile patientProfile, Music music, int duration, long date) {
        this.id = id;
        this.patientProfile = patientProfile;
        this.music = music;
        this.duration = duration;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PatientProfile getPatientProfile() {
        return patientProfile;
    }

    public void setPatientProfile(PatientProfile patientProfile) {
        this.patientProfile = patientProfile;
    }

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
