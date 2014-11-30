package ru.ifmo.md.lesson8;

/**
 * Created by dimatomp on 28.11.14.
 */
public enum TimeOfDay {
    MORNING(new int[]{R.drawable.sun_cloud}),
    DAYTIME(new int[]{R.drawable.sun_cloud}),
    EVENING(new int[]{R.drawable.sun_cloud}),
    NIGHT(new int[]{R.drawable.moon_cloud});

    public final int weatherPictures[];

    TimeOfDay(int[] weatherPictures) {
        this.weatherPictures = weatherPictures;
    }
}
