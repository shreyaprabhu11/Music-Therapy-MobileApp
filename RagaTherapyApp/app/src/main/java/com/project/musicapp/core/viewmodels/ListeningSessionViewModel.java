package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;

import com.project.musicapp.core.models.ListeningSession;
import com.project.musicapp.core.models.Music;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.ListeningSessionService;

import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated ListeningSessionViewModel with proper async handling
 * Follows the same pattern as MusicViewModel and other ViewModels
 */
public class ListeningSessionViewModel extends BaseViewModel<ListeningSession> {

    private final ListeningSessionService sessionService;

    public ListeningSessionViewModel() {
        super(ListeningSessionService.getInstance()); // ✅ Already using singleton
        this.sessionService = ListeningSessionService.getInstance();
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Returns LiveData of all sessions */
    public LiveData<List<ListeningSession>> getAllSessions() {
        return getItems();
    }

    /** Adds a new session */
    public void addSession(ListeningSession session) {
        addItem(session);
    }

    /** Updates a session by ID */
    public void updateSession(int id, ListeningSession updatedSession) {
        updateItem(id, updatedSession, ListeningSession::getId);
    }

    /** Deletes a session by ID */
    public void deleteSession(int id) {
        deleteItem(id, ListeningSession::getId);
    }

    /** Performs a search asynchronously using a predicate */
    public void searchSessions(Predicate<ListeningSession> predicate) {
        search(predicate);
    }

    /** Refreshes session list from Firebase */
    public void refreshSessions() {
        refresh();
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ SPECIALIZED QUERY METHODS (Async with Callbacks)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetch session by ID (async with callback)
     */
    public void fetchSessionById(int id, BaseService.SingleItemCallback<ListeningSession> callback) {
        sessionService.fetchSessionById(id, callback);
    }

    /**
     * Get sessions for a specific patient
     */
    public void getSessionsByPatient(PatientProfile patient, BaseService.DataCallback<ListeningSession> callback) {
        sessionService.getSessionsByPatient(patient, callback);
    }

    /**
     * Get sessions for a specific patient by ID
     */
    public void getSessionsByPatientId(int patientId, BaseService.DataCallback<ListeningSession> callback) {
        sessionService.getSessionsByPatientId(patientId, callback);
    }

    /**
     * Get sessions for a specific music
     */
    public void getSessionsByMusic(Music music, BaseService.DataCallback<ListeningSession> callback) {
        sessionService.getSessionsByMusic(music, callback);
    }

    /**
     * Get sessions by date (timestamp in milliseconds)
     */
    public void getSessionsByDate(long timestamp, BaseService.DataCallback<ListeningSession> callback) {
        sessionService.getSessionsByDate(timestamp, callback);
    }

    /**
     * Get sessions within a date range (timestamps in milliseconds)
     */
    public void getSessionsByDateRange(long startTimestamp, long endTimestamp,
                                       BaseService.DataCallback<ListeningSession> callback) {
        sessionService.getSessionsByDateRange(startTimestamp, endTimestamp, callback);
    }

    /**
     * Get sessions for today
     */
    public void getTodaySessions(BaseService.DataCallback<ListeningSession> callback) {
        sessionService.getTodaySessions(callback);
    }

    /**
     * Get sessions for a specific week
     */
    public void getWeeklySessions(long weekStartTimestamp, BaseService.DataCallback<ListeningSession> callback) {
        sessionService.getWeeklySessions(weekStartTimestamp, callback);
    }

    /**
     * Get sessions for a specific month
     */
    public void getMonthlySessions(long monthStartTimestamp, BaseService.DataCallback<ListeningSession> callback) {
        sessionService.getMonthlySessions(monthStartTimestamp, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ ANALYTICS METHODS (Async with Callbacks)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get total listening duration for a patient
     */
    public void getTotalListeningDuration(PatientProfile patient,
                                          ListeningSessionService.DurationCallback callback) {
        sessionService.getTotalListeningDuration(patient, callback);
    }

    /**
     * Get total listening duration for a patient within date range
     */
    public void getTotalListeningDurationByDateRange(PatientProfile patient, long startTimestamp,
                                                     long endTimestamp,
                                                     ListeningSessionService.DurationCallback callback) {
        sessionService.getTotalListeningDurationByDateRange(patient, startTimestamp, endTimestamp, callback);
    }

    /**
     * Get listening session count for a patient
     */
    public void getSessionCountByPatient(PatientProfile patient,
                                         ListeningSessionService.CountCallback callback) {
        sessionService.getSessionCountByPatient(patient, callback);
    }

    /**
     * Get total session count
     */
    public void getTotalSessionCount(ListeningSessionService.CountCallback callback) {
        sessionService.getTotalSessionCount(callback);
    }

    /**
     * Get average listening duration for a patient
     */
    public void getAverageListeningDuration(PatientProfile patient,
                                            ListeningSessionService.DurationCallback callback) {
        sessionService.getAverageListeningDuration(patient, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ BULK OPERATIONS WITH CALLBACKS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Add session with callback (recommended for error handling)
     */
    public void addSessionWithCallback(ListeningSession session, BaseService.DataCallback<ListeningSession> callback) {
        sessionService.addSession(session, callback);
    }

    /**
     * Update session with callback
     */
    public void updateSessionWithCallback(int id, ListeningSession updatedSession,
                                          BaseService.DataCallback<ListeningSession> callback) {
        sessionService.updateSession(id, updatedSession, callback);
    }

    /**
     * Delete session with callback
     */
    public void deleteSessionWithCallback(int id, BaseService.DataCallback<ListeningSession> callback) {
        sessionService.deleteSession(id, callback);
    }

    /**
     * Delete all sessions for a patient
     */
    public void deleteSessionsByPatient(PatientProfile patient, BaseService.DataCallback<ListeningSession> callback) {
        sessionService.deleteSessionsByPatient(patient, callback);
    }

    /**
     * Search sessions with callback
     */
    public void searchSessionsWithCallback(Predicate<ListeningSession> predicate,
                                           BaseService.DataCallback<ListeningSession> callback) {
        sessionService.searchSessions(predicate, callback);
    }
}
