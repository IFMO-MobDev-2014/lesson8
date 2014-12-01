package ru.ifmo.md.lesson8;

/**
 * Created by dimatomp on 28.11.14.
 */
public enum TimeOfDay {
    NIGHT(new int[]{R.drawable.moon_cloud}),
    MORNING(new int[]{R.drawable.sun_cloud}),
    DAYTIME(new int[]{R.drawable.sun_cloud}),
    EVENING(new int[]{R.drawable.sun_cloud});

    public final int weatherPictures[];

    TimeOfDay(int[] weatherPictures) {
        this.weatherPictures = weatherPictures;
    }
}
