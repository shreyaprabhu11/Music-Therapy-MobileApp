package com.project.musicapp.core.services;

import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.models.Holiday;
import java.util.ArrayList;
import java.util.List;

/**
 * Firebase-integrated ConsultantService extending BaseService.
 * All CRUD and holiday-related operations now persist in Firebase with async callbacks.
 */
public class ConsultantService extends BaseService<Consultant> {
    private static ConsultantService instance;

    private ConsultantService() {
        super(Consultant.class, "consultants");
    }

    // ✅ SINGLETON PATTERN
    public static synchronized ConsultantService getInstance() {
        if (instance == null) {
            instance = new ConsultantService();
        }
        return instance;
    }

    /**
     * Fetch all consultants from Firebase
     */
    public void fetchAllConsultants(DataCallback<Consultant> callback) {
        fetchAllAsync(callback);
    }

    /**
     * Fetch a single consultant by ID
     */
    public void fetchConsultantById(int consultantId, SingleItemCallback<Consultant> callback) {
        fetchByIdAsync(consultantId, Consultant::getConsultantId, callback);
    }

    /**
     * Add a new consultant
     */
    public void addConsultant(Consultant consultant, DataCallback<Consultant> callback) {
        addItem(consultant, callback);
    }

    /**
     * Update existing consultant
     */
    public void updateConsultant(int consultantId, Consultant updatedConsultant, DataCallback<Consultant> callback) {
        updateItem(consultantId, updatedConsultant, Consultant::getConsultantId, callback);
    }

    /**
     * Delete consultant by ID
     */
    public void deleteConsultantById(int consultantId, DataCallback<Consultant> callback) {
        deleteById(consultantId, Consultant::getConsultantId, callback);
    }

    /**
     * Search consultants based on condition
     */
    public void searchConsultants(java.util.function.Predicate<Consultant> predicate, DataCallback<Consultant> callback) {
        search(predicate, callback);
    }

    // ------------------------------
    // ✅ Holiday-related operations (fully functional with callbacks)
    // ------------------------------

    /**
     * Assign holidays to a consultant
     */
    public void setConsultantHolidays(int consultantId, List<Holiday> holidays, DataCallback<Consultant> callback) {
        fetchConsultantById(consultantId, new SingleItemCallback<Consultant>() {
            @Override
            public void onSuccess(Consultant consultant) {
                consultant.setHolidays(holidays);
                updateConsultant(consultantId, consultant, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError("Consultant not found: " + error);
            }
        });
    }

    /**
     * Add single holiday to consultant
     */
    public void addHolidayToConsultant(int consultantId, Holiday holiday, DataCallback<Consultant> callback) {
        fetchConsultantById(consultantId, new SingleItemCallback<Consultant>() {
            @Override
            public void onSuccess(Consultant consultant) {
                List<Holiday> list = consultant.getHolidays();
                if (list == null) list = new ArrayList<>();
                list.add(holiday);
                consultant.setHolidays(list);
                updateConsultant(consultantId, consultant, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError("Consultant not found: " + error);
            }
        });
    }

    /**
     * Remove specific holiday by DayOfWeek enum
     */
    public void removeHolidayByDay(int consultantId, Holiday.DayOfWeek day, DataCallback<Consultant> callback) {
        fetchConsultantById(consultantId, new SingleItemCallback<Consultant>() {
            @Override
            public void onSuccess(Consultant consultant) {
                if (consultant.getHolidays() != null) {
                    consultant.getHolidays().removeIf(h -> h.getDay() == day);
                    updateConsultant(consultantId, consultant, callback);
                } else {
                    callback.onError("No holidays found for consultant");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError("Consultant not found: " + error);
            }
        });
    }

    /**
     * Get all holidays for consultant
     */
    public void getConsultantHolidays(int consultantId, HolidaysCallback callback) {
        fetchConsultantById(consultantId, new SingleItemCallback<Consultant>() {
            @Override
            public void onSuccess(Consultant consultant) {
                List<Holiday> holidays = (consultant.getHolidays() != null)
                        ? consultant.getHolidays()
                        : new ArrayList<>();
                callback.onHolidays(holidays);
            }

            @Override
            public void onError(String error) {
                callback.onHolidays(new ArrayList<>());
            }
        });
    }

    /**
     * Check if a consultant has a holiday on a given day
     */
    public void isConsultantOnHoliday(int consultantId, Holiday.DayOfWeek day, OnHolidayCallback callback) {
        fetchConsultantById(consultantId, new SingleItemCallback<Consultant>() {
            @Override
            public void onSuccess(Consultant consultant) {
                if (consultant.getHolidays() == null) {
                    callback.onResult(false);
                    return;
                }
                boolean onHoliday = consultant.getHolidays().stream()
                        .anyMatch(h -> h.getDay() == day);
                callback.onResult(onHoliday);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Search consultants by designation
     */
    public void searchByDesignation(String designation, DataCallback<Consultant> callback) {
        search(consultant -> consultant.getDesignation() != null &&
                consultant.getDesignation().equalsIgnoreCase(designation), callback);
    }

    /**
     * Search consultants by qualification
     */
    public void searchByQualification(String qualification, DataCallback<Consultant> callback) {
        search(consultant -> consultant.getQualifications() != null &&
                consultant.getQualifications().toLowerCase().contains(qualification.toLowerCase()), callback);
    }

    /**
     * Get consultants with access
     */
    public void getConsultantsWithAccess(DataCallback<Consultant> callback) {
        search(Consultant::getAccessInfo, callback);
    }

    /**
     * Update consultant access status
     */
    public void updateAccessStatus(int consultantId, boolean hasAccess, DataCallback<Consultant> callback) {
        fetchConsultantById(consultantId, new SingleItemCallback<Consultant>() {
            @Override
            public void onSuccess(Consultant consultant) {
                consultant.setAccessInfo(hasAccess);
                updateConsultant(consultantId, consultant, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError("Consultant not found: " + error);
            }
        });
    }

    /**
     * Get consultant count
     */
    public void getConsultantCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<Consultant>() {
            @Override
            public void onSuccess(List<Consultant> consultants) {
                callback.onCount(consultants.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    // ✅ CUSTOM CALLBACK INTERFACES
    public interface HolidaysCallback {
        void onHolidays(List<Holiday> holidays);
    }

    public interface OnHolidayCallback {
        void onResult(boolean isOnHoliday);
    }

    public interface CountCallback {
        void onCount(int count);
    }
}
