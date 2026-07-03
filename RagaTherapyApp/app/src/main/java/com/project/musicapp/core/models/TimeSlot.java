package com.project.musicapp.core.models;

import java.io.Serializable;

public class TimeSlot implements Serializable {
    private String slotId;
    private String fromTime;
    private String toTime;
    private String location;
    private boolean enabled; // <- rename
    private String defaultMeetLink;

    public TimeSlot() { }

    public TimeSlot(String slotId, String fromTime, String toTime, String location, boolean enabled, String defaultMeetLink) {
        this.slotId = slotId;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.location = location;
        this.enabled = enabled;
        this.defaultMeetLink = defaultMeetLink;
    }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getFromTime() { return fromTime; }
    public void setFromTime(String fromTime) { this.fromTime = fromTime; }

    public String getToTime() { return toTime; }
    public void setToTime(String toTime) { this.toTime = toTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isEnabled() { return enabled; } // <- getter
    public void setEnabled(boolean enabled) { this.enabled = enabled; } // <- setter

    public String getDefaultMeetLink() { return defaultMeetLink; }
    public void setDefaultMeetLink(String defaultMeetLink) { this.defaultMeetLink = defaultMeetLink; }
}
