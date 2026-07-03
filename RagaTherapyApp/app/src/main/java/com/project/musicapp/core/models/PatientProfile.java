package com.project.musicapp.core.models;

import com.project.musicapp.core.services.ListeningSessionService;

import java.io.Serializable;
import java.util.List;

/**
 * PatientProfile model with async integration for ListeningSessionService
 */
public class PatientProfile implements Serializable {

    int id;
    String severity;
    List<Consultant> consultants;
    String state, city, urbanRural, address, emergencyContact;
    User user;

    // ✅ Cached total listening duration (updated async)
    private int totalListeningDuration = 0;

    public PatientProfile() {
    }

    public PatientProfile(int id, User user, String severity, List<Consultant> consultants,
                          String state, String city, String urbanRural, String address,
                          String emergencyContact) {
        this.id = id;
        this.user = user;
        this.severity = severity;
        this.consultants = consultants;
        this.state = state;
        this.city = city;
        this.urbanRural = urbanRural;
        this.address = address;
        this.emergencyContact = emergencyContact;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public List<Consultant> getConsultants() {
        return consultants;
    }

    public void setConsultants(List<Consultant> consultants) {
        this.consultants = consultants;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUrbanRural() {
        return urbanRural;
    }

    public void setUrbanRural(String urbanRural) {
        this.urbanRural = urbanRural;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * ✅ Get cached total listening duration (synchronous)
     * Call updateTotalListeningDuration() first to load async data
     */
    public int getTotalListeningDuration() {
        return totalListeningDuration;
    }

    /**
     * ✅ Set cached total listening duration
     */
    public void setTotalListeningDuration(int duration) {
        this.totalListeningDuration = duration;
    }

    /**
     * ✅ NEW: Update total listening duration asynchronously
     * This should be called when loading PatientProfile data
     */
    public void updateTotalListeningDuration(DurationUpdateCallback callback) {
        ListeningSessionService sessionService = ListeningSessionService.getInstance();

        sessionService.getTotalListeningDuration(this, new ListeningSessionService.DurationCallback() {
            @Override
            public void onDuration(int duration) {
                totalListeningDuration = duration;
                if (callback != null) {
                    callback.onDurationUpdated(duration);
                }
            }
        });
    }

    /**
     * ✅ NEW: Update total listening duration asynchronously (no callback version)
     */
    public void updateTotalListeningDuration() {
        updateTotalListeningDuration(null);
    }

    /**
     * ✅ Callback interface for duration updates
     */
    public interface DurationUpdateCallback {
        void onDurationUpdated(int duration);
    }
}
