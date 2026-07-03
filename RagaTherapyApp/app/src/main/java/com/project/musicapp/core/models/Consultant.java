package com.project.musicapp.core.models;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a Consultant entity containing personal info,
 * professional details, time slots, and optional holiday schedule.
 */
public class Consultant implements Serializable {

    private int consultantId;
    private User user;
    private String bio;
    private String designation;
    private String qualifications;

    private boolean hasAccess;

    private List<TimeSlot> timeSlots;

    // New field for consultant holidays
    private List<Holiday> holidays;

    public Consultant() {
    }

    public Consultant(int consultantId, User user, String bio, String designation, String qualifications, List<TimeSlot> timeSlots) {
        this.consultantId = consultantId;
        this.user = user;
        this.bio = bio;
        this.designation = designation;
        this.qualifications = qualifications;
        this.timeSlots = timeSlots;
        this.hasAccess=false;
    }

    // Optional overloaded constructor including holidays
    public Consultant(int consultantId, User user, String bio, String designation, String qualifications,
                      List<TimeSlot> timeSlots, List<Holiday> holidays) {
        this(consultantId, user, bio, designation, qualifications, timeSlots);
        this.holidays = holidays;
    }

    public int getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(int consultantId) {
        this.consultantId = consultantId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public boolean getAccessInfo() {
        return hasAccess;
    }

    public void setAccessInfo(boolean hasAccess) {
        this.hasAccess = hasAccess;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // New: holidays getter/setter
    public List<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<Holiday> holidays) {
        this.holidays = holidays;
    }
}
