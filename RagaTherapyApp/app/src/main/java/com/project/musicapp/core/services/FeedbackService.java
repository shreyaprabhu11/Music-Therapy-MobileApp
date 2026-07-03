package com.project.musicapp.core.services;

import com.project.musicapp.core.models.Feedback;
import java.util.List;

/**
 * FeedbackService extending BaseService for managing feedback data.
 * All operations use async callbacks for Firebase integration.
 */
public class FeedbackService extends BaseService<Feedback> {
    private static FeedbackService instance;

    private FeedbackService() {
        super(Feedback.class, "feedbacks"); // Firebase node name
    }

    // ✅ SINGLETON PATTERN
    public static synchronized FeedbackService getInstance() {
        if (instance == null) {
            instance = new FeedbackService();
        }
        return instance;
    }

    /**
     * Fetch all feedbacks
     */
    public void fetchAllFeedbacks(DataCallback<Feedback> callback) {
        fetchAllAsync(callback);
    }

    /**
     * Fetch single feedback by ID
     */
    public void fetchFeedbackById(int feedbackId, SingleItemCallback<Feedback> callback) {
        fetchByIdAsync(feedbackId, Feedback::getId, callback);
    }

    /**
     * Add new feedback
     */
    public void addFeedback(Feedback feedback, DataCallback<Feedback> callback) {
        addItem(feedback, callback);
    }

    /**
     * Update feedback
     */
    public void updateFeedback(int feedbackId, Feedback updatedFeedback, DataCallback<Feedback> callback) {
        updateItem(feedbackId, updatedFeedback, Feedback::getId, callback);
    }

    /**
     * Delete feedback
     */
    public void deleteFeedbackById(int feedbackId, DataCallback<Feedback> callback) {
        deleteById(feedbackId, Feedback::getId, callback);
    }

    /**
     * Search feedbacks by custom predicate
     */
    public void searchFeedbacks(java.util.function.Predicate<Feedback> predicate, DataCallback<Feedback> callback) {
        search(predicate, callback);
    }

    /**
     * Fetch feedbacks by email
     */
    public void fetchFeedbacksByEmail(String email, DataCallback<Feedback> callback) {
        search(feedback -> feedback.getEmail() != null &&
                feedback.getEmail().equalsIgnoreCase(email), callback);
    }

    /**
     * Fetch feedbacks by date
     */
    public void fetchFeedbacksByDate(String date, DataCallback<Feedback> callback) {
        search(feedback -> feedback.getDate() != null &&
                feedback.getDate().equals(date), callback);
    }

    /**
     * Fetch feedbacks by name (partial match)
     */
    public void fetchFeedbacksByName(String name, DataCallback<Feedback> callback) {
        search(feedback -> feedback.getName() != null &&
                feedback.getName().toLowerCase().contains(name.toLowerCase()), callback);
    }

    /**
     * Get feedback count
     */
    public void getFeedbackCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<Feedback>() {
            @Override
            public void onSuccess(List<Feedback> feedbacks) {
                callback.onCount(feedbacks.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get feedbacks by date range
     */
    public void getFeedbacksByDateRange(String startDate, String endDate, DataCallback<Feedback> callback) {
        search(feedback -> {
            if (feedback.getDate() == null) return false;
            String date = feedback.getDate();
            return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
        }, callback);
    }

    /**
     * Check if feedback exists by ID
     */
    public void feedbackExists(int feedbackId, ExistsCallback callback) {
        fetchByIdAsync(feedbackId, Feedback::getId, new SingleItemCallback<Feedback>() {
            @Override
            public void onSuccess(Feedback feedback) {
                callback.onResult(true);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Get recent feedbacks (last N items)
     */
    public void getRecentFeedbacks(int limit, DataCallback<Feedback> callback) {
        fetchAllAsync(new DataCallback<Feedback>() {
            @Override
            public void onSuccess(List<Feedback> feedbacks) {
                // Sort by date (descending) and take top N
                feedbacks.sort((f1, f2) -> {
                    if (f1.getDate() == null) return 1;
                    if (f2.getDate() == null) return -1;
                    return f2.getDate().compareTo(f1.getDate());
                });

                List<Feedback> recent = feedbacks.subList(0, Math.min(limit, feedbacks.size()));
                callback.onSuccess(recent);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
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
