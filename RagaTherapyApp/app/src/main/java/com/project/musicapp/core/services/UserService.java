package com.project.musicapp.core.services;

import com.project.musicapp.core.models.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Firebase-integrated UserService extending BaseService.
 * All operations use async callbacks for Firebase integration.
 */
public class UserService extends BaseService<User> {
    private static UserService instance;

    private UserService() {
        super(User.class, "users"); // Firebase node = "users"
    }

    // ✅ SINGLETON PATTERN
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Fetch all users
     */
    public void fetchAllUsers(DataCallback<User> callback) {
        fetchAllAsync(callback);
    }

    /**
     * Fetch a user by ID
     */
    public void fetchUserByIdAsync(int id, SingleItemCallback<User> callback) {
        fetchByIdAsync(id, User::getId, callback);
    }

    public User fetchUserById(int id) {
        return fetchById(id, User::getId);
    }

    /**
     * Add a new user
     */
    public void addUser(User user, DataCallback<User> callback) {
        if (user == null) {
            callback.onError("User cannot be null");
            return;
        }

        // Check if email already exists
        if (user.getEmail() != null) {
            findUserByEmail(user.getEmail(), new SingleItemCallback<User>() {
                @Override
                public void onSuccess(User existingUser) {
                    callback.onError("User with email '" + user.getEmail() + "' already exists");
                }

                @Override
                public void onError(String error) {
                    // Email doesn't exist - safe to add
                    addItem(user, callback);
                }
            });
        } else {
            addItem(user, callback);
        }
    }

    /**
     * Update existing user details
     */
    public void updateUser2(int id, User updatedUser, DataCallback<User> callback) {
        updateItem(id, updatedUser, User::getId, callback);
    }

    public boolean updateUser(int id, User updatedUser) {
        return updateItem(id, updatedUser, User::getId);
    }

    /**
     * Delete a user by ID
     */
    public void deleteUser(int id, DataCallback<User> callback) {
        deleteById(id, User::getId, callback);
    }

    /**
     * Find a user by email
     */
    public void findUserByEmail(String email, SingleItemCallback<User> callback) {
        if (email == null || email.trim().isEmpty()) {
            callback.onError("Email cannot be null or empty");
            return;
        }

        search(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email), new DataCallback<User>() {
            @Override
            public void onSuccess(List<User> users) {
                if (!users.isEmpty()) {
                    callback.onSuccess(users.get(0));
                } else {
                    callback.onError("User not found with email: " + email);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Validate user credentials for login
     */
    public void validateLogin(String email, String password, LoginCallback callback) {
        if (email == null || email.trim().isEmpty()) {
            callback.onLoginResult(null, "Email cannot be empty");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            callback.onLoginResult(null, "Password cannot be empty");
            return;
        }

        search(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email) &&
                u.getPassword() != null && u.getPassword().equals(password), new DataCallback<User>() {
            @Override
            public void onSuccess(List<User> users) {
                if (!users.isEmpty()) {
                    callback.onLoginResult(users.get(0), null);
                } else {
                    callback.onLoginResult(null, "Invalid email or password");
                }
            }

            @Override
            public void onError(String error) {
                callback.onLoginResult(null, "Login failed: " + error);
            }
        });
    }

    /**
     * Generic user search using predicate
     */
    public void searchUsers(java.util.function.Predicate<User> predicate, DataCallback<User> callback) {
        search(predicate, callback);
    }

    /**
     * Mark a user's first login status
     */
//    public void markFirstLoginNew(int userId, boolean status, DataCallback<User> callback) {
//        fetchUserById(userId, new SingleItemCallback<User>() {
//            @Override
//            public void onSuccess(User user) {
//                user.setFirstLogin(status);
//                updateUser(userId, user, callback);
//            }
//
//            @Override
//            public void onError(String error) {
//                callback.onError("User not found: " + error);
//            }
//        });
//    }

    public boolean markFirstLogin(int userId, boolean status) {
        User user = fetchUserById(userId);
        if (user != null) {
            user.setFirstLogin(status);
            return updateUser(userId, user);
        }
        return false;
    }

    /**
     * Fetch all users who are first-time logins
     */
    public void fetchFirstTimeUsers(DataCallback<User> callback) {
        search(User::isFirstLogin, callback);
    }

    /**
     * Get users by role
     */
    public void getUsersByRole(User.Role role, DataCallback<User> callback) {
        if (role == null) {
            callback.onError("Role cannot be null");
            return;
        }
        search(u -> u.getRole() == role, callback);
    }

    /**
     * Search users by name (partial match)
     */
    public void searchUsersByName(String name, DataCallback<User> callback) {
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Name cannot be null or empty");
            return;
        }

        String lowerName = name.toLowerCase();
        search(u -> u.getName() != null && u.getName().toLowerCase().contains(lowerName), callback);
    }

    /**
     * Search users by phone
     */
    public void findUserByPhone(String phone, SingleItemCallback<User> callback) {
        if (phone == null || phone.trim().isEmpty()) {
            callback.onError("Phone cannot be null or empty");
            return;
        }

        search(u -> u.getPhone() != null && u.getPhone().equals(phone), new DataCallback<User>() {
            @Override
            public void onSuccess(List<User> users) {
                if (!users.isEmpty()) {
                    callback.onSuccess(users.get(0));
                } else {
                    callback.onError("User not found with phone: " + phone);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }


    /**
     * Update user password
     */
    public void updatePassword(int userId, String newPassword, DataCallback<User> callback) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            callback.onError("Password cannot be empty");
            return;
        }

        fetchUserByIdAsync(userId, new SingleItemCallback<User>() {
            @Override
            public void onSuccess(User user) {
                user.setPassword(newPassword);
                updateUser(userId, user);
            }

            @Override
            public void onError(String error) {
                callback.onError("User not found: " + error);
            }
        });
    }

    /**
     * Update user profile picture
     */
    public void updateProfilePicture(int userId, String pictureUrl, DataCallback<User> callback) {
        fetchUserByIdAsync(userId, new SingleItemCallback<User>() {
            @Override
            public void onSuccess(User user) {
                user.setProfilePictureUrl(pictureUrl);
                updateUser(userId, user);
            }

            @Override
            public void onError(String error) {
                callback.onError("User not found: " + error);
            }
        });
    }

    /**
     * Get user count
     */
    public void getUserCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<User>() {
            @Override
            public void onSuccess(List<User> users) {
                callback.onCount(users.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get user count by role
     */
    public void getUserCountByRole(User.Role role, CountCallback callback) {
        getUsersByRole(role, new DataCallback<User>() {
            @Override
            public void onSuccess(List<User> users) {
                callback.onCount(users.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Check if user exists by email
     */
    public void userExistsByEmail(String email, ExistsCallback callback) {
        if (email == null || email.trim().isEmpty()) {
            callback.onResult(false);
            return;
        }

        findUserByEmail(email, new SingleItemCallback<User>() {
            @Override
            public void onSuccess(User user) {
                callback.onResult(true);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Check if user exists by ID
     */
    public void userExistsById(int userId, ExistsCallback callback) {
        fetchByIdAsync(userId, User::getId, new SingleItemCallback<User>() {
            @Override
            public void onSuccess(User user) {
                callback.onResult(true);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Get all admins
     */
    public void getAllAdmins(DataCallback<User> callback) {
        getUsersByRole(User.Role.ADMIN, callback);
    }

    /**
     * Get all consultants
     */
    public void getAllConsultants(DataCallback<User> callback) {
        getUsersByRole(User.Role.CONSULTANT, callback);
    }

    /**
     * Get all patients
     */
    public void getAllPatients(DataCallback<User> callback) {
        getUsersByRole(User.Role.PATIENT, callback);
    }

    /**
     * Delete users by role
     */
    public void deleteUsersByRole(User.Role role, DataCallback<User> callback) {
        if (role == null) {
            callback.onError("Role cannot be null");
            return;
        }

        getUsersByRole(role, new DataCallback<User>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users.isEmpty()) {
                    callback.onSuccess(users);
                    return;
                }

                int[] deletedCount = {0};
                for (User user : users) {
                    deleteUser(user.getId(), new DataCallback<User>() {
                        @Override
                        public void onSuccess(List<User> deletedUsers) {
                            deletedCount[0]++;
                            if (deletedCount[0] == users.size()) {
                                callback.onSuccess(users);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to delete user: " + error);
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
    public interface LoginCallback {
        void onLoginResult(User user, String error);
    }

    public interface CountCallback {
        void onCount(int count);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
    }
}
