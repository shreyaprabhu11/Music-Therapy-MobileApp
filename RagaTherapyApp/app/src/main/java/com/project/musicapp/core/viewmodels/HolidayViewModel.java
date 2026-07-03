package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;

import com.project.musicapp.core.models.Holiday;
import com.project.musicapp.core.services.HolidayService;

import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated HolidayViewModel following MusicService pattern
 * Uses singleton and proper async callbacks for Firebase integration
 */
public class HolidayViewModel extends BaseViewModel<Holiday> {

    private final HolidayService holidayService;

    public HolidayViewModel() {
        super(HolidayService.getInstance()); // ✅ Use singleton instead of new
        this.holidayService = (HolidayService) service;
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Returns LiveData of all holidays */
    public LiveData<List<Holiday>> getHolidays() {
        return getItems();
    }

    /** Adds a new holiday */
    public void addHoliday(Holiday holiday) {
        addItem(holiday);
    }

    /** Updates a holiday by ID */
    public void updateHoliday(int id, Holiday updatedHoliday) {
        updateItem(id, updatedHoliday, Holiday::getId);
    }

    /** Deletes a holiday by ID */
    public void deleteHoliday(int id) {
        deleteItem(id, Holiday::getId);
    }

    /** Reloads data from Firebase */
    public void refreshHolidays() {
        refresh();
    }

    /** Search by predicate (filter holidays) */
    public void searchHolidays(Predicate<Holiday> predicate) {
        search(predicate);
    }
}
