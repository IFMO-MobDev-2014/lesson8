package ru.ifmo.md.lesson8;

import java.util.ArrayList;

public class Weather {
    public String city = "";
    public String tempr = "";
    public String date = "";
    public String type = "";
    public String wind = "";
    public String humidity = "";
    public ArrayList<Weather5Days> weather5Days = new ArrayList<Weather5Days>();

    public Weather() {

    }

    public Weather(String city, String tempr, String date, String type, String wind, String humidity, ArrayList<Weather5Days> weather5Days) {
        this.city = city;
        this.tempr = tempr;
        this.date = date;
        this.type = type;
        this.wind = wind;
        this.humidity = humidity;
        this.weather5Days = weather5Days;
    }
}
