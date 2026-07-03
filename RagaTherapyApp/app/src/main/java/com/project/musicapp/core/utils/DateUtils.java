package com.project.musicapp.core.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    public static String formatTime(long millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    /**
     * Checks if two timestamps (milliseconds) fall on the same calendar day.
     *
     * @param time1Millis timestamp in milliseconds
     * @param time2Millis timestamp in milliseconds
     * @return true if both timestamps are on the same day
     */
    public static boolean isSameDay(long time1Millis, long time2Millis) {
        // Convert to Calendar once
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1Millis);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2Millis);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
