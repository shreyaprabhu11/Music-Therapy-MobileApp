package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.musicapp.core.models.Appointment;
import com.project.musicapp.core.services.AppointmentService;
import com.project.musicapp.core.services.BaseService;

import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated AppointmentViewModel with all specialized methods from AppointmentService
 * Exposes LiveData for UI observation and provides async callback methods
 */
public class AppointmentViewModel extends BaseViewModel<Appointment> {

    private final AppointmentService appointmentService;

    // ✅ LiveData for specialized queries
    private final MutableLiveData<List<Appointment>> appointmentsByConsultantLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> appointmentsByPatientLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> appointmentsByTimeSlotLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> upcomingAppointmentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> appointmentCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> conflictCheckLiveData = new MutableLiveData<>(false);

    public AppointmentViewModel() {
        super(AppointmentService.getInstance());
        this.appointmentService = (AppointmentService) service;
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Returns LiveData of all appointments */
    public LiveData<List<Appointment>> getAllAppointments() {
        return getItems();
    }

    /** Adds a new appointment */
    public void addAppointment(Appointment appointment) {
        addItem(appointment);
    }

    /** Updates an appointment by ID */
    public void updateAppointment(int id, Appointment updatedAppointment) {
        updateItem(id, updatedAppointment, Appointment::getId);
    }

    /** Deletes an appointment by ID */
    public void deleteAppointment(int id) {
        deleteItem(id, Appointment::getId);
    }

    /** Performs a predicate-based search */
    public void searchAppointments(Predicate<Appointment> predicate) {
        search(predicate);
    }

    /** Refreshes the appointment list */
    public void refreshAppointments() {
        refresh();
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ SPECIALIZED QUERIES (New methods from AppointmentService)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get appointments by consultant ID
     * @return LiveData that updates when appointments for this consultant change
     */
    public LiveData<List<Appointment>> getAppointmentsByConsultant(int consultantId) {
        appointmentService.fetchAppointmentsByConsultant(consultantId, new BaseService.DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                appointmentsByConsultantLiveData.postValue(appointments);
            }

            @Override
            public void onError(String error) {
                appointmentsByConsultantLiveData.postValue(null);
            }
        });
        return appointmentsByConsultantLiveData;
    }

    /**
     * Get appointments by patient ID
     * @return LiveData that updates when appointments for this patient change
     */
    public LiveData<List<Appointment>> getAppointmentsByPatient(int patientId) {
        appointmentService.fetchAppointmentsByPatient(patientId, new BaseService.DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                appointmentsByPatientLiveData.postValue(appointments);
            }

            @Override
            public void onError(String error) {
                appointmentsByPatientLiveData.postValue(null);
            }
        });
        return appointmentsByPatientLiveData;
    }

    /**
     * Get appointments by time slot ID
     * @return LiveData that updates when appointments for this time slot change
     */
    public LiveData<List<Appointment>> getAppointmentsByTimeSlot(String slotId) {
        appointmentService.fetchAppointmentsByTimeSlot(slotId, new BaseService.DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                appointmentsByTimeSlotLiveData.postValue(appointments);
            }

            @Override
            public void onError(String error) {
                appointmentsByTimeSlotLiveData.postValue(null);
            }
        });
        return appointmentsByTimeSlotLiveData;
    }

    /**
     * Get upcoming appointments for consultant
     * @return LiveData of upcoming appointments
     */
    public LiveData<List<Appointment>> getUpcomingAppointmentsByConsultant(int consultantId) {
        appointmentService.getUpcomingAppointmentsByConsultant(consultantId, new BaseService.DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                upcomingAppointmentsLiveData.postValue(appointments);
            }

            @Override
            public void onError(String error) {
                upcomingAppointmentsLiveData.postValue(null);
            }
        });
        return upcomingAppointmentsLiveData;
    }

    /**
     * Get upcoming appointments for patient
     * @return LiveData of upcoming appointments
     */
    public LiveData<List<Appointment>> getUpcomingAppointmentsByPatient(int patientId) {
        appointmentService.getUpcomingAppointmentsByPatient(patientId, new BaseService.DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                upcomingAppointmentsLiveData.postValue(appointments);
            }

            @Override
            public void onError(String error) {
                upcomingAppointmentsLiveData.postValue(null);
            }
        });
        return upcomingAppointmentsLiveData;
    }

    /**
     * Get appointments within a date range
     */
    public void getAppointmentsByDateRange(long startTime, long endTime, BaseService.DataCallback<Appointment> callback) {
        appointmentService.getAppointmentsByDateRange(startTime, endTime, callback);
    }

    /**
     * Get recent appointments (sorted by creation date)
     */
    public void getRecentAppointments(int limit, BaseService.DataCallback<Appointment> callback) {
        appointmentService.getRecentAppointments(limit, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ COUNT OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get total appointment count
     * @return LiveData of count
     */
    public LiveData<Integer> getAppointmentCount() {
        appointmentService.getAppointmentCount(count -> {
            appointmentCountLiveData.postValue(count);
        });
        return appointmentCountLiveData;
    }

    /**
     * Get appointment count by consultant
     */
    public void getAppointmentCountByConsultant(int consultantId, AppointmentService.CountCallback callback) {
        appointmentService.getAppointmentCountByConsultant(consultantId, callback);
    }

    /**
     * Get appointment count by patient
     */
    public void getAppointmentCountByPatient(int patientId, AppointmentService.CountCallback callback) {
        appointmentService.getAppointmentCountByPatient(patientId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ VALIDATION & CONFLICT CHECKING
    // ═══════════════════════════════════════════════════════════════

    /**
     * Check for conflicting appointments (same consultant + time slot)
     * @return LiveData of conflict status
     */
    public LiveData<Boolean> checkConflict(Appointment appointment) {
        appointmentService.checkConflict(appointment, hasConflict -> {
            conflictCheckLiveData.postValue(hasConflict);
        });
        return conflictCheckLiveData;
    }

    /**
     * Check if appointment exists by ID
     */
    public void appointmentExists(int appointmentId, AppointmentService.ExistsCallback callback) {
        appointmentService.appointmentExists(appointmentId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ BULK DELETE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Delete all appointments by consultant
     */
    public void deleteAppointmentsByConsultant(int consultantId, BaseService.DataCallback<Appointment> callback) {
        appointmentService.deleteAppointmentsByConsultant(consultantId, callback);
    }

    /**
     * Delete all appointments by patient
     */
    public void deleteAppointmentsByPatient(int patientId, BaseService.DataCallback<Appointment> callback) {
        appointmentService.deleteAppointmentsByPatient(patientId, callback);
    }

    /**
     * Get a single appointment by ID (async with callback)
     */
    public void fetchAppointmentById(int id, BaseService.SingleItemCallback<Appointment> callback) {
        appointmentService.fetchAppointmentById(id, callback);
    }

    /**
     * Add appointment with validation and conflict checking (recommended)
     */
    public void addAppointmentWithValidation(Appointment appointment, BaseService.DataCallback<Appointment> callback) {
        appointmentService.addAppointment(appointment, callback);
    }
}
