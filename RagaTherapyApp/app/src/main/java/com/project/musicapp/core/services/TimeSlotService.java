package com.project.musicapp.core.services;

import com.project.musicapp.core.models.TimeSlot;
import java.util.ArrayList;
import java.util.List;

/**
 * Firebase-integrated TimeSlotService extending BaseService.
 * All operations use async callbacks for Firebase integration.
 */
public class TimeSlotService extends BaseService<TimeSlot> {
    private static TimeSlotService instance;

    private TimeSlotService() {
        super(TimeSlot.class, "timeSlots"); // Firebase node = "timeSlots"
    }

    // ✅ SINGLETON PATTERN
    public static synchronized TimeSlotService getInstance() {
        if (instance == null) {
            instance = new TimeSlotService();
        }
        return instance;
    }

    /**
     * Fetch all time slots
     */
    public void fetchAllTimeSlots(DataCallback<TimeSlot> callback) {
        fetchAllAsync(callback);
    }

    /**
     * Fetch a time slot by its slotId (String-based lookup)
     */
    public void fetchTimeSlotById(String slotId, SingleItemCallback<TimeSlot> callback) {
        if (slotId == null || slotId.trim().isEmpty()) {
            callback.onError("Slot ID cannot be null or empty");
            return;
        }

        search(ts -> ts.getSlotId() != null && ts.getSlotId().equals(slotId), new DataCallback<TimeSlot>() {
            @Override
            public void onSuccess(List<TimeSlot> timeSlots) {
                if (!timeSlots.isEmpty()) {
                    callback.onSuccess(timeSlots.get(0));
                } else {
                    callback.onError("Time slot not found with ID: " + slotId);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Add a new time slot
     */
    public void addTimeSlot(TimeSlot timeSlot, DataCallback<TimeSlot> callback) {
        if (timeSlot == null) {
            callback.onError("Time slot cannot be null");
            return;
        }

        // Check if slot ID already exists
        if (timeSlot.getSlotId() != null) {
            fetchTimeSlotById(timeSlot.getSlotId(), new SingleItemCallback<TimeSlot>() {
                @Override
                public void onSuccess(TimeSlot existingSlot) {
                    callback.onError("Time slot with ID '" + timeSlot.getSlotId() + "' already exists");
                }

                @Override
                public void onError(String error) {
                    // Slot doesn't exist - safe to add
                    addItem(timeSlot, callback);
                }
            });
        } else {
            addItem(timeSlot, callback);
        }
    }

    /**
     * Update an existing time slot by slotId
     */
    public void updateTimeSlot(String slotId, TimeSlot updatedTimeSlot, DataCallback<TimeSlot> callback) {
        if (slotId == null || slotId.trim().isEmpty()) {
            callback.onError("Slot ID cannot be null or empty");
            return;
        }

        fetchTimeSlotById(slotId, new SingleItemCallback<TimeSlot>() {
            @Override
            public void onSuccess(TimeSlot existingSlot) {
                // Use the hash code as the Firebase key
                updateItem(slotId.hashCode(), updatedTimeSlot, ts -> ts.getSlotId().hashCode(), callback);
            }

            @Override
            public void onError(String error) {
                callback.onError("Time slot not found: " + error);
            }
        });
    }

    /**
     * Delete a time slot by slotId
     */
    public void deleteTimeSlot(String slotId, DataCallback<TimeSlot> callback) {
        if (slotId == null || slotId.trim().isEmpty()) {
            callback.onError("Slot ID cannot be null or empty");
            return;
        }

        deleteById(slotId.hashCode(), ts -> ts.getSlotId().hashCode(), callback);
    }

    /**
     * Search time slots based on predicate
     */
    public void searchTimeSlots(java.util.function.Predicate<TimeSlot> predicate, DataCallback<TimeSlot> callback) {
        search(predicate, callback);
    }

    /**
     * Get enabled time slots only
     */
    public void getEnabledTimeSlots(DataCallback<TimeSlot> callback) {
        search(TimeSlot::isEnabled, callback);
    }

    /**
     * Get disabled time slots only
     */
    public void getDisabledTimeSlots(DataCallback<TimeSlot> callback) {
        search(ts -> !ts.isEnabled(), callback);
    }

    /**
     * Find time slots by location
     */
    public void getTimeSlotsByLocation(String location, DataCallback<TimeSlot> callback) {
        if (location == null || location.trim().isEmpty()) {
            callback.onError("Location cannot be null or empty");
            return;
        }

        search(ts -> ts.getLocation() != null &&
                ts.getLocation().equalsIgnoreCase(location), callback);
    }

    /**
     * Find time slots by time range (partial match on fromTime)
     */
    public void getTimeSlotsByStartTime(String startTime, DataCallback<TimeSlot> callback) {
        if (startTime == null || startTime.trim().isEmpty()) {
            callback.onError("Start time cannot be null or empty");
            return;
        }

        search(ts -> ts.getFromTime() != null &&
                ts.getFromTime().contains(startTime), callback);
    }

    /**
     * Enable a time slot
     */
    public void enableTimeSlot(String slotId, DataCallback<TimeSlot> callback) {
        fetchTimeSlotById(slotId, new SingleItemCallback<TimeSlot>() {
            @Override
            public void onSuccess(TimeSlot timeSlot) {
                if (!timeSlot.isEnabled()) {
                    timeSlot.setEnabled(true);
                    updateTimeSlot(slotId, timeSlot, callback);
                } else {
                    callback.onSuccess(List.of(timeSlot)); // Already enabled
                }
            }

            @Override
            public void onError(String error) {
                callback.onError("Time slot not found: " + error);
            }
        });
    }

    /**
     * Disable a time slot
     */
    public void disableTimeSlot(String slotId, DataCallback<TimeSlot> callback) {
        fetchTimeSlotById(slotId, new SingleItemCallback<TimeSlot>() {
            @Override
            public void onSuccess(TimeSlot timeSlot) {
                if (timeSlot.isEnabled()) {
                    timeSlot.setEnabled(false);
                    updateTimeSlot(slotId, timeSlot, callback);
                } else {
                    callback.onSuccess(List.of(timeSlot)); // Already disabled
                }
            }

            @Override
            public void onError(String error) {
                callback.onError("Time slot not found: " + error);
            }
        });
    }

    /**
     * Get time slot count
     */
    public void getTimeSlotCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<TimeSlot>() {
            @Override
            public void onSuccess(List<TimeSlot> timeSlots) {
                callback.onCount(timeSlots.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get enabled time slot count
     */
    public void getEnabledTimeSlotCount(CountCallback callback) {
        getEnabledTimeSlots(new DataCallback<TimeSlot>() {
            @Override
            public void onSuccess(List<TimeSlot> timeSlots) {
                callback.onCount(timeSlots.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Check if time slot exists by slotId
     */
    public void timeSlotExists(String slotId, ExistsCallback callback) {
        if (slotId == null || slotId.trim().isEmpty()) {
            callback.onResult(false);
            return;
        }

        fetchTimeSlotById(slotId, new SingleItemCallback<TimeSlot>() {
            @Override
            public void onSuccess(TimeSlot timeSlot) {
                callback.onResult(true);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Get all unique locations
     */
    public void getAllLocations(LocationsCallback callback) {
        fetchAllAsync(new DataCallback<TimeSlot>() {
            @Override
            public void onSuccess(List<TimeSlot> timeSlots) {
                List<String> locations = new ArrayList<>();
                for (TimeSlot slot : timeSlots) {
                    if (slot.getLocation() != null && !slot.getLocation().isEmpty()
                            && !locations.contains(slot.getLocation())) {
                        locations.add(slot.getLocation());
                    }
                }
                callback.onLocations(locations);
            }

            @Override
            public void onError(String error) {
                callback.onLocations(new ArrayList<>());
            }
        });
    }

    /**
     * Get time slots with meet links
     */
    public void getTimeSlotsWithMeetLink(DataCallback<TimeSlot> callback) {
        search(ts -> ts.getDefaultMeetLink() != null && !ts.getDefaultMeetLink().isEmpty(), callback);
    }

    /**
     * Delete all time slots by location
     */
    public void deleteTimeSlotsByLocation(String location, DataCallback<TimeSlot> callback) {
        if (location == null || location.trim().isEmpty()) {
            callback.onError("Location cannot be null or empty");
            return;
        }

        getTimeSlotsByLocation(location, new DataCallback<TimeSlot>() {
            @Override
            public void onSuccess(List<TimeSlot> timeSlots) {
                if (timeSlots.isEmpty()) {
                    callback.onSuccess(timeSlots);
                    return;
                }

                int[] deletedCount = {0};
                for (TimeSlot slot : timeSlots) {
                    deleteTimeSlot(slot.getSlotId(), new DataCallback<TimeSlot>() {
                        @Override
                        public void onSuccess(List<TimeSlot> deletedSlots) {
                            deletedCount[0]++;
                            if (deletedCount[0] == timeSlots.size()) {
                                callback.onSuccess(timeSlots);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to delete time slot: " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ✅ CUSTOM CALLBACK INTERFACES
    public interface CountCallback {
        void onCount(int count);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
    }

    public interface LocationsCallback {
        void onLocations(List<String> locations);
    }
}
