package com.example.lesson8;

import java.io.Serializable;

public class DayForecast implements Serializable{
    private String temp;
    private String date;
    private String weather;

    public DayForecast() {}

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public DayForecast(String temp, String date, String weather) {

        this.temp = temp;
        this.date = date;
        this.weather = weather;
    }
}
