package ru.ifmo.md.lesson8;

public class WeatherDay {
    private String mIcon;
    private String mDate;
    private int mMinTemperature;
    private int mMaxTemperature;
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