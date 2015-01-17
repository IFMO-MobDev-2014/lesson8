package ru.ifmo.md.lesson8;

/**
 * Created by MSviridenkov on 30.11.2014.
 */
public class Day {
    private String conditionCode;
    private String condition;
    private String name;
    private String forecast;

    Day() {
    }

    Day(Day day) {
        this.conditionCode = day.getConditionCode();
        this.condition = day.getCondition();
        this.name = day.getName();
        this.forecast = day.getForecast();
    }

    Day(String conditionCode, String condition, String name, String forecast) {
        this.conditionCode = conditionCode;
        this.condition = condition;
        this.name = name;
        this.forecast = forecast;
    }

    public String getConditionCode() {
        return this.conditionCode;
    }

    public String getCondition() {
        return this.condition;
    }

    public String getName() {
        return this.name;
    }

    public String getForecast() {
        return this.forecast;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setForecast(String forecast) {
        this.forecast = forecast;
    }
}
