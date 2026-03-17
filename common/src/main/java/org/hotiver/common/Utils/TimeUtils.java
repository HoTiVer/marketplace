package org.hotiver.common.Utils;

public class TimeUtils {

    public static long toSeconds(long ms) {
        return ms / 1000;
    }

    public static long toMinutes(long ms) {
        return ms / (1000 * 60);
    }

    public static int toSecondsInt(long ms) {
        return (int) (ms / 1000);
    }
}
