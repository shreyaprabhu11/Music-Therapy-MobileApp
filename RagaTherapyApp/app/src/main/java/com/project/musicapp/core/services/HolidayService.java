package com.project.musicapp.core.services;

import com.project.musicapp.core.models.Holiday;
import java.util.ArrayList;
import java.util.List;

/**
 * Firebase-integrated HolidayService.
 * Extends BaseService for generic Firebase CRUD and search operations with async callbacks.
 */
public class HolidayService extends BaseService<Holiday> {
    private static HolidayService instance;

    private HolidayService() {
        super(Holiday.class, "holidays");
    }

    // ✅ SINGLETON PATTERN
    public static synchronized HolidayService getInstance() {
        if (instance == null) {
            instance = new HolidayService();
        }
        return instance;
    }

    /**
     * Fetch all holidays
     */
    public void fetchAllHolidays(DataCallback<Holiday> callback) {
        fetchAllAsync(callback);
    }

    /**
     * Fetch holiday by ID
     */
    public void fetchHolidayById(int id, SingleItemCallback<Holiday> callback) {
        fetchByIdAsync(id, Holiday::getId, callback);
    }

    /**
     * Fetch all holidays for a specific consultant
     */
    public void fetchHolidaysByConsultantId(int consultantId, DataCallback<Holiday> callback) {
        search(holiday -> holiday.getConsultantId() == consultantId, callback);
    }

    /**
     * Add a new holiday
     */
    public void addHoliday(Holiday holiday, DataCallback<Holiday> callback) {
        if (holiday == null) {
            callback.onError("Holiday cannot be null");
            return;
        }

        // Check if consultant already has this day as holiday
        checkDuplicateHoliday(holiday.getConsultantId(), holiday.getDay(), new ExistsCallback() {
            @Override
            public void onResult(boolean exists) {
                if (exists) {
                    callback.onError("Consultant already has " + holiday.getDay() + " marked as holiday");
                } else {
                    addItem(holiday, callback);
                }
            }
        });
    }

    /**
     * Update a holiday entry
     */
    public void updateHoliday(int id, Holiday updatedHoliday, DataCallback<Holiday> callback) {
        updateItem(id, updatedHoliday, Holiday::getId, callback);
    }

    /**
     * Delete holiday by ID
     */
    public void deleteHolidayById(int id, DataCallback<Holiday> callback) {
        deleteById(id, Holiday::getId, callback);
    }

    /**
     * Search holidays by any custom condition
     */
    public void searchHolidays(java.util.function.Predicate<Holiday> predicate, DataCallback<Holiday> callback) {
        search(predicate, callback);
    }

    /**
     * Fetch holidays by specific day of week
     */
    public void fetchHolidaysByDay(Holiday.DayOfWeek day, DataCallback<Holiday> callback) {
        if (day == null) {
            callback.onError("Day cannot be null");
            return;
        }
        search(holiday -> holiday.getDay() == day, callback);
    }

    /**
     * Fetch holidays for a consultant on a specific day
     */
    public void fetchHolidayByConsultantAndDay(int consultantId, Holiday.DayOfWeek day, SingleItemCallback<Holiday> callback) {
        if (day == null) {
            callback.onError("Day cannot be null");
            return;
        }

        search(holiday -> holiday.getConsultantId() == consultantId &&
                holiday.getDay() == day, new DataCallback<Holiday>() {
            @Override
            public void onSuccess(List<Holiday> holidays) {
                if (!holidays.isEmpty()) {
                    callback.onSuccess(holidays.get(0));
                } else {
                    callback.onError("No holiday found for consultant " + consultantId + " on " + day);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Check if a consultant has a holiday on a specific day
     */
    public void checkDuplicateHoliday(int consultantId, Holiday.DayOfWeek day, ExistsCallback callback) {
        if (day == null) {
            callback.onResult(false);
            return;
        }

        search(holiday -> holiday.getConsultantId() == consultantId &&
                holiday.getDay() == day, new DataCallback<Holiday>() {
            @Override
            public void onSuccess(List<Holiday> holidays) {
                callback.onResult(!holidays.isEmpty());
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Delete all holidays for a specific consultant
     */
    public void deleteHolidaysByConsultantId(int consultantId, DataCallback<Holiday> callback) {
        fetchHolidaysByConsultantId(consultantId, new DataCallback<Holiday>() {
            @Override
            public void onSuccess(List<Holiday> holidays) {
                if (holidays.isEmpty()) {
                    callback.onSuccess(holidays);
                    return;
                }

                int[] deletedCount = {0};
                for (Holiday holiday : holidays) {
                    deleteHolidayById(holiday.getId(), new DataCallback<Holiday>() {
                        @Override
                        public void onSuccess(List<Holiday> deletedHolidays) {
                            deletedCount[0]++;
                            if (deletedCount[0] == holidays.size()) {
                                callback.onSuccess(holidays);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to delete holiday: " + error);
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

    /**
     * Get count of holidays for a consultant
     */
    public void getHolidayCountByConsultant(int consultantId, CountCallback callback) {
        fetchHolidaysByConsultantId(consultantId, new DataCallback<Holiday>() {
            @Override
            public void onSuccess(List<Holiday> holidays) {
                callback.onCount(holidays.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get total holiday count
     */
    public void getTotalHolidayCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<Holiday>() {
            @Override
            public void onSuccess(List<Holiday> holidays) {
                callback.onCount(holidays.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get all unique days that have holidays
     */
    public void getAllHolidayDays(DaysCallback callback) {
        fetchAllAsync(new DataCallback<Holiday>() {
            @Override
            public void onSuccess(List<Holiday> holidays) {
                List<Holiday.DayOfWeek> days = new ArrayList<>();
                for (Holiday holiday : holidays) {
                    if (holiday.getDay() != null && !days.contains(holiday.getDay())) {
                        days.add(holiday.getDay());
                    }
                }
                callback.onDays(days);
            }

            @Override
            public void onError(String error) {
                callback.onDays(new ArrayList<>());
            }
        });
    }

    /**
     * Get working days for a consultant (inverse of holidays)
     */
    public void getWorkingDays(int consultantId, DaysCallback callback) {
        fetchHolidaysByConsultantId(consultantId, new DataCallback<Holiday>() {
            @Override
            public void onSuccess(List<Holiday> holidays) {
                List<Holiday.DayOfWeek> workingDays = new ArrayList<>();

                // Add all days
                for (Holiday.DayOfWeek day : Holiday.DayOfWeek.values()) {
                    workingDays.add(day);
                }

                // Remove holidays
                for (Holiday holiday : holidays) {
                    workingDays.remove(holiday.getDay());
                }

                callback.onDays(workingDays);
            }

            @Override
            public void onError(String error) {
                // Return all days if error
                List<Holiday.DayOfWeek> allDays = new ArrayList<>();
                for (Holiday.DayOfWeek day : Holiday.DayOfWeek.values()) {
                    allDays.add(day);
                }
                callback.onDays(allDays);
            }
        });
    }

    /**
     * Check if consultant is available on a specific day
     */
    public void isConsultantAvailable(int consultantId, Holiday.DayOfWeek day, AvailabilityCallback callback) {
        if (day == null) {
            callback.onAvailability(true); // Default to available if day is null
            return;
        }

        checkDuplicateHoliday(consultantId, day, new ExistsCallback() {
            @Override
            public void onResult(boolean hasHoliday) {
                callback.onAvailability(!hasHoliday); // Available if no holiday
            }
        });
    }

    /**
     * Add multiple holidays for a consultant at once
     */
    public void addMultipleHolidays(int consultantId, List<Holiday.DayOfWeek> days, DataCallback<Holiday> callback) {
        if (days == null || days.isEmpty()) {
            callback.onError("No days provided");
            return;
        }

        List<Holiday> holidays = new ArrayList<>();
        int[] addedCount = {0};

        for (Holiday.DayOfWeek day : days) {
            Holiday holiday = new Holiday();
            holiday.setConsultantId(consultantId);
            holiday.setDay(day);

            addHoliday(holiday, new DataCallback<Holiday>() {
                @Override
                public void onSuccess(List<Holiday> addedHolidays) {
                    holidays.addAll(addedHolidays);
                    addedCount[0]++;
                    if (addedCount[0] == days.size()) {
                        callback.onSuccess(holidays);
                    }
                }

                @Override
                public void onError(String error) {
                    // Continue even if one fails (might be duplicate)
                    addedCount[0]++;
                    if (addedCount[0] == days.size()) {
                        if (holidays.isEmpty()) {
                            callback.onError("Failed to add any holidays");
                        } else {
                            callback.onSuccess(holidays);
                        }
                    }
                }
            });
        }
    }

    // ✅ CUSTOM CALLBACK INTERFACES
    public interface CountCallback {
        void onCount(int count);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
    }

    public interface DaysCallback {
        void onDays(List<Holiday.DayOfWeek> days);
    }

    public interface AvailabilityCallback {
        void onAvailability(boolean isAvailable);
    }
}
