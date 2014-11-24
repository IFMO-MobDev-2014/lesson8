package com.ilya239.weather;

/**
 * Created by Илья on 24.11.2014.
 */
public class DetailsDay {
    private String day,date,low,high,sky;
    DetailsDay() { day = date = low = high = sky = null; }
    void setDay(String day) {
        this.day = day;
    }

    void setDate(String date) {
        this.date = date;
    }
    void setLow(String low) {
        this.low = low;
    }
    void setHigh(String high) {
        this.high = high;
    }
    void setSky(String sky) {
        this.sky = sky;
    }

    String getDay() {
        return day;
    }
    String getDate() {
        return date;
    }
    String getLow() {
        return low;
    }
    String getHigh() {
        return high;
    }
    String getSky() {
        return sky;
    }
    String getAll() {
        return "\n\n" + day+ "\n" + date+ "\n" + "low:" + low+ "\n" + "high:" + high+ "\n" + sky;
    }


}
