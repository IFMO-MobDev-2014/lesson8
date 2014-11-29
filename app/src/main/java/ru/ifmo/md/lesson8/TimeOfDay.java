package ru.ifmo.md.lesson8;

/**
 * Created by dimatomp on 28.11.14.
 */
public enum TimeOfDay {
    MORNING(R.drawable.morning, R.drawable.morning_upside_down, new int[]{R.drawable.sun_cloud}),
    DAYTIME(R.drawable.daytime, R.drawable.daytime_upside_down, new int[]{R.drawable.sun_cloud}),
    EVENING(R.drawable.evening, R.drawable.evening_upside_down, new int[]{R.drawable.sun_cloud}),
    NIGHT(R.drawable.night, R.drawable.night_upside_down, new int[]{R.drawable.moon_cloud});

    public final int mainBackground;
    public final int tabBackground;
    public final int weatherPictures[];

    TimeOfDay(int mainBackground, int tabBackground, int[] weatherPictures) {
        this.mainBackground = mainBackground;
        this.tabBackground = tabBackground;
        this.weatherPictures = weatherPictures;
    }
}
