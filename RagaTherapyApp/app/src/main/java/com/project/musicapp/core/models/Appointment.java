package com.project.musicapp.core.models;

import java.io.Serializable;
import java.util.Date;

public class Appointment implements Serializable {
    int id;
    TimeSlot timeSlot;
    PatientProfile patientProfile;
    Consultant consultant;
    String meetLink;
    long createdAt;

    public Appointment(int id, TimeSlot timeSlot, PatientProfile patientProfile, Consultant consultant, String meetLink, long createdAt) {
        this.id = id;
        this.timeSlot = timeSlot;
        this.patientProfile = patientProfile;
        this.consultant = consultant;
        this.meetLink = meetLink;
        this.createdAt = createdAt;
    }

    public Appointment() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public PatientProfile getPatientProfile() {
        return patientProfile;
    }

    public void setPatientProfile(PatientProfile patientProfile) {
        this.patientProfile = patientProfile;
    }

    public Consultant getConsultant() {
        return consultant;
    }

    public void setConsultant(Consultant consultant) {
        this.consultant = consultant;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
