package com.project.musicapp.core.services;

import android.net.DnsResolver;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.models.Consultant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Firebase-integrated service for managing PatientProfile entities.
 * Uses BaseService for real-time CRUD operations with async callbacks.
 */
public class PatientProfileService extends BaseService<PatientProfile> {
    private static PatientProfileService instance;
    private static final String TAG = "PatientProfileService";

    public PatientProfileService() {
        super(PatientProfile.class, "patientProfiles"); // Firebase node name
    }

    // ✅ SINGLETON PATTERN
    public static synchronized PatientProfileService getInstance() {
        if (instance == null) {
            instance = new PatientProfileService();
        }
        return instance;
    }

    /**
     * Fetch all patient profiles
     */
    public void fetchAllProfiles(DataCallback<PatientProfile> callback) {
        fetchAllAsync(callback);
    }

    public boolean updateProfile(int id, PatientProfile updatedProfile) {
        Log.d(TAG, "Updating patient profile with ID: " + id);
        return updateItem(id, updatedProfile, PatientProfile::getId);
    }

    /**
     * Fetch a specific patient profile by ID
     */
    public void fetchProfileById(int id, SingleItemCallback<PatientProfile> callback) {
        fetchByIdAsync(id, PatientProfile::getId, callback);
    }

    public void fetchProfileById(int id, Callback<PatientProfile> callback) {
        Log.d(TAG, "Fetching patient profile by ID async: " + id);
        databaseReference.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PatientProfile found = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    PatientProfile p = child.getValue(PatientProfile.class);
                    if (p != null) {
                        found = p;
                        break;
                    }
                }
                Log.d(TAG, found != null
                        ? "✅ Found patient profile with ID " + id
                        : "⚠ No patient profile found with ID " + id);
                callback.onResult(found);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ fetchProfileByIdAsync cancelled: " + error.getMessage());
                callback.onResult(null);
            }
        });
    }

    /**
     * Add a new patient profile
     */
    public void addProfile(PatientProfile profile, DataCallback<PatientProfile> callback) {
        addItem(profile, callback);
    }

    /**
     * Update an existing patient profile
     */
    public void updateProfile(int id, PatientProfile updatedProfile, DataCallback<PatientProfile> callback) {
        updateItem(id, updatedProfile, PatientProfile::getId, callback);
    }

    /**
     * Delete a patient profile by ID
     */
    public void deleteProfile(int id, DataCallback<PatientProfile> callback) {
        deleteById(id, PatientProfile::getId, callback);
    }

    /**
     * Search profiles by a given predicate
     */
    public void searchProfiles(java.util.function.Predicate<PatientProfile> predicate, DataCallback<PatientProfile> callback) {
        search(predicate, callback);
    }

    /**
     * Find all patients by city
     */
    public void findProfilesByCity(String city, DataCallback<PatientProfile> callback) {
        if (city == null || city.trim().isEmpty()) {
            callback.onError("City cannot be null or empty");
            return;
        }
        search(p -> p.getCity() != null && p.getCity().equalsIgnoreCase(city), callback);
    }

    /**
     * Find all patients by state
     */
    public void findProfilesByState(String state, DataCallback<PatientProfile> callback) {
        if (state == null || state.trim().isEmpty()) {
            callback.onError("State cannot be null or empty");
            return;
        }
        search(p -> p.getState() != null && p.getState().equalsIgnoreCase(state), callback);
    }

    /**
     * Find patients by severity level
     */
    public void findProfilesBySeverity(String severity, DataCallback<PatientProfile> callback) {
        if (severity == null || severity.trim().isEmpty()) {
            callback.onError("Severity cannot be null or empty");
            return;
        }
        search(p -> p.getSeverity() != null && p.getSeverity().equalsIgnoreCase(severity), callback);
    }

    /**
     * Find patients by urban/rural classification
     */
    public void findProfilesByUrbanRural(String urbanRural, DataCallback<PatientProfile> callback) {
        if (urbanRural == null || urbanRural.trim().isEmpty()) {
            callback.onError("Urban/Rural cannot be null or empty");
            return;
        }
        search(p -> p.getUrbanRural() != null && p.getUrbanRural().equalsIgnoreCase(urbanRural), callback);
    }

    /**
     * Get patient profile by user ID
     */
    public void getProfileByUserId(int userId, SingleItemCallback<PatientProfile> callback) {
        search(p -> p.getUser() != null && p.getUser().getId() == userId, new DataCallback<PatientProfile>() {
            @Override
            public void onSuccess(List<PatientProfile> profiles) {
                if (!profiles.isEmpty()) {
                    callback.onSuccess(profiles.get(0));
                } else {
                    callback.onError("No profile found for user ID: " + userId);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Find patients assigned to a specific consultant
     */
    public void findProfilesByConsultant(Consultant consultant, DataCallback<PatientProfile> callback) {
        if (consultant == null) {
            callback.onError("Consultant cannot be null");
            return;
        }

        search(p -> {
            if (p.getConsultants() == null) return false;
            return p.getConsultants().stream()
                    .anyMatch(c -> c.getConsultantId() == consultant.getConsultantId());
        }, callback);
    }

    /**
     * Find patients by consultant ID
     */
    public void findProfilesByConsultantId(int consultantId, DataCallback<PatientProfile> callback) {
        search(p -> {
            if (p.getConsultants() == null) return false;
            return p.getConsultants().stream()
                    .anyMatch(c -> c.getConsultantId() == consultantId);
        }, callback);
    }

    /**
     * Get patient count
     */
    public void getPatientCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<PatientProfile>() {
            @Override
            public void onSuccess(List<PatientProfile> profiles) {
                callback.onCount(profiles.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get patient count by city
     */
    public void getPatientCountByCity(String city, CountCallback callback) {
        findProfilesByCity(city, new DataCallback<PatientProfile>() {
            @Override
            public void onSuccess(List<PatientProfile> profiles) {
                callback.onCount(profiles.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get patient count by severity
     */
    public void getPatientCountBySeverity(String severity, CountCallback callback) {
        findProfilesBySeverity(severity, new DataCallback<PatientProfile>() {
            @Override
            public void onSuccess(List<PatientProfile> profiles) {
                callback.onCount(profiles.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get all unique cities
     */
    public void getAllCities(CitiesCallback callback) {
        fetchAllAsync(new DataCallback<PatientProfile>() {
            @Override
            public void onSuccess(List<PatientProfile> profiles) {
                Set<String> citySet = new HashSet<>();
                for (PatientProfile profile : profiles) {
                    if (profile.getCity() != null && !profile.getCity().isEmpty()) {
                        citySet.add(profile.getCity());
                    }
                }
                callback.onCities(new ArrayList<>(citySet));
            }

            @Override
            public void onError(String error) {
                callback.onCities(new ArrayList<>());
            }
        });
    }

    /**
     * Get all unique states
     */
    public void getAllStates(StatesCallback callback) {
        fetchAllAsync(new DataCallback<PatientProfile>() {
            @Override
            public void onSuccess(List<PatientProfile> profiles) {
                Set<String> stateSet = new HashSet<>();
                for (PatientProfile profile : profiles) {
                    if (profile.getState() != null && !profile.getState().isEmpty()) {
                        stateSet.add(profile.getState());
                    }
                }
                callback.onStates(new ArrayList<>(stateSet));
            }

            @Override
            public void onError(String error) {
                callback.onStates(new ArrayList<>());
            }
        });
    }

    /**
     * Check if patient profile exists by ID
     */
    public void profileExists(int profileId, ExistsCallback callback) {
        fetchByIdAsync(profileId, PatientProfile::getId, new SingleItemCallback<PatientProfile>() {
            @Override
            public void onSuccess(PatientProfile profile) {
                callback.onResult(true);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Assign consultant to patient
     */
    public void assignConsultant(int patientId, Consultant consultant, DataCallback<PatientProfile> callback) {
        if (consultant == null) {
            callback.onError("Consultant cannot be null");
            return;
        }

        fetchProfileById(patientId, new SingleItemCallback<PatientProfile>() {
            @Override
            public void onSuccess(PatientProfile profile) {
                List<Consultant> consultants = profile.getConsultants();
                if (consultants == null) {
                    consultants = new ArrayList<>();
                }

                // Check if consultant already assigned
                boolean alreadyAssigned = consultants.stream()
                        .anyMatch(c -> c.getConsultantId() == consultant.getConsultantId());

                if (!alreadyAssigned) {
                    consultants.add(consultant);
                    profile.setConsultants(consultants);
                    updateProfile(patientId, profile, callback);
                } else {
                    callback.onError("Consultant already assigned to this patient");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError("Patient not found: " + error);
            }
        });
    }

    /**
     * Remove consultant from patient
     */
    public void removeConsultant(int patientId, int consultantId, DataCallback<PatientProfile> callback) {
        fetchProfileById(patientId, new SingleItemCallback<PatientProfile>() {
            @Override
            public void onSuccess(PatientProfile profile) {
                List<Consultant> consultants = profile.getConsultants();
                if (consultants == null || consultants.isEmpty()) {
                    callback.onError("Patient has no assigned consultants");
                    return;
                }

                boolean removed = consultants.removeIf(c -> c.getConsultantId() == consultantId);

                if (removed) {
                    profile.setConsultants(consultants);
                    updateProfile(patientId, profile, callback);
                } else {
                    callback.onError("Consultant not found in patient's consultant list");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError("Patient not found: " + error);
            }
        });
    }

    /**
     * Search patients by name (from User object)
     */
    public void searchPatientsByName(String name, DataCallback<PatientProfile> callback) {
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Name cannot be null or empty");
            return;
        }

        String lowerName = name.toLowerCase();
        search(p -> p.getUser() != null &&
                p.getUser().getName() != null &&
                p.getUser().getName().toLowerCase().contains(lowerName), callback);
    }

    // ✅ CUSTOM CALLBACK INTERFACES
    public interface CountCallback {
        void onCount(int count);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
    }

    public interface CitiesCallback {
        void onCities(List<String> cities);
    }

    public interface StatesCallback {
        void onStates(List<String> states);
    }
    public interface Callback<T> {
        void onResult(T result);
    }
}
