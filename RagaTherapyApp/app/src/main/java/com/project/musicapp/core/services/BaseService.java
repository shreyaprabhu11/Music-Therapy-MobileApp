package com.project.musicapp.core.services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Generic BaseService for Firebase Realtime Database CRUD operations.
 * @param <T> Model type
 */
public abstract class BaseService<T> {
    public final DatabaseReference databaseReference;
    public final Class<T> modelClass;
    private static final String TAG = "FirebaseBaseService";

    // ✅ CALLBACK INTERFACES
    public interface DataCallback<T> {
        void onSuccess(List<T> items);
        void onError(String error);
    }

    public interface SingleItemCallback<T> {
        void onSuccess(T item);
        void onError(String error);
    }

    public BaseService(Class<T> modelClass, String nodeName) {
        this.databaseReference = FirebaseDatabase.getInstance().getReference(nodeName);
        this.modelClass = modelClass;
    }

    // ✅ FETCH ALL - NOW WITH CALLBACK
    public void fetchAllAsync(DataCallback<T> callback) {
        try {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<T> list = new ArrayList<>();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        T item = snap.getValue(modelClass);
                        if (item != null) {
                            list.add(item);
                        }
                    }
                    Log.d(TAG, "Fetched " + list.size() + " items");
                    callback.onSuccess(list);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to fetch data: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all data: ", e);
            callback.onError(e.getMessage());
        }
    }

    public List<T> fetchAll() {
        List<T> list = new ArrayList<>();
        try {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        T item = snap.getValue(modelClass);
                        if (item != null) list.add(item);
                    }
                    Log.d(TAG, "Fetched " + list.size() + " items from " + databaseReference.getKey());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to fetch data: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all data: ", e);
        }
        return list;
    }

    // ✅ FETCH BY ID - NOW WITH CALLBACK
    public void fetchByIdAsync(int id, Function<T, Integer> idExtractor, SingleItemCallback<T> callback) {
        fetchAllAsync(new DataCallback<T>() {
            @Override
            public void onSuccess(List<T> items) {
                for (T item : items) {
                    if (idExtractor.apply(item) == id) {
                        Log.d(TAG, "Found item with ID: " + id);
                        callback.onSuccess(item);
                        return;
                    }
                }
                String error = "Item not found with ID: " + id;
                Log.w(TAG, error);
                callback.onError(error);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public T fetchById(int id, Function<T, Integer> idExtractor) {
        List<T> all = fetchAll();
        for (T item : all) {
            if (idExtractor.apply(item) == id) return item;
        }
        Log.w(TAG, "Item with ID " + id + " not found in " + databaseReference.getKey());
        return null;
    }

    // ✅ ADD ITEM - WITH CALLBACK
    public void addItem(T item, DataCallback<T> callback) {
        try {
            String key = databaseReference.push().getKey();
            if (key != null) {
                databaseReference.child(key).setValue(item)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Item added successfully");
                            List<T> result = new ArrayList<>();
                            result.add(item);
                            callback.onSuccess(result);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to add item: " + e.getMessage());
                            callback.onError(e.getMessage());
                        });
            } else {
                callback.onError("Failed to generate key");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding item: ", e);
            callback.onError(e.getMessage());
        }
    }

    public void addItem(T item) {
        try {
            String key = String.valueOf(item.hashCode());
            databaseReference.child(key).setValue(item)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Item added successfully to " + databaseReference.getKey()))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add item: ", e));
        } catch (Exception e) {
            Log.e(TAG, "Error adding item: ", e);
        }
    }

    // ✅ UPDATE ITEM - WITH CALLBACK
    public void updateItem(int id, T updatedItem, Function<T, Integer> idExtractor, DataCallback<T> callback) {
        fetchByIdAsync(id, idExtractor, new SingleItemCallback<T>() {
            @Override
            public void onSuccess(T item) {
                // Find the key for this item
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            T existingItem = snap.getValue(modelClass);
                            if (existingItem != null && idExtractor.apply(existingItem) == id) {
                                String key = snap.getKey();
                                if (key != null) {
                                    databaseReference.child(key).setValue(updatedItem)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "Item updated successfully");
                                                List<T> result = new ArrayList<>();
                                                result.add(updatedItem);
                                                callback.onSuccess(result);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Failed to update item: " + e.getMessage());
                                                callback.onError(e.getMessage());
                                            });
                                }
                                return;
                            }
                        }
                        callback.onError("Item not found for update");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public boolean updateItem(int id, T updatedItem, Function<T, Integer> idExtractor) {
        try {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean found = false;
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        T item = snap.getValue(modelClass);
                        if (item != null && idExtractor.apply(item) == id) {
                            databaseReference.child(snap.getKey()).setValue(updatedItem)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Item updated successfully"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update item: ", e));
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        Log.w(TAG, "No matching item found to update (ID " + id + ")");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error while updating item: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error updating item: ", e);
        }
        return true;
    }

    // ✅ DELETE BY ID - WITH CALLBACK
    public void deleteById(int id, Function<T, Integer> idExtractor, DataCallback<T> callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    T item = snap.getValue(modelClass);
                    if (item != null && idExtractor.apply(item) == id) {
                        String key = snap.getKey();
                        if (key != null) {
                            databaseReference.child(key).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Item deleted successfully");
                                        callback.onSuccess(new ArrayList<>());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to delete item: " + e.getMessage());
                                        callback.onError(e.getMessage());
                                    });
                        }
                        return;
                    }
                }
                callback.onError("Item not found for deletion");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to delete: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    // ✅ SEARCH - WITH CALLBACK
    public void search(Predicate<T> predicate, DataCallback<T> callback) {
        fetchAllAsync(new DataCallback<T>() {
            @Override
            public void onSuccess(List<T> items) {
                List<T> results = new ArrayList<>();
                for (T item : items) {
                    if (predicate.test(item)) {
                        results.add(item);
                    }
                }
                Log.d(TAG, "Search found " + results.size() + " items");
                callback.onSuccess(results);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ADD MULTIPLE ITEMS - WITH CALLBACK
    public void addMultiple(List<T> items, DataCallback<T> callback) {
        if (items == null || items.isEmpty()) {
            callback.onError("No items to add");
            return;
        }

        int[] successCount = {0};
        int totalItems = items.size();

        for (T item : items) {
            String key = databaseReference.push().getKey();
            if (key != null) {
                databaseReference.child(key).setValue(item)
                        .addOnSuccessListener(aVoid -> {
                            successCount[0]++;
                            if (successCount[0] == totalItems) {
                                Log.d(TAG, "All items added successfully");
                                callback.onSuccess(items);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to add item: " + e.getMessage());
                            callback.onError(e.getMessage());
                        });
            }
        }
    }
}
