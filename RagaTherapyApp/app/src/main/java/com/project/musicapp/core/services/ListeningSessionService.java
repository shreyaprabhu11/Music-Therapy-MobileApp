package com.project.musicapp.core.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.project.musicapp.core.models.ListeningSession;
import java.util.Calendar;
import com.project.musicapp.core.models.PatientProfile;
import com.project.musicapp.core.models.Music;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebase-integrated ListeningSessionService.
 * Extends BaseService for real-time Firebase CRUD operations with async callbacks.
 * Date is stored as long (timestamp in milliseconds).
 */
public class ListeningSessionService extends BaseService<ListeningSession> {
    private static ListeningSessionService instance;

    public ListeningSessionService() {
        super(ListeningSession.class, "listeningSessions");
    }

    public List<ListeningSession> fetchAllSessions() {
        return fetchAll();
    }

    /**
     * Singleton pattern for consistent Firebase reference
     */
    public static synchronized ListeningSessionService getInstance() {
        if (instance == null) {
            instance = new ListeningSessionService();
        }
        return instance;
    }

    public void addOrUpdateTodaySessionAsync(PatientProfile patient, Music music, int secondsPlayed) {
        if (patient == null || music == null) return;

        fetchAllSessionsAsync(sessions -> {
            long now = System.currentTimeMillis();
            ListeningSession todaySession = null;

            for (ListeningSession s : sessions) {
                if (s.getPatientProfile() == null || s.getMusic() == null) continue;

                boolean samePatient = s.getPatientProfile().getId() == patient.getId();
                boolean sameMusic = s.getMusic().getMusicUrl().equals(music.getMusicUrl());
                boolean sameDay = isSameDay(s.getDate(), now);

                if (samePatient && sameMusic && sameDay) {
                    todaySession = s;
                    break;
                }
            }

            if (todaySession != null) {
                // Update existing session
                int newDuration = todaySession.getDuration() + secondsPlayed;
                todaySession.setDuration(newDuration);
                updateSession(todaySession.getId(), todaySession);
                Log.d("ListeningLogger", "🔄 Updated session ID " + todaySession.getId()
                        + " | New duration: " + newDuration + "s");
            } else {
                // Add new session
                ListeningSession session = new ListeningSession(
                        0, patient, music, secondsPlayed, System.currentTimeMillis()
                );
                addSession(session);
                Log.d("ListeningLogger", "➕ Added new session for " + music.getMusicUrl()
                        + " (" + secondsPlayed + "s)");
            }
        });
    }

    /**
     * Fetch all sessions from Firebase
     */
    public void fetchAllSessions(DataCallback<ListeningSession> callback) {
        fetchAllAsync(callback);
    }

    public void fetchAllSessionsAsync(ListeningSessionsCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ListeningSession> sessions = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ListeningSession session = snap.getValue(ListeningSession.class);
                    if (session != null) sessions.add(session);
                }
                Log.d("SessionCheck", "Fetched " + sessions.size() + " sessions from Firebase");
                callback.onSessionsFetched(sessions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SessionCheck", "Failed to fetch sessions: " + error.getMessage());
                callback.onSessionsFetched(new ArrayList<>());
            }
        });
    }

    public interface ListeningSessionsCallback {
        void onSessionsFetched(List<ListeningSession> sessions);
    }

    /**
     * Fetch a session by ID
     */
    public void fetchSessionById(int id, SingleItemCallback<ListeningSession> callback) {
        fetchByIdAsync(id, ListeningSession::getId, callback);
    }

    /**
     * Add a new session to Firebase
     */
    public void addSession(ListeningSession session, DataCallback<ListeningSession> callback) {
        addItem(session, callback);
    }

    public boolean addSession(ListeningSession session) {
        addItem(session);
        return true;
    }

    /**
     * Update a session in Firebase
     */
    public void updateSession(int id, ListeningSession updatedSession, DataCallback<ListeningSession> callback) {
        updateItem(id, updatedSession, ListeningSession::getId, callback);
    }

    public boolean updateSession(int id, ListeningSession updatedSession) {
        return updateItem(id, updatedSession, ListeningSession::getId);
    }

    /**
     * Delete a session from Firebase
     */
    public void deleteSession(int id, DataCallback<ListeningSession> callback) {
        deleteById(id, ListeningSession::getId, callback);
    }

    /**
     * Search sessions based on a predicate
     */
    public void searchSessions(java.util.function.Predicate<ListeningSession> predicate, DataCallback<ListeningSession> callback) {
        search(predicate, callback);
    }

    /**
     * Get total listening duration for a patient (ASYNC)
     */
    public void getTotalListeningDuration(PatientProfile patient, DurationCallback callback) {
        if (patient == null) {
            callback.onDuration(0);
            return;
        }

        fetchAllSessions(new DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> sessions) {
                int totalDuration = 0;
                for (ListeningSession session : sessions) {
                    if (session.getPatientProfile() != null &&
                            session.getPatientProfile().getId() == patient.getId()) {
                        totalDuration += session.getDuration();
                    }
                }
                callback.onDuration(totalDuration);
            }

            @Override
            public void onError(String error) {
                callback.onDuration(0);
            }
        });
    }

    /**
     * Get sessions for a specific patient
     */
    public void getSessionsByPatient(PatientProfile patient, DataCallback<ListeningSession> callback) {
        if (patient == null) {
            callback.onError("Patient cannot be null");
            return;
        }

        search(session -> session.getPatientProfile() != null &&
                session.getPatientProfile().getId() == patient.getId(), callback);
    }

    /**
     * Get sessions for a specific patient by ID
     */
    public void getSessionsByPatientId(int patientId, DataCallback<ListeningSession> callback) {
        search(session -> session.getPatientProfile() != null &&
                session.getPatientProfile().getId() == patientId, callback);
    }

    /**
     * Get sessions for a specific music
     */
    public void getSessionsByMusic(Music music, DataCallback<ListeningSession> callback) {
        if (music == null) {
            callback.onError("Music cannot be null");
            return;
        }

        search(session -> session.getMusic() != null &&
                session.getMusic().getId() == music.getId(), callback);
    }

    /**
     * Get sessions by date (timestamp in milliseconds)
     */
    public void getSessionsByDate(long timestamp, DataCallback<ListeningSession> callback) {
        if (timestamp <= 0) {
            callback.onError("Invalid timestamp");
            return;
        }

        search(session -> session.getDate() > 0 &&
                isSameDay(session.getDate(), timestamp), callback);
    }

    /**
     * Get sessions within a date range (timestamps in milliseconds)
     */
    public void getSessionsByDateRange(long startTimestamp, long endTimestamp, DataCallback<ListeningSession> callback) {
        if (startTimestamp <= 0 || endTimestamp <= 0) {
            callback.onError("Invalid timestamp range");
            return;
        }

        search(session -> {
            if (session.getDate() <= 0) return false;
            return session.getDate() >= startTimestamp &&
                    session.getDate() <= endTimestamp;
        }, callback);
    }

    /**
     * Get total listening duration for a patient within date range
     */
    public void getTotalListeningDurationByDateRange(PatientProfile patient, long startTimestamp, long endTimestamp, DurationCallback callback) {
        if (patient == null) {
            callback.onDuration(0);
            return;
        }

        getSessionsByDateRange(startTimestamp, endTimestamp, new DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> sessions) {
                int totalDuration = 0;
                for (ListeningSession session : sessions) {
                    if (session.getPatientProfile() != null &&
                            session.getPatientProfile().getId() == patient.getId()) {
                        totalDuration += session.getDuration();
                    }
                }
                callback.onDuration(totalDuration);
            }

            @Override
            public void onError(String error) {
                callback.onDuration(0);
            }
        });
    }

    /**
     * Get listening session count for a patient
     */
    public void getSessionCountByPatient(PatientProfile patient, CountCallback callback) {
        if (patient == null) {
            callback.onCount(0);
            return;
        }

        getSessionsByPatient(patient, new DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> sessions) {
                callback.onCount(sessions.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get total session count
     */
    public void getTotalSessionCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> sessions) {
                callback.onCount(sessions.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get average listening duration for a patient
     */
    public void getAverageListeningDuration(PatientProfile patient, DurationCallback callback) {
        if (patient == null) {
            callback.onDuration(0);
            return;
        }

        getSessionsByPatient(patient, new DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> sessions) {
                if (sessions.isEmpty()) {
                    callback.onDuration(0);
                    return;
                }

                int totalDuration = 0;
                for (ListeningSession session : sessions) {
                    totalDuration += session.getDuration();
                }
                int average = totalDuration / sessions.size();
                callback.onDuration(average);
            }

            @Override
            public void onError(String error) {
                callback.onDuration(0);
            }
        });
    }

    /**
     * Delete all sessions for a patient
     */
    public void deleteSessionsByPatient(PatientProfile patient, DataCallback<ListeningSession> callback) {
        if (patient == null) {
            callback.onError("Patient cannot be null");
            return;
        }

        getSessionsByPatient(patient, new DataCallback<ListeningSession>() {
            @Override
            public void onSuccess(List<ListeningSession> sessions) {
                if (sessions.isEmpty()) {
                    callback.onSuccess(sessions);
                    return;
                }

                // Delete each session
                int[] deletedCount = {0};
                for (ListeningSession session : sessions) {
                    deleteSession(session.getId(), new DataCallback<ListeningSession>() {
                        @Override
                        public void onSuccess(List<ListeningSession> deletedSessions) {
                            deletedCount[0]++;
                            if (deletedCount[0] == sessions.size()) {
                                callback.onSuccess(sessions);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to delete session: " + error);
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
     * Get sessions for today
     */
    public void getTodaySessions(DataCallback<ListeningSession> callback) {
        long todayStart = getTodayStartTimestamp();
        long todayEnd = getTodayEndTimestamp();
        getSessionsByDateRange(todayStart, todayEnd, callback);
    }

    /**
     * Get sessions for a specific week
     */
    public void getWeeklySessions(long weekStartTimestamp, DataCallback<ListeningSession> callback) {
        long weekEndTimestamp = weekStartTimestamp + (7 * 24 * 60 * 60 * 1000L); // Add 7 days
        getSessionsByDateRange(weekStartTimestamp, weekEndTimestamp, callback);
    }

    /**
     * Get sessions for a specific month
     */
    public void getMonthlySessions(long monthStartTimestamp, DataCallback<ListeningSession> callback) {
        long monthEndTimestamp = monthStartTimestamp + (30 * 24 * 60 * 60 * 1000L); // Add 30 days (approx)
        getSessionsByDateRange(monthStartTimestamp, monthEndTimestamp, callback);
    }

    // ✅ HELPER METHODS FOR LONG TIMESTAMPS

    /**
     * Check if two timestamps are on the same day
     */
    public static boolean isSameDay(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }



    /**
     * Get today's start timestamp (00:00:00)
     */
    private long getTodayStartTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Get today's end timestamp (23:59:59)
     */
    private long getTodayEndTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    // ✅ CUSTOM CALLBACK INTERFACES
    public interface DurationCallback {
        void onDuration(int totalDuration);
    }

    public interface CountCallback {
        void onCount(int count);
    }
}
