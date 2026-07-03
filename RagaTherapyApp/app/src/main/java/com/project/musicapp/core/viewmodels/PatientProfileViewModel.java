package com.project.musicapp.core.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.musicapp.core.models.Consultant;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.PatientProfileService;

import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated PatientProfileViewModel with proper async handling
 * Follows singleton pattern and uses callbacks for all Firebase operations
 */
public class PatientProfileViewModel extends BaseViewModel<PatientProfile> {

    private final PatientProfileService profileService;
    private final MutableLiveData<PatientProfile> selectedProfile = new MutableLiveData<>();
    private static final String TAG = "PatientProfileVM";

    public PatientProfileViewModel() {
        super(PatientProfileService.getInstance()); // ✅ Use singleton instead of new
        this.profileService = PatientProfileService.getInstance();
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Expose all patient profiles as LiveData */
    public LiveData<List<PatientProfile>> getAllProfiles() {
        return getItems();
    }

    /** Add a new patient profile */
    public void addProfile(PatientProfile profile) {
        addItem(profile);
    }

    /** Update a patient profile */
    public void updateProfile(int id, PatientProfile updatedProfile) {
        updateItem(id, updatedProfile, PatientProfile::getId);
    }

    /** Delete a patient profile */
    public void deleteProfile(int id) {
        deleteItem(id, PatientProfile::getId);
    }

    /** Search for profiles using predicate */
    public void searchProfiles(Predicate<PatientProfile> predicate) {
        search(predicate);
    }

    /** Refresh all profiles (reload data) */
    public void refreshProfiles() {
        refresh();
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ SPECIALIZED QUERY METHODS (Async with Callbacks)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetch patient profile by ID (async with callback)
     */
    public void fetchProfileById(int id, BaseService.SingleItemCallback<PatientProfile> callback) {
        profileService.fetchProfileById(id, callback);
    }

    public LiveData<PatientProfile> getSelectedProfile() {
        return selectedProfile;
    }

    public void fetchProfileById(int id) {
        Log.d(TAG, "Fetching patient profile async with ID: " + id);
        profileService.fetchProfileById(id, profile -> {
            if (profile != null) {
                Log.d(TAG, "✅ Found profile: " + profile.getUser().getName());
                selectedProfile.postValue(profile);
            } else {
                Log.w(TAG, "⚠ No profile found for ID: " + id);
                selectedProfile.postValue(null);
            }
        });
    }

    /**
     * Get patient profile by user ID
     */
    public void getProfileByUserId(int userId, BaseService.SingleItemCallback<PatientProfile> callback) {
        profileService.getProfileByUserId(userId, callback);
    }

    /**
     * Find profiles by city
     */
    public void findProfilesByCity(String city, BaseService.DataCallback<PatientProfile> callback) {
        profileService.findProfilesByCity(city, callback);
    }

    /**
     * Find profiles by state
     */
    public void findProfilesByState(String state, BaseService.DataCallback<PatientProfile> callback) {
        profileService.findProfilesByState(state, callback);
    }

    /**
     * Find profiles by severity level
     */
    public void findProfilesBySeverity(String severity, BaseService.DataCallback<PatientProfile> callback) {
        profileService.findProfilesBySeverity(severity, callback);
    }

    /**
     * Find profiles by urban/rural classification
     */
    public void findProfilesByUrbanRural(String urbanRural, BaseService.DataCallback<PatientProfile> callback) {
        profileService.findProfilesByUrbanRural(urbanRural, callback);
    }

    /**
     * Search patients by name
     */
    public void searchPatientsByName(String name, BaseService.DataCallback<PatientProfile> callback) {
        profileService.searchPatientsByName(name, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ CONSULTANT-RELATED OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Find profiles assigned to a specific consultant
     */
    public void findProfilesByConsultant(Consultant consultant, BaseService.DataCallback<PatientProfile> callback) {
        profileService.findProfilesByConsultant(consultant, callback);
    }

    /**
     * Find profiles by consultant ID
     */
    public void findProfilesByConsultantId(int consultantId, BaseService.DataCallback<PatientProfile> callback) {
        profileService.findProfilesByConsultantId(consultantId, callback);
    }

    /**
     * Assign consultant to patient
     */
    public void assignConsultant(int patientId, Consultant consultant, BaseService.DataCallback<PatientProfile> callback) {
        profileService.assignConsultant(patientId, consultant, callback);
    }

    /**
     * Remove consultant from patient
     */
    public void removeConsultant(int patientId, int consultantId, BaseService.DataCallback<PatientProfile> callback) {
        profileService.removeConsultant(patientId, consultantId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ COUNT & CHECK OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get total patient count
     */
    public void getPatientCount(PatientProfileService.CountCallback callback) {
        profileService.getPatientCount(callback);
    }

    /**
     * Get patient count by city
     */
    public void getPatientCountByCity(String city, PatientProfileService.CountCallback callback) {
        profileService.getPatientCountByCity(city, callback);
    }

    /**
     * Get patient count by severity
     */
    public void getPatientCountBySeverity(String severity, PatientProfileService.CountCallback callback) {
        profileService.getPatientCountBySeverity(severity, callback);
    }

    /**
     * Check if patient profile exists
     */
    public void profileExists(int profileId, PatientProfileService.ExistsCallback callback) {
        profileService.profileExists(profileId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get all unique cities
     */
    public void getAllCities(PatientProfileService.CitiesCallback callback) {
        profileService.getAllCities(callback);
    }

    /**
     * Get all unique states
     */
    public void getAllStates(PatientProfileService.StatesCallback callback) {
        profileService.getAllStates(callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ CRUD WITH CALLBACKS (For Error Handling)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Add profile with callback
     */
    public void addProfileWithCallback(PatientProfile profile, BaseService.DataCallback<PatientProfile> callback) {
        profileService.addProfile(profile, callback);
    }

    /**
     * Update profile with callback
     */
    public void updateProfileWithCallback(int id, PatientProfile updatedProfile,
                                          BaseService.DataCallback<PatientProfile> callback) {
        profileService.updateProfile(id, updatedProfile, callback);
    }

    /**
     * Delete profile with callback
     */
    public void deleteProfileWithCallback(int id, BaseService.DataCallback<PatientProfile> callback) {
        profileService.deleteProfile(id, callback);
    }

    /**
     * Search profiles with callback
     */
    public void searchProfilesWithCallback(Predicate<PatientProfile> predicate,
                                           BaseService.DataCallback<PatientProfile> callback) {
        profileService.searchProfiles(predicate, callback);
    }
}
