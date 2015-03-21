package ru.ifmo.md.lesson8;

/**
 * @author Aydar Gizatullin a.k.a. lightning95
 */

public class WeatherDay {
    private int mMinTemperature;
    private int mMaxTemperature;
    private String mIcon;
    private String mDate;

    public int getMinTemperature() {
        return mMinTemperature;
    }

    public void setMinTemperature(int minTemperature) {
        mMinTemperature = minTemperature;
    }

    public int getMaxTemperature() {
        return mMaxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        mMaxTemperature = maxTemperature;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }
}
