package com.project.musicapp.core.services;

import com.project.musicapp.core.models.Category;
import com.project.musicapp.core.models.Music;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Firebase-integrated MusicService extending BaseService with CRUD & search.
 * All operations use async callbacks for Firebase integration.
 */
public class MusicService extends BaseService<Music> {
    private static MusicService instance;

    private MusicService() {
        super(Music.class, "music"); // "music" = Firebase node name
    }

    // ✅ SINGLETON PATTERN
    public static synchronized MusicService getInstance() {
        if (instance == null) {
            instance = new MusicService();
        }
        return instance;
    }

    /**
     * Fetch all music
     */
    public void fetchAllMusic(DataCallback<Music> callback) {
        fetchAllAsync(callback);
    }

    /**
     * Fetch music by ID
     */
    public void fetchMusicById(int id, SingleItemCallback<Music> callback) {
        fetchByIdAsync(id, Music::getId, callback);
    }

    /**
     * Add new music to Firebase
     */
    public void addMusic(Music music, DataCallback<Music> callback) {
        addItem(music, callback);
    }

    /**
     * Update music in Firebase
     */
    public void updateMusic(int id, Music updatedMusic, DataCallback<Music> callback) {
        updateItem(id, updatedMusic, Music::getId, callback);
    }

    /**
     * Delete music from Firebase
     */
    public void deleteMusic(int id, DataCallback<Music> callback) {
        deleteById(id, Music::getId, callback);
    }

    /**
     * Search music based on predicate
     */
    public void searchMusic(java.util.function.Predicate<Music> predicate, DataCallback<Music> callback) {
        search(predicate, callback);
    }

    /**
     * Fetch all unique categories (computed from existing music items)
     */
    public void getAllCategories(CategoriesCallback callback) {
        fetchAllAsync(new DataCallback<Music>() {
            @Override
            public void onSuccess(List<Music> musicList) {
                Set<Category> categorySet = new HashSet<>();
                for (Music music : musicList) {
                    if (music.getCategory() != null) {
                        categorySet.add(music.getCategory());
                    }
                }
                callback.onCategories(new ArrayList<>(categorySet));
            }

            @Override
            public void onError(String error) {
                callback.onCategories(new ArrayList<>());
            }
        });
    }

    /**
     * Fetch all music belonging to a specific category
     */
    public void getMusicByCategory(int categoryId, DataCallback<Music> callback) {
        search(music -> music.getCategory() != null &&
                music.getCategory().getId() == categoryId, callback);
    }

    /**
     * Fetch music by category name
     */
    public void getMusicByCategoryName(String categoryName, DataCallback<Music> callback) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            callback.onError("Category name cannot be null or empty");
            return;
        }

        search(music -> music.getCategory() != null &&
                music.getCategory().getName().equalsIgnoreCase(categoryName), callback);
    }

    /**
     * Search music by name (partial match)
     */
    public void searchMusicByName(String name, DataCallback<Music> callback) {
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Search name cannot be null or empty");
            return;
        }

        String lowerName = name.toLowerCase();
        search(music -> music.getMusicName() != null &&
                music.getMusicName().toLowerCase().contains(lowerName), callback);
    }

    /**
     * Search music by artist (partial match)
     */
    public void searchMusicByArtist(String artist, DataCallback<Music> callback) {
        if (artist == null || artist.trim().isEmpty()) {
            callback.onError("Artist name cannot be null or empty");
            return;
        }

        String lowerArtist = artist.toLowerCase();
        search(music -> music.getMusicArtist() != null &&
                music.getMusicArtist().toLowerCase().contains(lowerArtist), callback);
    }

    /**
     * Get music count
     */
    public void getMusicCount(CountCallback callback) {
        fetchAllAsync(new DataCallback<Music>() {
            @Override
            public void onSuccess(List<Music> musicList) {
                callback.onCount(musicList.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Get music count by category
     */
    public void getMusicCountByCategory(int categoryId, CountCallback callback) {
        getMusicByCategory(categoryId, new DataCallback<Music>() {
            @Override
            public void onSuccess(List<Music> musicList) {
                callback.onCount(musicList.size());
            }

            @Override
            public void onError(String error) {
                callback.onCount(0);
            }
        });
    }

    /**
     * Check if music exists by ID
     */
    public void musicExists(int musicId, ExistsCallback callback) {
        fetchByIdAsync(musicId, Music::getId, new SingleItemCallback<Music>() {
            @Override
            public void onSuccess(Music music) {
                callback.onResult(true);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false);
            }
        });
    }

    /**
     * Get music with duration greater than specified value
     */
    public void getMusicByMinDuration(int minDuration, DataCallback<Music> callback) {
        search(music -> music.getDuration() >= minDuration, callback);
    }

    /**
     * Get music with duration in range
     */
    public void getMusicByDurationRange(int minDuration, int maxDuration, DataCallback<Music> callback) {
        search(music -> music.getDuration() >= minDuration &&
                music.getDuration() <= maxDuration, callback);
    }

    /**
     * Get all music names (for dropdowns/spinners)
     */
    public void getAllMusicNames(NamesCallback callback) {
        fetchAllAsync(new DataCallback<Music>() {
            @Override
            public void onSuccess(List<Music> musicList) {
                List<String> names = new ArrayList<>();
                for (Music music : musicList) {
                    if (music.getMusicName() != null) {
                        names.add(music.getMusicName());
                    }
                }
                callback.onNames(names);
            }

            @Override
            public void onError(String error) {
                callback.onNames(new ArrayList<>());
            }
        });
    }

    /**
     * Get all unique artists
     */
    public void getAllArtists(NamesCallback callback) {
        fetchAllAsync(new DataCallback<Music>() {
            @Override
            public void onSuccess(List<Music> musicList) {
                Set<String> artistSet = new HashSet<>();
                for (Music music : musicList) {
                    if (music.getMusicArtist() != null && !music.getMusicArtist().isEmpty()) {
                        artistSet.add(music.getMusicArtist());
                    }
                }
                callback.onNames(new ArrayList<>(artistSet));
            }

            @Override
            public void onError(String error) {
                callback.onNames(new ArrayList<>());
            }
        });
    }

    /**
     * Delete all music in a category
     */
    public void deleteMusicByCategory(int categoryId, DataCallback<Music> callback) {
        getMusicByCategory(categoryId, new DataCallback<Music>() {
            @Override
            public void onSuccess(List<Music> musicList) {
                if (musicList.isEmpty()) {
                    callback.onSuccess(musicList);
                    return;
                }

                // Delete each music item
                int[] deletedCount = {0};
                for (Music music : musicList) {
                    deleteMusic(music.getId(), new DataCallback<Music>() {
                        @Override
                        public void onSuccess(List<Music> deletedMusic) {
                            deletedCount[0]++;
                            if (deletedCount[0] == musicList.size()) {
                                callback.onSuccess(musicList);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to delete music: " + error);
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
    public interface CategoriesCallback {
        void onCategories(List<Category> categories);
    }

    public interface CountCallback {
        void onCount(int count);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
    }

    public interface NamesCallback {
        void onNames(List<String> names);
    }
}
