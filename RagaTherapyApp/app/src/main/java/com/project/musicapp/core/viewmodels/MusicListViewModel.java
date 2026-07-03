package com.project.musicapp.core.viewmodels;

import androidx.lifecycle.LiveData;

import com.project.musicapp.core.models.Category;
import com.project.musicapp.core.models.Music;
import com.project.musicapp.core.services.BaseService;
import com.project.musicapp.core.services.MusicService;

import java.util.List;
import java.util.function.Predicate;

/**
 * ✅ Updated MusicListViewModel with proper async handling
 * Follows singleton pattern and uses callbacks for all Firebase operations
 */
public class MusicListViewModel extends BaseViewModel<Music> {

    private final MusicService musicService;

    public MusicListViewModel() {
        super(MusicService.getInstance()); // ✅ Use singleton instead of new
        this.musicService = MusicService.getInstance();
    }

    // ═══════════════════════════════════════════════════════════════
    // BASIC CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Get LiveData of all music (auto updates when Firebase changes) */
    public LiveData<List<Music>> getAllMusic() {
        return getItems();
    }

    /** Add new music */
    public void addMusic(Music music) {
        addItem(music);
    }

    /** Update existing music */
    public void updateMusic(int id, Music updatedMusic) {
        updateItem(id, updatedMusic, Music::getId);
    }

    /** Delete music by ID */
    public void deleteMusic(int id) {
        deleteItem(id, Music::getId);
    }

    /** Search music locally (filtered from LiveData list) */
    public void searchMusic(Predicate<Music> predicate) {
        search(predicate);
    }

    /** Force manual Firebase refresh */
    public void refreshMusic() {
        refresh();
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ SPECIALIZED QUERY METHODS (Async with Callbacks)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Fetch single music item by ID (async with callback)
     */
    public void fetchMusicById(int id, BaseService.SingleItemCallback<Music> callback) {
        musicService.fetchMusicById(id, callback);
    }

    /**
     * Get all unique categories (async)
     */
    public void getAllCategories(MusicService.CategoriesCallback callback) {
        musicService.getAllCategories(callback);
    }

    /**
     * Get all music in a specific category (async)
     */
    public void getMusicByCategory(int categoryId, BaseService.DataCallback<Music> callback) {
        musicService.getMusicByCategory(categoryId, callback);
    }

    /**
     * Get music by category name (async)
     */
    public void getMusicByCategoryName(String categoryName, BaseService.DataCallback<Music> callback) {
        musicService.getMusicByCategoryName(categoryName, callback);
    }

    /**
     * Search music by name (partial match)
     */
    public void searchMusicByName(String name, BaseService.DataCallback<Music> callback) {
        musicService.searchMusicByName(name, callback);
    }

    /**
     * Search music by artist (partial match)
     */
    public void searchMusicByArtist(String artist, BaseService.DataCallback<Music> callback) {
        musicService.searchMusicByArtist(artist, callback);
    }

    /**
     * Get music count
     */
    public void getMusicCount(MusicService.CountCallback callback) {
        musicService.getMusicCount(callback);
    }

    /**
     * Get music count by category
     */
    public void getMusicCountByCategory(int categoryId, MusicService.CountCallback callback) {
        musicService.getMusicCountByCategory(categoryId, callback);
    }

    /**
     * Check if music exists by ID
     */
    public void musicExists(int musicId, MusicService.ExistsCallback callback) {
        musicService.musicExists(musicId, callback);
    }

    /**
     * Get music with duration greater than specified value
     */
    public void getMusicByMinDuration(int minDuration, BaseService.DataCallback<Music> callback) {
        musicService.getMusicByMinDuration(minDuration, callback);
    }

    /**
     * Get music with duration in range
     */
    public void getMusicByDurationRange(int minDuration, int maxDuration, BaseService.DataCallback<Music> callback) {
        musicService.getMusicByDurationRange(minDuration, maxDuration, callback);
    }

    /**
     * Get all music names (for dropdowns/spinners)
     */
    public void getAllMusicNames(MusicService.NamesCallback callback) {
        musicService.getAllMusicNames(callback);
    }

    /**
     * Get all unique artists
     */
    public void getAllArtists(MusicService.NamesCallback callback) {
        musicService.getAllArtists(callback);
    }

    /**
     * Delete all music in a category
     */
    public void deleteMusicByCategory(int categoryId, BaseService.DataCallback<Music> callback) {
        musicService.deleteMusicByCategory(categoryId, callback);
    }

    // ═══════════════════════════════════════════════════════════════
    // ✅ ADDITIONAL HELPER METHODS WITH CALLBACKS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Add music with callback (recommended for error handling)
     */
    public void addMusicWithCallback(Music music, BaseService.DataCallback<Music> callback) {
        musicService.addMusic(music, callback);
    }

    /**
     * Update music with callback
     */
    public void updateMusicWithCallback(int id, Music updatedMusic, BaseService.DataCallback<Music> callback) {
        musicService.updateMusic(id, updatedMusic, callback);
    }

    /**
     * Delete music with callback
     */
    public void deleteMusicWithCallback(int id, BaseService.DataCallback<Music> callback) {
        musicService.deleteMusic(id, callback);
    }

    /**
     * Search music with callback
     */
    public void searchMusicWithCallback(Predicate<Music> predicate, BaseService.DataCallback<Music> callback) {
        musicService.searchMusic(predicate, callback);
    }
}
