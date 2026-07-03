package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.musicapp.core.models.Feedback;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.FeedbackService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated FeedbackViewModel with full feature coverage from FeedbackService
 * Includes LiveData for specialized queries and proper async handling
 */
public class FeedbackViewModel extends BaseViewModel<Feedback> {

    private final FeedbackService feedbackService;

    // ✅ LiveData for specialized queries
    private final MutableLiveData<List<Feedback>> feedbacksByEmailLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Feedback>> feedbacksByDateLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Feedback>> feedbacksByNameLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Feedback>> feedbacksByDateRangeLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Feedback>> recentFeedbacksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> feedbackCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> feedbackExistsLiveData = new MutableLiveData<>(false);

    public FeedbackViewModel() {
        super(FeedbackService.getInstance()); // ✅ Use singleton instead of new
        this.feedbackService = (FeedbackService) service;
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Expose LiveData of all feedbacks */
    public LiveData<List<Feedback>> getFeedbacks() {
        return getItems();
    }

    /** Add feedback */
    public void addFeedback(Feedback feedback) {
        addItem(feedback);
    }

    /** Update feedback */
    public void updateFeedback(int feedbackId, Feedback updatedFeedback) {
        updateItem(feedbackId, updatedFeedback, Feedback::getId);
    }

    /** Delete feedback */
    public void deleteFeedback(int feedbackId) {
        deleteItem(feedbackId, Feedback::getId);
    }

    /** Search feedbacks by predicate */
    public void searchFeedbacks(Predicate<Feedback> predicate) {
        search(predicate);
    }

    /** Refresh feedbacks */
    public void refreshFeedbacks() {
        refresh();
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ SPECIALIZED QUERY METHODS WITH LIVEDATA (New from FeedbackService)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetch feedbacks by email (LiveData version)
     * @return LiveData that updates when search completes
     */
    public LiveData<List<Feedback>> getFeedbacksByEmail(String email) {
        feedbackService.fetchFeedbacksByEmail(email, new BaseService.DataCallback<Feedback>() {
            @Override
            public void onSuccess(List<Feedback> feedbacks) {
                feedbacksByEmailLiveData.postValue(feedbacks);
            }

            @Override
            public void onError(String error) {
                feedbacksByEmailLiveData.postValue(new ArrayList<>());
            }
        });
        return feedbacksByEmailLiveData;
    }

    /**
     * Fetch feedbacks by email (callback version)
     */
    public void getFeedbacksByEmail(String email, BaseService.DataCallback<Feedback> callback) {
        feedbackService.fetchFeedbacksByEmail(email, callback);
    }

    /**
     * Fetch feedbacks by date (LiveData version)
     * @return LiveData that updates when search completes
     */
    public LiveData<List<Feedback>> getFeedbacksByDate(String date) {
        feedbackService.fetchFeedbacksByDate(date, new BaseService.DataCallback<Feedback>() {
            @Override
            public void onSuccess(List<Feedback> feedbacks) {
                feedbacksByDateLiveData.postValue(feedbacks);
            }

            @Override
            public void onError(String error) {
                feedbacksByDateLiveData.postValue(new ArrayList<>());
            }
        });
        return feedbacksByDateLiveData;
    }

    /**
     * Fetch feedbacks by date (callback version)
     */
    public void getFeedbacksByDate(String date, BaseService.DataCallback<Feedback> callback) {
        feedbackService.fetchFeedbacksByDate(date, callback);
    }

    /**
     * Fetch feedbacks by name (partial match) (LiveData version)
     * @return LiveData that updates when search completes
     */
    public LiveData<List<Feedback>> getFeedbacksByName(String name) {
        feedbackService.fetchFeedbacksByName(name, new BaseService.DataCallback<Feedback>() {
            @Override
            public void onSuccess(List<Feedback> feedbacks) {
                feedbacksByNameLiveData.postValue(feedbacks);
            }

            @Override
            public void onError(String error) {
                feedbacksByNameLiveData.postValue(new ArrayList<>());
            }
        });
        return feedbacksByNameLiveData;
    }

    /**
     * Fetch feedbacks by name (callback version)
     */
    public void getFeedbacksByName(String name, BaseService.DataCallback<Feedback> callback) {
        feedbackService.fetchFeedbacksByName(name, callback);
    }

    /**
     * Get feedbacks by date range (LiveData version)
     * @return LiveData that updates when search completes
     */
    public LiveData<List<Feedback>> getFeedbacksByDateRange(String startDate, String endDate) {
        feedbackService.getFeedbacksByDateRange(startDate, endDate, new BaseService.DataCallback<Feedback>() {
            @Override
            public void onSuccess(List<Feedback> feedbacks) {
                feedbacksByDateRangeLiveData.postValue(feedbacks);
            }

            @Override
            public void onError(String error) {
                feedbacksByDateRangeLiveData.postValue(new ArrayList<>());
            }
        });
        return feedbacksByDateRangeLiveData;
    }

    /**
     * Get feedbacks by date range (callback version)
     */
    public void getFeedbacksByDateRange(String startDate, String endDate, BaseService.DataCallback<Feedback> callback) {
        feedbackService.getFeedbacksByDateRange(startDate, endDate, callback);
    }

    /**
     * Get recent feedbacks (last N items) (LiveData version)
     * @return LiveData of recent feedbacks
     */
    public LiveData<List<Feedback>> getRecentFeedbacks(int limit) {
        feedbackService.getRecentFeedbacks(limit, new BaseService.DataCallback<Feedback>() {
            @Override
            public void onSuccess(List<Feedback> feedbacks) {
                recentFeedbacksLiveData.postValue(feedbacks);
            }

            @Override
            public void onError(String error) {
                recentFeedbacksLiveData.postValue(new ArrayList<>());
            }
        });
        return recentFeedbacksLiveData;
    }

    /**
     * Get recent feedbacks (callback version)
     */
    public void getRecentFeedbacks(int limit, BaseService.DataCallback<Feedback> callback) {
        feedbackService.getRecentFeedbacks(limit, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ COUNT & VALIDATION OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get feedback count (LiveData version)
     * @return LiveData of total feedback count
     */
    public LiveData<Integer> getFeedbackCount() {
        feedbackService.getFeedbackCount(count -> {
            feedbackCountLiveData.postValue(count);
        });
        return feedbackCountLiveData;
    }

    /**
     * Get feedback count (callback version)
     */
    public void getFeedbackCount(FeedbackService.CountCallback callback) {
        feedbackService.getFeedbackCount(callback);
    }

    /**
     * Check if feedback exists by ID (LiveData version)
     * @return LiveData of existence status
     */
    public LiveData<Boolean> feedbackExists(int feedbackId) {
        feedbackService.feedbackExists(feedbackId, exists -> {
            feedbackExistsLiveData.postValue(exists);
        });
        return feedbackExistsLiveData;
    }

    /**
     * Check if feedback exists by ID (callback version)
     */
    public void feedbackExists(int feedbackId, FeedbackService.ExistsCallback callback) {
        feedbackService.feedbackExists(feedbackId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ ADDITIONAL HELPER METHODS WITH CALLBACKS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetch a single feedback by ID (async with callback)
     */
    public void fetchFeedbackById(int feedbackId, BaseService.SingleItemCallback<Feedback> callback) {
        feedbackService.fetchFeedbackById(feedbackId, callback);
    }

    /**
     * Add feedback with callback (recommended for error handling)
     */
    public void addFeedbackWithCallback(Feedback feedback, BaseService.DataCallback<Feedback> callback) {
        feedbackService.addFeedback(feedback, callback);
    }

    /**
     * Update feedback with callback (recommended for error handling)
     */
    public void updateFeedbackWithCallback(int feedbackId, Feedback updatedFeedback, BaseService.DataCallback<Feedback> callback) {
        feedbackService.updateFeedback(feedbackId, updatedFeedback, callback);
    }

    /**
     * Delete feedback with callback (recommended for error handling)
     */
    public void deleteFeedbackWithCallback(int feedbackId, BaseService.DataCallback<Feedback> callback) {
        feedbackService.deleteFeedbackById(feedbackId, callback);
    }

    /**
     * Search feedbacks with callback
     */
    public void searchFeedbacksWithCallback(Predicate<Feedback> predicate, BaseService.DataCallback<Feedback> callback) {
        feedbackService.searchFeedbacks(predicate, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ DEPRECATED IN-MEMORY SEARCH METHODS (Kept for backward compatibility)
    // ═══════════════════════════════════════════════════════════════

    /**
     * @deprecated Use getFeedbacksByName(String) instead for Firebase async search
     * Fetch feedbacks by name (in-memory filtering)
     */
    @Deprecated
    public void searchByName(String name) {
        search(feedback -> feedback.getName() != null &&
                feedback.getName().toLowerCase().contains(name.toLowerCase()));
    }

    /**
     * @deprecated Use getFeedbacksByEmail(String) instead for Firebase async search
     * Fetch feedbacks by email (in-memory filtering)
     */
    @Deprecated
    public void searchByEmail(String email) {
        search(feedback -> feedback.getEmail() != null &&
                feedback.getEmail().equalsIgnoreCase(email));
    }

    /**
     * @deprecated Use getFeedbacksByDate(String) instead for Firebase async search
     * Fetch feedbacks by date (in-memory filtering)
     */
    @Deprecated
    public void searchByDate(String date) {
        search(feedback -> feedback.getDate() != null &&
                feedback.getDate().equals(date));
    }
}
