package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;

import com.project.musicapp.core.models.Notification;
import com.project.musicapp.core.models.User;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.NotificationService;

import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated NotificationListViewModel with proper async handling
 * Follows singleton pattern and uses callbacks for all Firebase operations
 */
public class NotificationListViewModel extends BaseViewModel<Notification> {

    private final NotificationService notificationService;

    public NotificationListViewModel() {
        super(NotificationService.getInstance()); // ✅ Use singleton instead of new
        this.notificationService = NotificationService.getInstance();
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Get LiveData of all notifications (auto updates when Firebase changes) */
    public LiveData<List<Notification>> getAllNotifications() {
        return getItems();
    }

    public void getNotificationByIdAsync(int id, NotificationService.Callback<Notification> callback) {
        notificationService.fetchNotificationByIdAsync(id, callback);
    }

    /** Add new notification */
    public void addNotification(Notification notification) {
        addItem(notification);
    }

    public void markAsReadAsync(int id) {
        getNotificationByIdAsync(id, notification -> {
            if (notification != null && !notification.isRead()) {
                notification.setRead(true);
                notificationService.updateNotification(id, notification);
                refreshNotifications();
            }
        });
    }

    /** Update existing notification */
    public void updateNotification(int id, Notification updatedNotification) {
        updateItem(id, updatedNotification, Notification::getId);
    }

    /** Delete notification by ID */
    public void deleteNotification(int id) {
        deleteItem(id, Notification::getId);
    }

    /** Search notifications locally (filtered from LiveData list) */
    public void searchNotifications(Predicate<Notification> predicate) {
        search(predicate);
    }

    /** Force manual Firebase refresh */
    public void refreshNotifications() {
        refresh();
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ SPECIALIZED QUERY METHODS (Async with Callbacks)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetch single notification by ID (async with callback)
     */
    public void fetchNotificationById(int id, BaseService.SingleItemCallback<Notification> callback) {
        notificationService.fetchNotificationById(id, callback);
    }

    /**
     * Get all unread notifications
     */
    public void getUnreadNotifications(BaseService.DataCallback<Notification> callback) {
        notificationService.getUnreadNotifications(callback);
    }

    /**
     * Get notifications for a specific user
     */
    public void getNotificationsByUser(User user, BaseService.DataCallback<Notification> callback) {
        notificationService.getNotificationsByUser(user, callback);
    }


    /**
     * Get notifications by user ID
     */
    public void getNotificationsByUserId(int userId, BaseService.DataCallback<Notification> callback) {
        notificationService.getNotificationsByUserId(userId, callback);
    }

    /**
     * Get unread notifications for a specific user
     */
    public void getUnreadNotificationsByUser(User user, BaseService.DataCallback<Notification> callback) {
        notificationService.getUnreadNotificationsByUser(user, callback);
    }

    /**
     * Get read notifications for a specific user
     */
    public void getReadNotificationsByUser(User user, BaseService.DataCallback<Notification> callback) {
        notificationService.getReadNotificationsByUser(user, callback);
    }

    /**
     * Get notifications with appointments
     */
    public void getNotificationsWithAppointments(BaseService.DataCallback<Notification> callback) {
        notificationService.getNotificationsWithAppointments(callback);
    }

    /**
     * Get notifications by time range (timestamps in milliseconds)
     */
    public void getNotificationsByTimeRange(long startTime, long endTime,
                                            BaseService.DataCallback<Notification> callback) {
        notificationService.getNotificationsByTimeRange(startTime, endTime, callback);
    }

    /**
     * Get recent notifications (last N items)
     */
    public void getRecentNotifications(int limit, BaseService.DataCallback<Notification> callback) {
        notificationService.getRecentNotifications(limit, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ NOTIFICATION ACTIONS (Mark as Read, Delete, etc.)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Mark notification as read
     */
    public void markAsReadAsync(int id, BaseService.DataCallback<Notification> callback) {
        notificationService.markAsReadAsync(id, callback);
    }

    public void markAsRead(int id) {
        if (notificationService.markAsRead(id)) {
            refreshNotifications();
        }
    }

    /**
     * Mark multiple notifications as read
     */
    public void markMultipleAsRead(List<Integer> ids, BaseService.DataCallback<Notification> callback) {
        notificationService.markMultipleAsRead(ids, callback);
    }

    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsReadForUser(User user, BaseService.DataCallback<Notification> callback) {
        notificationService.markAllAsReadForUser(user, callback);
    }

    /**
     * Delete all notifications for a user
     */
    public void deleteNotificationsByUser(User user, BaseService.DataCallback<Notification> callback) {
        notificationService.deleteNotificationsByUser(user, callback);
    }

    /**
     * Delete all read notifications for a user
     */
    public void deleteReadNotificationsByUser(User user, BaseService.DataCallback<Notification> callback) {
        notificationService.deleteReadNotificationsByUser(user, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ COUNT OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get total notification count
     */
    public void getNotificationCount(NotificationService.CountCallback callback) {
        notificationService.getNotificationCount(callback);
    }

    /**
     * Get unread notification count for user
     */
    public void getUnreadCountForUser(User user, NotificationService.CountCallback callback) {
        notificationService.getUnreadCountForUser(user, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ ADDITIONAL HELPER METHODS WITH CALLBACKS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Add notification with callback (recommended for error handling)
     */
    public void addNotificationWithCallback(Notification notification,
                                            BaseService.DataCallback<Notification> callback) {
        notificationService.addNotification(notification, callback);
    }

    /**
     * Update notification with callback
     */
    public void updateNotificationWithCallback(int id, Notification updatedNotification,
                                               BaseService.DataCallback<Notification> callback) {
        notificationService.updateNotification(id, updatedNotification, callback);
    }

    /**
     * Delete notification with callback
     */
    public void deleteNotificationWithCallback(int id, BaseService.DataCallback<Notification> callback) {
        notificationService.deleteNotification(id, callback);
    }

    /**
     * Search notifications with callback
     */
    public void searchNotificationsWithCallback(Predicate<Notification> predicate,
                                                BaseService.DataCallback<Notification> callback) {
        notificationService.searchNotifications(predicate, callback);
    }
}
