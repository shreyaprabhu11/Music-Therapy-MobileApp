package com.project.musicapp.core.models;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    private int id;
    private String title;
    private String message;
    private boolean isRead;
    private long sentTime;
    private User receiver;
    private Appointment appointment;

    public Notification() {
    }

    public Notification(int id, String title, String message, long sentTime, boolean isRead, User receiver) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.sentTime = sentTime;
        this.isRead = isRead;
        this.receiver = receiver;
    }

    public Notification(int id, String title, String message, long sentTime, boolean isRead, User receiver, Appointment appointment) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.sentTime = sentTime;
        this.isRead = isRead;
        this.receiver = receiver;
        this.appointment = appointment;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getSentTime() { return sentTime; }
    public void setSentTime(long sentTime) { this.sentTime = sentTime; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public boolean hasAppointment() {
        return appointment != null;
    }
}
