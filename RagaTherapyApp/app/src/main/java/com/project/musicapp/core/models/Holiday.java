package com.project.musicapp.core.models;

import java.io.Serializable;

/**
 * Represents a consultant's holiday (unavailable day) in the week.
 * Each record defines one weekday (Sun–Sat) during which the consultant
 * is not available for appointments.
 *
 * Uses enum DayOfWeek to ensure type safety.
 */
public class Holiday implements Serializable {

    private int id;
    private int consultantId;
    private DayOfWeek day; // Enum instead of String

    public enum DayOfWeek {
        SUN, MON, TUE, WED, THU, FRI, SAT
    }

    public Holiday() {
    }

    /**
     * Creates a new Holiday for a consultant.
     *
     * @param id            Unique holiday ID
     * @param consultantId  Consultant who owns this holiday
     * @param day           Day of the week (enum)
     */
    public Holiday(int id, int consultantId, DayOfWeek day) {
        if (day == null) {
            throw new IllegalArgumentException("Day cannot be null");
        }
        this.id = id;
        this.consultantId = consultantId;
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(int consultantId) {
        this.consultantId = consultantId;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        if (day == null) {
            throw new IllegalArgumentException("Day cannot be null");
        }
        this.day = day;
    }

    @Override
    public String toString() {
        return "Holiday{" +
                "id=" + id +
                ", consultantId=" + consultantId +
                ", day=" + day +
                '}';
    }
}
