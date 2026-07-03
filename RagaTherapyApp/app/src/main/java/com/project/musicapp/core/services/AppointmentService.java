package com.project.musicapp.core.services;

import com.project.musicapp.core.models.Appointment;
import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.models.TimeSlot;
import java.util.ArrayList;
import java.util.List;

/**
 * Firebase-integrated AppointmentService extending BaseService.
 * All CRUD operations are persisted in Firebase Realtime Database with async callbacks.
 */
public class AppointmentService extends BaseService<Appointment> {
    private static AppointmentService instance;

    public AppointmentService() {
        super(Appointment.class, "appointments");
    }

    // ✅ SINGLETON PATTERN
    public static synchronized AppointmentService getInstance() {
        if (instance == null) {
            instance = new AppointmentService();
        }
        return instance;
    }

    /**
     * Fetch all appointments from Firebase
     */
    public void fetchAllAppointments(DataCallback<Appointment> callback) {
        fetchAllAsync(callback);
    }

    /**
     * Fetch a single appointment by ID
     */
    public void fetchAppointmentById(int id, SingleItemCallback<Appointment> callback) {
        fetchByIdAsync(id, Appointment::getId, callback);
    }

    public boolean addAppointment(Appointment appointment) {
        addItem(appointment);
        return true;
    }

    /**
     * Add a new appointment
     */
    public void addAppointment(Appointment appointment, DataCallback<Appointment> callback) {
        if (appointment == null) {
            callback.onError("Appointment cannot be null");
            return;
        }

        // Validate required fields
        if (appointment.getConsultant() == null) {
            callback.onError("Consultant is required");
            return;
        }
        if (appointment.getPatientProfile() == null) {
            callback.onError("Patient is required");
            return;
        }
        if (appointment.getTimeSlot() == null) {
            callback.onError("Time slot is required");
            return;
        }

        // Check for conflicting appointments
        checkConflict(appointment, new ExistsCallback() {
            @Override
            public void onResult(boolean hasConflict) {
                if (hasConflict) {
                    callback.onError("Time slot already booked for this consultant");
                } else {
                    addItem(appointment, callback);
                }
            }
        });
    }

    /**
     * Update an existing appointment
     */
    public void updateAppointment(int id, Appointment updatedAppointment, DataCallback<Appointment> callback) {
        updateItem(id, updatedAppointment, Appointment::getId, callback);
    }

    /**
     * Delete an appointment by ID
     */
    public void deleteAppointment(int id, DataCallback<Appointment> callback) {
        deleteById(id, Appointment::getId, callback);
    }

    /**
     * Search appointments based on a condition
     */
    public void searchAppointments(java.util.function.Predicate<Appointment> predicate, DataCallback<Appointment> callback) {
        search(predicate, callback);
    }

    /**
     * Fetch appointments by consultant ID
     */
    public void fetchAppointmentsByConsultant(int consultantId, DataCallback<Appointment> callback) {
        search(appointment -> appointment.getConsultant() != null &&
                appointment.getConsultant().getConsultantId() == consultantId, callback);
    }

    /**
     * Fetch appointments by patient ID
     */
    public void fetchAppointmentsByPatient(int patientId, DataCallback<Appointment> callback) {
        search(appointment -> appointment.getPatientProfile() != null &&
                appointment.getPatientProfile().getId() == patientId, callback);
    }

    /**
     * Fetch appointments by time slot ID
     */
    public void fetchAppointmentsByTimeSlot(String slotId, DataCallback<Appointment> callback) {
        if (slotId == null || slotId.trim().isEmpty()) {
            callback.onError("Slot ID cannot be null or empty");
            return;
        }

        search(appointment -> appointment.getTimeSlot() != null &&
                appointment.getTimeSlot().getSlotId() != null &&
                appointment.getTimeSlot().getSlotId().equals(slotId), callback);
    }

    /**
     * Check for conflicting appointments (same consultant + time slot)
     */
    public void checkConflict(Appointment appointment, ExistsCallback callback) {
        if (appointment.getConsultant() == null || appointment.getTimeSlot() == null) {
            callback.onResult(false);
            return;
        }

        int consultantId = appointment.getConsultant().getConsultantId();
        String slotId = appointment.getTimeSlot().getSlotId();

        search(a -> a.getConsultant() != null &&
                        a.getConsultant().getConsultantId() == consultantId &&
                        a.getTimeSlot() != null &&
                        a.getTimeSlot().getSlotId() != null &&
                        a.getTimeSlot().getSlotId().equals(slotId) &&
                        a.getId() != appointment.getId(), // Exclude self when updating
                new DataCallback<Appointment>() {
                    @Override
                    public void onSuccess(List<Appointment> appointments) {
                        callback.onResult(!appointments.isEmpty());
                    }

                    @Override
                    public void onError(String error) {
                        callback.onResult(false);
                    }
                });
    }

    /**
     * Get appointment count
     */
    public void getAppointmentCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                callback.onCount(appointments.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get appointment count by consultant
     */
    public void getAppointmentCountByConsultant(int consultantId, CountCallback callback) {
        fetchAppointmentsByConsultant(consultantId, new DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                callback.onCount(appointments.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get appointment count by patient
     */
    public void getAppointmentCountByPatient(int patientId, CountCallback callback) {
        fetchAppointmentsByPatient(patientId, new DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                callback.onCount(appointments.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get appointments by creation date range
     */
    public void getAppointmentsByDateRange(long startTime, long endTime, DataCallback<Appointment> callback) {
        if (startTime <= 0 || endTime <= 0) {
            callback.onError("Invalid time range");
            return;
        }

        search(appointment -> appointment.getCreatedAt() >= startTime &&
                appointment.getCreatedAt() <= endTime, callback);
    }

    /**
     * Get recent appointments (sorted by creation date)
     */
    public void getRecentAppointments(int limit, DataCallback<Appointment> callback) {
        fetchAllAsync(new DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                // Sort by createdAt descending
                appointments.sort((a1, a2) -> Long.compare(a2.getCreatedAt(), a1.getCreatedAt()));

                List<Appointment> recent = appointments.subList(0, Math.min(limit, appointments.size()));
                callback.onSuccess(recent);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Get upcoming appointments for consultant
     */
    public void getUpcomingAppointmentsByConsultant(int consultantId, DataCallback<Appointment> callback) {
        long currentTime = System.currentTimeMillis();

        search(appointment -> appointment.getConsultant() != null &&
                appointment.getConsultant().getConsultantId() == consultantId &&
                appointment.getCreatedAt() >= currentTime, callback);
    }

    /**
     * Get upcoming appointments for patient
     */
    public void getUpcomingAppointmentsByPatient(int patientId, DataCallback<Appointment> callback) {
        long currentTime = System.currentTimeMillis();

        search(appointment -> appointment.getPatientProfile() != null &&
                appointment.getPatientProfile().getId() == patientId &&
                appointment.getCreatedAt() >= currentTime, callback);
    }

    /**
     * Delete all appointments by consultant
     */
    public void deleteAppointmentsByConsultant(int consultantId, DataCallback<Appointment> callback) {
        fetchAppointmentsByConsultant(consultantId, new DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                if (appointments.isEmpty()) {
                    callback.onSuccess(appointments);
                    return;
                }

                int[] deletedCount = {0};
                for (Appointment appointment : appointments) {
                    deleteAppointment(appointment.getId(), new DataCallback<Appointment>() {
                        @Override
                        public void onSuccess(List<Appointment> deletedAppointments) {
                            deletedCount[0]++;
                            if (deletedCount[0] == appointments.size()) {
                                callback.onSuccess(appointments);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to delete appointment: " + error);
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
     * Delete all appointments by patient
     */
    public void deleteAppointmentsByPatient(int patientId, DataCallback<Appointment> callback) {
        fetchAppointmentsByPatient(patientId, new DataCallback<Appointment>() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                if (appointments.isEmpty()) {
                    callback.onSuccess(appointments);
                    return;
                }

                int[] deletedCount = {0};
                for (Appointment appointment : appointments) {
                    deleteAppointment(appointment.getId(), new DataCallback<Appointment>() {
                        @Override
                        public void onSuccess(List<Appointment> deletedAppointments) {
                            deletedCount[0]++;
                            if (deletedCount[0] == appointments.size()) {
                                callback.onSuccess(appointments);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to delete appointment: " + error);
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
     * Check if appointment exists
     */
    public void appointmentExists(int appointmentId, ExistsCallback callback) {
        fetchByIdAsync(appointmentId, Appointment::getId, new SingleItemCallback<Appointment>() {
            @Override
            public void onSuccess(Appointment appointment) {
                callback.onResult(true);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
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
}
