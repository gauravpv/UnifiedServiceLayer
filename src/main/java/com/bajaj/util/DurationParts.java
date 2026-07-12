package com.bajaj.util;

public final class DurationParts {

    private DurationParts() {
    }

    /** Formats milliseconds as minutes:seconds:milliseconds, e.g. 1:02:346 */
    public static String formatMillis(long millis) {
        long safe = Math.max(0, millis);
        int minutes = (int) (safe / 60_000);
        int seconds = (int) ((safe % 60_000) / 1_000);
        int ms = (int) (safe % 1_000);
        return String.format("%d:%02d:%03d", minutes, seconds, ms);
    }
}
