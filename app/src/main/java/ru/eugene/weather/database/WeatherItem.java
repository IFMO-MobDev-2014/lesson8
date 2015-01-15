package ru.eugene.weather.database;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by eugene on 12/16/14.
 */
public class WeatherItem implements Serializable {
    private int id;
    private int idCity;
    private int temp;
    private int tempMin;
    private int tempMax;
    private double speed;
    private int humidity;
    private double visibility;
    private int code;
    private String pubDate;
    private String text;
    private int chill;
    public static final int INF = (int) -1e9;

    public WeatherItem() {
        idCity =  chill = humidity =  temp = tempMax = tempMin = code = INF;
        visibility = speed = INF;
    }

    public WeatherItem(int id, int idCity, int temp, int tempMin, int tempMax, double speed, int humidity, double visibility, int code, String pubDate, String text, int chill) {
        this.id = id;
        this.idCity = idCity;
        this.temp = temp;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.speed = speed;
        this.humidity = humidity;
        this.visibility = visibility;
        this.code = code;
        this.pubDate = pubDate;
        this.text = text;
        this.chill = chill;
    }

    public int getTempMin() {
        return tempMin;
    }

    public void setTempMin(int tempMin) {
        this.tempMin = tempMin;
    }

    public int getTempMax() {
        return tempMax;
    }

    public void setTempMax(int tempMax) {
        this.tempMax = tempMax;
    }

    public int getId() {
        return id;
    }

    public int getIdCity() {
        return idCity;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setChill(int chill) {
        this.chill = chill;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIdCity(int idCity) {
        this.idCity = idCity;
    }

    public String getPubDate() {
        return pubDate;
    }

    public double getSpeed() {
        return speed;
    }

    public int getChill() {
        return chill;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getVisibility() {
        return visibility;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
