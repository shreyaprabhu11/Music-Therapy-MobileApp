package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.project.musicapp.core.models.Category;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.CategoryService;

import java.util.List;
import java.util.function.Predicate;

/**
 * CategoryViewModel - Reactive ViewModel for managing music categories.
 * Integrates with Firebase via CategoryService and exposes LiveData for UI.
 */
public class CategoryViewModel extends BaseViewModel<Category> {
    private final CategoryService categoryService;

    // Additional LiveData for category-specific operations
    private final MutableLiveData<String[]> categoryNamesLiveData = new MutableLiveData<>(new String[0]);
    private final MutableLiveData<Integer> nextCategoryIdLiveData = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> categoryCountLiveData = new MutableLiveData<>(0);

    public CategoryViewModel() {
        super(CategoryService.getInstance());
        this.categoryService = CategoryService.getInstance();
        loadCategoryNames();
        loadCategoryCount();
    }

    /**
     * Get all categories (LiveData from BaseViewModel)
     */
    public LiveData<List<Category>> getAllCategories() {
        return getItems();
    }

    /**
     * Get category names (for spinners/dropdowns)
     */
    public LiveData<String[]> getCategoryNames() {
        return categoryNamesLiveData;
    }

    /**
     * Get category count
     */
    public LiveData<Integer> getCategoryCount() {
        return categoryCountLiveData;
    }

    /**
     * Get next available category ID
     */
    public LiveData<Integer> getNextCategoryId() {
        return nextCategoryIdLiveData;
    }

    /**
     * Load category names (for dropdowns)
     */
    public void loadCategoryNames() {
        categoryService.getAllCategoryNames(new CategoryService.NamesCallback() {
            @Override
            public void onNames(String[] names) {
                categoryNamesLiveData.postValue(names);
            }
        });
    }

    /**
     * Load category count
     */
    public void loadCategoryCount() {
        categoryService.getCategoryCount(new CategoryService.CountCallback() {
            @Override
            public void onCount(int count) {
                categoryCountLiveData.postValue(count);
            }
        });
    }

    /**
     * Load next available category ID
     */
    public void loadNextCategoryId() {
        categoryService.getNextCategoryId(new CategoryService.IdCallback() {
            @Override
            public void onId(int id) {
                nextCategoryIdLiveData.postValue(id);
            }
        });
    }

    /**
     * Add new category
     */
    public void addCategory(String name) {
        loadingLiveData.postValue(true);

        // Get next ID first
        categoryService.getNextCategoryId(new CategoryService.IdCallback() {
            @Override
            public void onId(int nextId) {
                Category category = new Category(nextId, name);

                categoryService.addCategory(category, new BaseService.DataCallback<Category>() {
                    @Override
                    public void onSuccess(List<Category> categories) {
                        successLiveData.postValue("Category '" + name + "' added successfully");
                        loadingLiveData.postValue(false);
                        loadCategoryNames();  // Refresh names
                        loadCategoryCount();   // Refresh count
                        // BaseViewModel auto-updates the list via Firebase listener
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.postValue(error);
                        loadingLiveData.postValue(false);
                    }
                });
            }
        });
    }

    /**
     * Update existing category
     */
    public void updateCategory(int id, String newName) {
        loadingLiveData.postValue(true);
        Category updated = new Category(id, newName);

        categoryService.updateCategory(id, updated, new BaseService.DataCallback<Category>() {
            @Override
            public void onSuccess(List<Category> categories) {
                successLiveData.postValue("Category updated successfully");
                loadingLiveData.postValue(false);
                loadCategoryNames();  // Refresh names
                // BaseViewModel auto-updates the list via Firebase listener
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Delete category
     */
    public void deleteCategory(int id) {
        loadingLiveData.postValue(true);

        categoryService.deleteCategoryById(id, new BaseService.DataCallback<Category>() {
            @Override
            public void onSuccess(List<Category> categories) {
                successLiveData.postValue("Category deleted successfully");
                loadingLiveData.postValue(false);
                loadCategoryNames();  // Refresh names
                loadCategoryCount();   // Refresh count
                // BaseViewModel auto-updates the list via Firebase listener
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Search categories by name (in-memory filter)
     */
    public void searchByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            resetSearch();
            return;
        }

        String lowerQuery = query.toLowerCase();
        search(category -> category.getName().toLowerCase().contains(lowerQuery));
    }

    /**
     * Check if category exists by name
     */
    public void checkCategoryExists(String name, CategoryService.ExistsCallback callback) {
        categoryService.categoryExistsByName(name, callback);
    }

    /**
     * Get category by ID
     */
    public void getCategoryById(int id, BaseService.SingleItemCallback<Category> callback) {
        categoryService.fetchCategoryById(id, callback);
    }

    /**
     * Get category by name
     */
    public void getCategoryByName(String name, BaseService.SingleItemCallback<Category> callback) {
        categoryService.fetchCategoryByName(name, callback);
    }
}
