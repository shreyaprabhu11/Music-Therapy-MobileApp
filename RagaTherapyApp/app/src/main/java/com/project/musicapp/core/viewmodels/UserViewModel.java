package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;

import com.project.musicapp.core.models.User;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.UserService;

import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated UserViewModel with proper async handling
 * Follows singleton pattern and uses callbacks for all Firebase operations
 */
public class UserViewModel extends BaseViewModel<User> {

    private final UserService userService;

    public UserViewModel() {
        super(UserService.getInstance()); // ✅ Use singleton instead of new
        this.userService = UserService.getInstance();
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Get LiveData of all users (auto updates when Firebase changes) */
    public LiveData<List<User>> getAllUsers() {
        return getItems();
    }

    /** Add new user */
    public void addUser(User user) {
        addItem(user);
    }

    /** Update existing user */
    public void updateUser(int id, User updatedUser) {
        updateItem(id, updatedUser, User::getId);
    }

    /** Delete user by ID */
    public void deleteUser(int id) {
        deleteItem(id, User::getId);
    }

    /** Search users locally (filtered from LiveData list) */
    public void searchUsers(Predicate<User> predicate) {
        search(predicate);
    }

    /** Force manual Firebase refresh */
    public void refreshUsers() {
        refresh();
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ SPECIALIZED QUERY METHODS (Async with Callbacks)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetch single user by ID (async with callback)
     */
    public void fetchUserById(int id, BaseService.SingleItemCallback<User> callback) {
        userService.fetchUserByIdAsync(id, callback);
    }

    /**
     * Find user by email
     */
    public void findUserByEmail(String email, BaseService.SingleItemCallback<User> callback) {
        userService.findUserByEmail(email, callback);
    }

    /**
     * Find user by phone number
     */
    public void findUserByPhone(String phone, BaseService.SingleItemCallback<User> callback) {
        userService.findUserByPhone(phone, callback);
    }

    /**
     * Validate login credentials
     */
    public void validateLogin(String email, String password, String role, UserService.LoginCallback callback) {
        userService.validateLogin(email, password, (user, error) -> {
            if (user != null && error == null) {
                if (user.getRole().name().equalsIgnoreCase(role)) {
                    callback.onLoginResult(user, null);
                } else {
                    callback.onLoginResult(null, "Incorrect role selected");
                }
            } else {
                callback.onLoginResult(null, error != null ? error : "Invalid credentials");
            }
        });
    }


    /**
     * Get users by role
     */
    public void getUsersByRole(User.Role role, BaseService.DataCallback<User> callback) {
        userService.getUsersByRole(role, callback);
    }

    /**
     * Search users by name (partial match)
     */
    public void searchUsersByName(String name, BaseService.DataCallback<User> callback) {
        userService.searchUsersByName(name, callback);
    }

    /**
     * Get all first-time login users
     */
    public void fetchFirstTimeUsers(BaseService.DataCallback<User> callback) {
        userService.fetchFirstTimeUsers(callback);
    }

    /**
     * Get all admins
     */
    public void getAllAdmins(BaseService.DataCallback<User> callback) {
        userService.getAllAdmins(callback);
    }

    /**
     * Get all consultants
     */
    public void getAllConsultants(BaseService.DataCallback<User> callback) {
        userService.getAllConsultants(callback);
    }

    /**
     * Get all patients
     */
    public void getAllPatients(BaseService.DataCallback<User> callback) {
        userService.getAllPatients(callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ USER UPDATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Mark user's first login status
     */
    public void markFirstLogin(int userId, boolean status, BaseService.DataCallback<User> callback) {
        userService.markFirstLogin(userId, status);
    }

    /**
     * Update user password
     */
    public void updatePassword(int userId, String newPassword, BaseService.DataCallback<User> callback) {
        userService.updatePassword(userId, newPassword, callback);
    }

    public void setFirstLogin(int userId, boolean status) {
        java.util.concurrent.CompletableFuture.runAsync(() -> userService.markFirstLogin(userId, status));
    }

    /**
     * Update user profile picture
     */
    public void updateProfilePicture(int userId, String pictureUrl, BaseService.DataCallback<User> callback) {
        userService.updateProfilePicture(userId, pictureUrl, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ CHECK & COUNT OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get total user count
     */
    public void getUserCount(UserService.CountCallback callback) {
        userService.getUserCount(callback);
    }

    /**
     * Get user count by role
     */
    public void getUserCountByRole(User.Role role, UserService.CountCallback callback) {
        userService.getUserCountByRole(role, callback);
    }

    /**
     * Check if user exists by email
     */
    public void userExistsByEmail(String email, UserService.ExistsCallback callback) {
        userService.userExistsByEmail(email, callback);
    }

    /**
     * Check if user exists by ID
     */
    public void userExistsById(int userId, UserService.ExistsCallback callback) {
        userService.userExistsById(userId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ BULK OPERATIONS WITH CALLBACKS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Add user with callback (recommended for error handling)
     */
    public void addUserWithCallback(User user, BaseService.DataCallback<User> callback) {
        userService.addUser(user, callback);
    }

    /**
     * Update user with callback
     */
    public void updateUserWithCallback(int id, User updatedUser, BaseService.DataCallback<User> callback) {
        userService.updateUser2(id, updatedUser, callback);
    }

    /**
     * Delete user with callback
     */
    public void deleteUserWithCallback(int id, BaseService.DataCallback<User> callback) {
        userService.deleteUser(id, callback);
    }

    /**
     * Delete users by role
     */
    public void deleteUsersByRole(User.Role role, BaseService.DataCallback<User> callback) {
        userService.deleteUsersByRole(role, callback);
    }

    /**
     * Search users with callback
     */
    public void searchUsersWithCallback(Predicate<User> predicate, BaseService.DataCallback<User> callback) {
        userService.searchUsers(predicate, callback);
    }
}
