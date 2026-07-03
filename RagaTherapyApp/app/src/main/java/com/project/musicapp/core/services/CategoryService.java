package com.project.musicapp.core.services;

import com.project.musicapp.core.models.Category;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryService extending BaseService for full CRUD and utility operations.
 * Manages music categories for the application using Firebase Realtime Database.
 */
public class CategoryService extends BaseService<Category> {
    private static CategoryService instance;

    private CategoryService() {
        super(Category.class, "categories");
        preloadCategories();
    }

    // ✅ SINGLETON PATTERN
    public static synchronized CategoryService getInstance() {
        if (instance == null) {
            instance = new CategoryService();
        }
        return instance;
    }

    /**
     * Preload some default categories (only if database is empty)
     */
    private void preloadCategories() {
        fetchAllAsync(new DataCallback<Category>() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (categories.isEmpty()) {
                    // Add default categories
                    addItem(new Category(1, "Classical"), new DataCallback<Category>() {
                        @Override
                        public void onSuccess(List<Category> items) {}
                        @Override
                        public void onError(String error) {}
                    });
                    addItem(new Category(2, "Country"), new DataCallback<Category>() {
                        @Override
                        public void onSuccess(List<Category> items) {}
                        @Override
                        public void onError(String error) {}
                    });
                    addItem(new Category(3, "Dance"), new DataCallback<Category>() {
                        @Override
                        public void onSuccess(List<Category> items) {}
                        @Override
                        public void onError(String error) {}
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Handle error silently for preload
            }
        });
    }

    /**
     * Fetch all categories
     */
    public void fetchAllCategories(DataCallback<Category> callback) {
        fetchAllAsync(callback);
    }

    /**
     * Fetch category by ID
     */
    public void fetchCategoryById(int categoryId, SingleItemCallback<Category> callback) {
        fetchByIdAsync(categoryId, Category::getId, callback);
    }

    /**
     * Fetch category by name
     */
    public void fetchCategoryByName(String name, SingleItemCallback<Category> callback) {
        search(category -> category.getName().equalsIgnoreCase(name), new DataCallback<Category>() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (!categories.isEmpty()) {
                    callback.onSuccess(categories.get(0));
                } else {
                    callback.onError("Category not found with name: " + name);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Add new category (checks for duplicate name first)
     */
    public void addCategory(Category category, DataCallback<Category> callback) {
        // First check if category with same name exists
        fetchCategoryByName(category.getName(), new SingleItemCallback<Category>() {
            @Override
            public void onSuccess(Category existingCategory) {
                // Category with same name exists
                callback.onError("Category with name '" + category.getName() + "' already exists");
            }

            @Override
            public void onError(String error) {
                // Category doesn't exist, safe to add
                addItem(category, callback);
            }
        });
    }

    /**
     * Update existing category
     */
    public void updateCategory(int categoryId, Category updatedCategory, DataCallback<Category> callback) {
        updateItem(categoryId, updatedCategory, Category::getId, callback);
    }

    /**
     * Delete category by ID
     */
    public void deleteCategoryById(int categoryId, DataCallback<Category> callback) {
        deleteById(categoryId, Category::getId, callback);
    }

    /**
     * Search categories using a predicate
     */
    public void searchCategories(java.util.function.Predicate<Category> predicate, DataCallback<Category> callback) {
        search(predicate, callback);
    }

    /**
     * Get total category count
     */
    public void getCategoryCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<Category>() {
            @Override
            public void onSuccess(List<Category> categories) {
                callback.onCount(categories.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Check if a category exists by ID
     */
    public void categoryExists(int categoryId, ExistsCallback callback) {
        fetchByIdAsync(categoryId, Category::getId, new SingleItemCallback<Category>() {
            @Override
            public void onSuccess(Category category) {
                callback.onResult(true);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Check if a category exists by name
     */
    public void categoryExistsByName(String name, ExistsCallback callback) {
        fetchCategoryByName(name, new SingleItemCallback<Category>() {
            @Override
            public void onSuccess(Category category) {
                callback.onResult(true);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Get all category names as a String[] (useful for Spinners, dropdowns, etc.)
     */
    public void getAllCategoryNames(NamesCallback callback) {
        fetchAllAsync(new DataCallback<Category>() {
            @Override
            public void onSuccess(List<Category> categories) {
                String[] names = new String[categories.size()];
                for (int i = 0; i < categories.size(); i++) {
                    names[i] = categories.get(i).getName();
                }
                callback.onNames(names);
            }

            @Override
            public void onError(String error) {
                callback.onNames(new String[0]);
            }
        });
    }

    /**
     * Generate next available category ID
     */
    public void getNextCategoryId(IdCallback callback) {
        fetchAllAsync(new DataCallback<Category>() {
            @Override
            public void onSuccess(List<Category> categories) {
                int maxId = 0;
                for (Category c : categories) {
                    if (c.getId() > maxId) {
                        maxId = c.getId();
                    }
                }
                callback.onId(maxId + 1);
            }

            @Override
            public void onError(String error) {
                callback.onId(1); // Default to 1 if error
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

    public interface NamesCallback {
        void onNames(String[] names);
    }

    public interface IdCallback {
        void onId(int id);
    }
}
