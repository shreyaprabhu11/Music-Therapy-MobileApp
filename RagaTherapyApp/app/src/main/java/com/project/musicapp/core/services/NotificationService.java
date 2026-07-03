package com.project.musicapp.core.services;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.musicapp.core.models.Notification;
import com.project.musicapp.core.models.User;
import java.util.List;

/**
 * Firebase-integrated NotificationService
 * Provides CRUD + search + domain-specific helpers with async callbacks.
 */
public class NotificationService extends BaseService<Notification> {
    private static NotificationService instance;
    private final DatabaseReference ref;

    public NotificationService() {
        super(Notification.class, "notifications");
        ref = FirebaseDatabase.getInstance().getReference("notifications");
    }

    // ✅ SINGLETON PATTERN
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void fetchNotificationByIdAsync(int id, Callback<Notification> callback) {
        ref.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Notification result = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Notification n = child.getValue(Notification.class);
                    if (n != null) {
                        result = n;
                        break;
                    }
                }
                callback.onResult(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(null);
            }
        });
    }

    public boolean updateNotification(int id, Notification updatedNotification) {
        return updateItem(id, updatedNotification, Notification::getId);
    }

    public boolean addNotification(Notification notification) {
        addItem(notification);
        return true;
    }

    /**
     * Fetch all notifications
     */
    public void fetchAllNotifications(DataCallback<Notification> callback) {
        fetchAllAsync(callback);
    }

    public List<Notification> fetchAllNotifications() {
        return fetchAll();
    }

    public Notification fetchNotificationById(int id) {
        List<Notification> all = fetchAllNotifications();
        for (Notification n : all) {
            if (n.getId() == id) return n;
        }
        return null;
    }

    /**
     * Fetch notification by ID
     */
    public void fetchNotificationById(int id, SingleItemCallback<Notification> callback) {
        fetchByIdAsync(id, Notification::getId, callback);
    }

    /**
     * Add new notification
     */
    public void addNotification(Notification notification, DataCallback<Notification> callback) {
        addItem(notification, callback);
    }

    /**
     * Update notification
     */
    public void updateNotification(int id, Notification updatedNotification, DataCallback<Notification> callback) {
        updateItem(id, updatedNotification, Notification::getId, callback);
    }

    /**
     * Delete notification
     */
    public void deleteNotification(int id, DataCallback<Notification> callback) {
        deleteById(id, Notification::getId, callback);
    }

    /**
     * Search notifications based on predicate
     */
    public void searchNotifications(java.util.function.Predicate<Notification> predicate, DataCallback<Notification> callback) {
        search(predicate, callback);
    }

    /**
     * Get all unread notifications
     */
    public void getUnreadNotifications(DataCallback<Notification> callback) {
        search(n -> !n.isRead(), callback);
    }

    /**
     * Get notifications for a specific user
     */
    public void getNotificationsByUser(User user, DataCallback<Notification> callback) {
        if (user == null) {
            callback.onError("User cannot be null");
            return;
        }
        search(n -> n.getReceiver() != null &&
                n.getReceiver().getId() == user.getId(), callback);
    }

    /**
     * Get notifications by user ID
     */
    public void getNotificationsByUserId(int userId, DataCallback<Notification> callback) {
        search(n -> n.getReceiver() != null &&
                n.getReceiver().getId() == userId, callback);
    }

    /**
     * Mark notification as read (ASYNC)
     */
    public void markAsReadAsync(int id, DataCallback<Notification> callback) {
        fetchNotificationById(id, new SingleItemCallback<Notification>() {
            @Override
            public void onSuccess(Notification notification) {
                if (!notification.isRead()) {
                    notification.setRead(true);
                    updateNotification(id, notification, callback);
                } else {
                    // Already read - still return success
                    callback.onSuccess(List.of(notification));
                }
            }

            @Override
            public void onError(String error) {
                callback.onError("Notification not found: " + error);
            }
        });
    }

    public boolean markAsRead(int id) {
        Notification notification = fetchNotificationById(id);
        if (notification != null && !notification.isRead()) {
            notification.setRead(true);
            updateItem(id, notification, Notification::getId);
            return true;
        }
        return false;
    }

    /**
     * Mark multiple notifications as read
     */
    public void markMultipleAsRead(List<Integer> ids, DataCallback<Notification> callback) {
        if (ids == null || ids.isEmpty()) {
            callback.onError("No notification IDs provided");
            return;
        }

        int[] updatedCount = {0};
        for (int id : ids) {
            markAsReadAsync(id, new DataCallback<Notification>() {
                @Override
                public void onSuccess(List<Notification> notifications) {
                    updatedCount[0]++;
                    if (updatedCount[0] == ids.size()) {
                        callback.onSuccess(notifications);
                    }
                }

                @Override
                public void onError(String error) {
                    // Continue even if one fails
                    updatedCount[0]++;
                    if (updatedCount[0] == ids.size()) {
                        callback.onError("Some notifications failed to update");
                    }
                }
            });
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsReadForUser(User user, DataCallback<Notification> callback) {
        if (user == null) {
            callback.onError("User cannot be null");
            return;
        }

        getUnreadNotificationsByUser(user, new DataCallback<Notification>() {
            @Override
            public void onSuccess(List<Notification> unreadNotifications) {
                if (unreadNotifications.isEmpty()) {
                    callback.onSuccess(unreadNotifications);
                    return;
                }

                int[] updatedCount = {0};
                for (Notification notification : unreadNotifications) {
                    notification.setRead(true);
                    updateNotification(notification.getId(), notification, new DataCallback<Notification>() {
                        @Override
                        public void onSuccess(List<Notification> notifications) {
                            updatedCount[0]++;
                            if (updatedCount[0] == unreadNotifications.size()) {
                                callback.onSuccess(unreadNotifications);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to mark all as read: " + error);
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
     * Get notifications with appointments
     */
    public void getNotificationsWithAppointments(DataCallback<Notification> callback) {
        search(Notification::hasAppointment, callback);
    }

    /**
     * Get unread notifications for a specific user
     */
    public void getUnreadNotificationsByUser(User user, DataCallback<Notification> callback) {
        if (user == null) {
            callback.onError("User cannot be null");
            return;
        }

        search(n -> n.getReceiver() != null &&
                n.getReceiver().getId() == user.getId() &&
                !n.isRead(), callback);
    }

    /**
     * Get read notifications for a specific user
     */
    public void getReadNotificationsByUser(User user, DataCallback<Notification> callback) {
        if (user == null) {
            callback.onError("User cannot be null");
            return;
        }

        search(n -> n.getReceiver() != null &&
                n.getReceiver().getId() == user.getId() &&
                n.isRead(), callback);
    }

    /**
     * Get notification count
     */
    public void getNotificationCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<Notification>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                callback.onCount(notifications.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get unread notification count for user
     */
    public void getUnreadCountForUser(User user, CountCallback callback) {
        if (user == null) {
            callback.onCount(0);
            return;
        }

        getUnreadNotificationsByUser(user, new DataCallback<Notification>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                callback.onCount(notifications.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get notifications by time range (timestamps in milliseconds)
     */
    public void getNotificationsByTimeRange(long startTime, long endTime, DataCallback<Notification> callback) {
        if (startTime <= 0 || endTime <= 0) {
            callback.onError("Invalid time range");
            return;
        }

        search(n -> n.getSentTime() >= startTime &&
                n.getSentTime() <= endTime, callback);
    }

    /**
     * Get recent notifications (last N items)
     */
    public void getRecentNotifications(int limit, DataCallback<Notification> callback) {
        fetchAllAsync(new DataCallback<Notification>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                // Sort by sentTime (descending) and take top N
                notifications.sort((n1, n2) -> Long.compare(n2.getSentTime(), n1.getSentTime()));

                List<Notification> recent = notifications.subList(0, Math.min(limit, notifications.size()));
                callback.onSuccess(recent);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Delete all notifications for a user
     */
    public void deleteNotificationsByUser(User user, DataCallback<Notification> callback) {
        if (user == null) {
            callback.onError("User cannot be null");
            return;
        }

        getNotificationsByUser(user, new DataCallback<Notification>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (notifications.isEmpty()) {
                    callback.onSuccess(notifications);
                    return;
                }

                // Delete each notification
                int[] deletedCount = {0};
                for (Notification notification : notifications) {
                    deleteNotification(notification.getId(), new DataCallback<Notification>() {
                        @Override
                        public void onSuccess(List<Notification> deletedNotifications) {
                            deletedCount[0]++;
                            if (deletedCount[0] == notifications.size()) {
                                callback.onSuccess(notifications);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to delete notification: " + error);
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
     * Delete all read notifications for a user
     */
    public void deleteReadNotificationsByUser(User user, DataCallback<Notification> callback) {
        if (user == null) {
            callback.onError("User cannot be null");
            return;
        }

        getReadNotificationsByUser(user, new DataCallback<Notification>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (notifications.isEmpty()) {
                    callback.onSuccess(notifications);
                    return;
                }

                int[] deletedCount = {0};
                for (Notification notification : notifications) {
                    deleteNotification(notification.getId(), new DataCallback<Notification>() {
                        @Override
                        public void onSuccess(List<Notification> deletedNotifications) {
                            deletedCount[0]++;
                            if (deletedCount[0] == notifications.size()) {
                                callback.onSuccess(notifications);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to delete notification: " + error);
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

    // ✅ CUSTOM CALLBACK INTERFACES
    public interface CountCallback {
        void onCount(int count);
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
