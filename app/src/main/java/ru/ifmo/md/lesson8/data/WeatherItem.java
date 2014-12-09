package ru.ifmo.md.lesson8.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariashka on 11/28/14.
 */
public class WeatherItem {

    private boolean isLoaded;
    private int id;
    private String name;
    private int minT, maxT, feels;
    private String date;
    private int currT;
    private String condition;
    private List<Integer> hourlyT = new ArrayList<>();
    private List<String> hourlyC = new ArrayList<>();
    private List<WeatherItem> next = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMin() {
        return minT;
    }

    public void setMin(int min_t) {
        this.minT = min_t;
    }

    public int getMax() {
        return maxT;
    }

    public void setMax(int max_t) {
        this.maxT = max_t;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }


    public List<Integer> getHourlyT() {
        return hourlyT;
    }

    public void addHourlyT(int t) {
        hourlyT.add(t);
    }

    public List<String> getHourlyC() {
        return hourlyC;
    }

    public void addHourlyC(String c) {
        hourlyC.add(c);
    }

    public void addNext(WeatherItem t) {
        next.add(t);
    }

    public List<WeatherItem> getNext() {
        return next;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCurrT() {
        return currT;
    }

    public void setCurrT(int currT) {
        this.currT = currT;
    }

    public int getFeels() {
        return feels;
    }

    public void setFeels(int feels) {
        this.feels = feels;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }
}
