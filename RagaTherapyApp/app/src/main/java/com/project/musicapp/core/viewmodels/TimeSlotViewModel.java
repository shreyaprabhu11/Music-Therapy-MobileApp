package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;

import com.project.musicapp.core.models.TimeSlot;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.TimeSlotService;

import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated TimeSlotViewModel with proper async handling
 * Follows singleton pattern and uses callbacks for all Firebase operations
 */
public class TimeSlotViewModel extends BaseViewModel<TimeSlot> {

    private final TimeSlotService timeSlotService;

    public TimeSlotViewModel() {
        super(TimeSlotService.getInstance()); // ✅ Use singleton instead of new
        this.timeSlotService = TimeSlotService.getInstance();
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Get LiveData of all time slots (auto updates when Firebase changes) */
    public LiveData<List<TimeSlot>> getAllTimeSlots() {
        return getItems();
    }

    /** Add new time slot */
    public void addTimeSlot(TimeSlot timeSlot) {
        addItem(timeSlot);
    }

    /** Update existing time slot */
    public void updateTimeSlot(String slotId, TimeSlot updatedTimeSlot) {
        updateItem(slotId.hashCode(), updatedTimeSlot, ts -> ts.getSlotId().hashCode());
    }

    /** Delete time slot by slotId */
    public void deleteTimeSlot(String slotId) {
        deleteItem(slotId.hashCode(), ts -> ts.getSlotId().hashCode());
    }

    /** Search time slots locally (filtered from LiveData list) */
    public void searchTimeSlots(Predicate<TimeSlot> predicate) {
        search(predicate);
    }

    /** Force manual Firebase refresh */
    public void refreshTimeSlots() {
        refresh();
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ SPECIALIZED QUERY METHODS (Async with Callbacks)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetch single time slot by ID (async with callback)
     */
    public void fetchTimeSlotById(String slotId, BaseService.SingleItemCallback<TimeSlot> callback) {
        timeSlotService.fetchTimeSlotById(slotId, callback);
    }

    /**
     * Get enabled time slots only
     */
    public void getEnabledTimeSlots(BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.getEnabledTimeSlots(callback);
    }

    /**
     * Get disabled time slots only
     */
    public void getDisabledTimeSlots(BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.getDisabledTimeSlots(callback);
    }

    /**
     * Get time slots by location
     */
    public void getTimeSlotsByLocation(String location, BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.getTimeSlotsByLocation(location, callback);
    }

    /**
     * Get time slots by start time (partial match)
     */
    public void getTimeSlotsByStartTime(String startTime, BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.getTimeSlotsByStartTime(startTime, callback);
    }

    /**
     * Get time slots with meet links
     */
    public void getTimeSlotsWithMeetLink(BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.getTimeSlotsWithMeetLink(callback);
    }

    /**
     * Get all unique locations
     */
    public void getAllLocations(TimeSlotService.LocationsCallback callback) {
        timeSlotService.getAllLocations(callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ ENABLE/DISABLE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Enable a time slot
     */
    public void enableTimeSlot(String slotId, BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.enableTimeSlot(slotId, callback);
    }

    /**
     * Disable a time slot
     */
    public void disableTimeSlot(String slotId, BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.disableTimeSlot(slotId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ COUNT OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get total time slot count
     */
    public void getTimeSlotCount(TimeSlotService.CountCallback callback) {
        timeSlotService.getTimeSlotCount(callback);
    }

    /**
     * Get enabled time slot count
     */
    public void getEnabledTimeSlotCount(TimeSlotService.CountCallback callback) {
        timeSlotService.getEnabledTimeSlotCount(callback);
    }

    /**
     * Check if time slot exists by slotId
     */
    public void timeSlotExists(String slotId, TimeSlotService.ExistsCallback callback) {
        timeSlotService.timeSlotExists(slotId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ BULK OPERATIONS WITH CALLBACKS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Add time slot with callback (recommended for error handling)
     */
    public void addTimeSlotWithCallback(TimeSlot timeSlot, BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.addTimeSlot(timeSlot, callback);
    }

    /**
     * Update time slot with callback
     */
    public void updateTimeSlotWithCallback(String slotId, TimeSlot updatedTimeSlot,
                                           BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.updateTimeSlot(slotId, updatedTimeSlot, callback);
    }

    /**
     * Delete time slot with callback
     */
    public void deleteTimeSlotWithCallback(String slotId, BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.deleteTimeSlot(slotId, callback);
    }

    /**
     * Delete all time slots by location
     */
    public void deleteTimeSlotsByLocation(String location, BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.deleteTimeSlotsByLocation(location, callback);
    }

    /**
     * Search time slots with callback
     */
    public void searchTimeSlotsWithCallback(Predicate<TimeSlot> predicate,
                                            BaseService.DataCallback<TimeSlot> callback) {
        timeSlotService.searchTimeSlots(predicate, callback);
    }
}
