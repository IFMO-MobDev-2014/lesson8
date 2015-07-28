package ru.ifmo.md.lesson8;

import android.content.ContentValues;

public class City {
    private int id;
    private String name;
    private long updateDate;

    public City(int id, String name, long updateDate) {
        this.id = id;
        this.name = name;
        this.updateDate = updateDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(WeatherDatabaseHelper.CITY_NAME, name);
        cv.put(WeatherDatabaseHelper.CITY_UPDATE_DATE, updateDate);
        return cv;
    }
}
