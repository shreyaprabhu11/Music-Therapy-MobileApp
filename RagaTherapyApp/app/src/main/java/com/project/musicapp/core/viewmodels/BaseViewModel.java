package com.project.musicapp.core.viewmodels;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.project.musicapp.core.services.BaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Reactive BaseViewModel for Firebase integration with LiveData.
 * @param <T> Model type
 */
public class BaseViewModel<T> extends ViewModel {
    protected final BaseService<T> service;

    // LiveData for observing data changes
    protected final MutableLiveData<List<T>> itemsLiveData = new MutableLiveData<>(new ArrayList<>());
    protected final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    protected final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);
    protected final MutableLiveData<String> successLiveData = new MutableLiveData<>(null);

    private static final String TAG = "BaseViewModel";
    private ValueEventListener firebaseListener;

    public BaseViewModel(BaseService<T> service) {
        this.service = service;
        startListeningToFirebase();
    }

    /**
     * Start observing Firebase data for real-time updates
     */
    private void startListeningToFirebase() {
        loadingLiveData.postValue(true);

        firebaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> list = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    T item = snap.getValue(service.modelClass);
                    if (item != null) {
                        list.add(item);
                    }
                }
                itemsLiveData.postValue(list);
                loadingLiveData.postValue(false);
                Log.d(TAG, "Loaded " + list.size() + " items");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase read cancelled: " + error.getMessage());
                errorLiveData.postValue("Failed to load data: " + error.getMessage());
                loadingLiveData.postValue(false);
            }
        };

        service.databaseReference.addValueEventListener(firebaseListener);
    }

    /**
     * Expose LiveData for observing items
     */
    public LiveData<List<T>> getItems() {
        return itemsLiveData;
    }

    /**
     * Expose loading state
     */
    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    /**
     * Expose error messages
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * Expose success messages
     */
    public LiveData<String> getSuccess() {
        return successLiveData;
    }

    /**
     * Force refresh manually
     */
    public void refresh() {
        loadingLiveData.postValue(true);
        service.fetchAllAsync(new BaseService.DataCallback<T>() {
            @Override
            public void onSuccess(List<T> items) {
                itemsLiveData.postValue(items);
                loadingLiveData.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Add item with callback handling
     */
    public void addItem(T item) {
        loadingLiveData.postValue(true);
        service.addItem(item, new BaseService.DataCallback<T>() {
            @Override
            public void onSuccess(List<T> items) {
                successLiveData.postValue("Item added successfully");
                loadingLiveData.postValue(false);
                // Firebase listener will auto-update the list
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue("Failed to add item: " + error);
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Update item with callback handling
     */
    public void updateItem(int id, T updatedItem, Function<T, Integer> idExtractor) {
        loadingLiveData.postValue(true);
        service.updateItem(id, updatedItem, idExtractor, new BaseService.DataCallback<T>() {
            @Override
            public void onSuccess(List<T> items) {
                successLiveData.postValue("Item updated successfully");
                loadingLiveData.postValue(false);
                // Firebase listener will auto-update the list
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue("Failed to update item: " + error);
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Delete item with callback handling
     */
    public void deleteItem(int id, Function<T, Integer> idExtractor) {
        loadingLiveData.postValue(true);
        service.deleteById(id, idExtractor, new BaseService.DataCallback<T>() {
            @Override
            public void onSuccess(List<T> items) {
                successLiveData.postValue("Item deleted successfully");
                loadingLiveData.postValue(false);
                // Firebase listener will auto-update the list
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue("Failed to delete item: " + error);
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Search items (in-memory filtering)
     */
    public void search(Predicate<T> predicate) {
        List<T> all = itemsLiveData.getValue();
        if (all == null) return;

        List<T> filtered = new ArrayList<>();
        for (T item : all) {
            if (predicate.test(item)) {
                filtered.add(item);
            }
        }
        itemsLiveData.postValue(filtered);
    }

    /**
     * Reset search (show all items)
     */
    public void resetSearch() {
        refresh();
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorLiveData.postValue(null);
    }

    /**
     * Clear success message
     */
    public void clearSuccess() {
        successLiveData.postValue(null);
    }

    /**
     * Clean up Firebase listener when ViewModel is destroyed
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (firebaseListener != null) {
            service.databaseReference.removeEventListener(firebaseListener);
        }
    }
}
