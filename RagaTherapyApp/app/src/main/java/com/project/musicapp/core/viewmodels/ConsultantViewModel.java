package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.models.Holiday;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.ConsultantService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated ConsultantViewModel with full feature coverage from ConsultantService
 * Includes LiveData for specialized queries and proper async handling
 */
public class ConsultantViewModel extends BaseViewModel<Consultant> {

    private final ConsultantService consultantService;

    // ✅ LiveData for specialized queries
    private final MutableLiveData<List<Consultant>> consultantsByDesignationLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Consultant>> consultantsByQualificationLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Consultant>> consultantsWithAccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Holiday>> consultantHolidaysLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> holidayCheckLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> consultantCountLiveData = new MutableLiveData<>(0);

    public ConsultantViewModel() {
        super(ConsultantService.getInstance()); // ✅ Use singleton
        this.consultantService = (ConsultantService) service;
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Returns LiveData of all consultants */
    public LiveData<List<Consultant>> getConsultants() {
        return getItems();
    }

    /** Adds a new consultant */
    public void addConsultant(Consultant consultant) {
        addItem(consultant);
    }

    /** Updates a consultant by ID */
    public void updateConsultant(int consultantId, Consultant updatedConsultant) {
        updateItem(consultantId, updatedConsultant, Consultant::getConsultantId);
    }

    /** Deletes a consultant by ID */
    public void deleteConsultant(int consultantId) {
        deleteItem(consultantId, Consultant::getConsultantId);
    }

    /** Refreshes the consultant list */
    public void refreshConsultants() {
        refresh();
    }

    /** Performs a predicate-based search */
    public void searchConsultants(Predicate<Consultant> predicate) {
        search(predicate);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ SPECIALIZED QUERY METHODS (New from ConsultantService)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Search consultants by designation
     * @return LiveData that updates when search completes
     */
    public LiveData<List<Consultant>> searchByDesignation(String designation) {
        consultantService.searchByDesignation(designation, new BaseService.DataCallback<Consultant>() {
            @Override
            public void onSuccess(List<Consultant> consultants) {
                consultantsByDesignationLiveData.postValue(consultants);
            }

            @Override
            public void onError(String error) {
                consultantsByDesignationLiveData.postValue(new ArrayList<>());
            }
        });
        return consultantsByDesignationLiveData;
    }

    /**
     * Search consultants by qualification
     * @return LiveData that updates when search completes
     */
    public LiveData<List<Consultant>> searchByQualification(String qualification) {
        consultantService.searchByQualification(qualification, new BaseService.DataCallback<Consultant>() {
            @Override
            public void onSuccess(List<Consultant> consultants) {
                consultantsByQualificationLiveData.postValue(consultants);
            }

            @Override
            public void onError(String error) {
                consultantsByQualificationLiveData.postValue(new ArrayList<>());
            }
        });
        return consultantsByQualificationLiveData;
    }

    /**
     * Get consultants with access
     * @return LiveData of consultants who have access
     */
    public LiveData<List<Consultant>> getConsultantsWithAccess() {
        consultantService.getConsultantsWithAccess(new BaseService.DataCallback<Consultant>() {
            @Override
            public void onSuccess(List<Consultant> consultants) {
                consultantsWithAccessLiveData.postValue(consultants);
            }

            @Override
            public void onError(String error) {
                consultantsWithAccessLiveData.postValue(new ArrayList<>());
            }
        });
        return consultantsWithAccessLiveData;
    }

    /**
     * Update consultant access status
     */
    public void updateAccessStatus(int consultantId, boolean hasAccess, BaseService.DataCallback<Consultant> callback) {
        consultantService.updateAccessStatus(consultantId, hasAccess, callback);
    }

    /**
     * Get consultant count
     * @return LiveData of total consultant count
     */
    public LiveData<Integer> getConsultantCount() {
        consultantService.getConsultantCount(count -> {
            consultantCountLiveData.postValue(count);
        });
        return consultantCountLiveData;
    }

    /**
     * Fetch a single consultant by ID (async with callback)
     */
    public void fetchConsultantById(int consultantId, BaseService.SingleItemCallback<Consultant> callback) {
        consultantService.fetchConsultantById(consultantId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ HOLIDAY-RELATED METHODS (Updated with proper async handling)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Set holidays for a consultant (async)
     */
    public void setConsultantHolidays(int consultantId, List<Holiday> holidays, BaseService.DataCallback<Consultant> callback) {
        consultantService.setConsultantHolidays(consultantId, holidays, callback);
    }

    /**
     * Set holidays for a consultant (no callback version)
     */
    public void setConsultantHolidays(int consultantId, List<Holiday> holidays) {
        consultantService.setConsultantHolidays(consultantId, holidays, new BaseService.DataCallback<Consultant>() {
            @Override
            public void onSuccess(List<Consultant> data) {
                // Silent success
            }

            @Override
            public void onError(String error) {
                // Silent error
            }
        });
    }

    /**
     * Add a single holiday to consultant (async)
     */
    public void addHolidayToConsultant(int consultantId, Holiday holiday, BaseService.DataCallback<Consultant> callback) {
        consultantService.addHolidayToConsultant(consultantId, holiday, callback);
    }

    /**
     * Add a single holiday to consultant (no callback version)
     */
    public void addHolidayToConsultant(int consultantId, Holiday holiday) {
        consultantService.addHolidayToConsultant(consultantId, holiday, new BaseService.DataCallback<Consultant>() {
            @Override
            public void onSuccess(List<Consultant> data) {
                // Silent success
            }

            @Override
            public void onError(String error) {
                // Silent error
            }
        });
    }

    /**
     * Remove a holiday by day (async)
     */
    public void removeHolidayByDay(int consultantId, Holiday.DayOfWeek day, BaseService.DataCallback<Consultant> callback) {
        consultantService.removeHolidayByDay(consultantId, day, callback);
    }

    /**
     * Remove a holiday by day (no callback version)
     */
    public void removeHolidayByDay(int consultantId, Holiday.DayOfWeek day) {
        consultantService.removeHolidayByDay(consultantId, day, new BaseService.DataCallback<Consultant>() {
            @Override
            public void onSuccess(List<Consultant> data) {
                // Silent success
            }

            @Override
            public void onError(String error) {
                // Silent error
            }
        });
    }

    /**
     * Get all holidays for a consultant
     * @return LiveData of holidays list
     */
    public LiveData<List<Holiday>> getConsultantHolidays(int consultantId) {
        consultantService.getConsultantHolidays(consultantId, holidays -> {
            consultantHolidaysLiveData.postValue(holidays);
        });
        return consultantHolidaysLiveData;
    }

    /**
     * Get all holidays for a consultant (callback version)
     */
    public void getConsultantHolidays(int consultantId, ConsultantService.HolidaysCallback callback) {
        consultantService.getConsultantHolidays(consultantId, callback);
    }

    /**
     * Check if consultant is on holiday on a specific day
     * @return LiveData of holiday status
     */
    public LiveData<Boolean> isConsultantOnHoliday(int consultantId, Holiday.DayOfWeek day) {
        consultantService.isConsultantOnHoliday(consultantId, day, isOnHoliday -> {
            holidayCheckLiveData.postValue(isOnHoliday);
        });
        return holidayCheckLiveData;
    }

    /**
     * Check if consultant is on holiday on a specific day (callback version)
     */
    public void isConsultantOnHoliday(int consultantId, Holiday.DayOfWeek day, ConsultantService.OnHolidayCallback callback) {
        consultantService.isConsultantOnHoliday(consultantId, day, callback);
    }
}
