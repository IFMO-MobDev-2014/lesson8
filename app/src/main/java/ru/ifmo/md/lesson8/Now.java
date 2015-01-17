package ru.ifmo.md.lesson8;

/**
 * Created by MSviridenkov on 30.11.2014.
 */
public class Now {
    private String conditionCode;
    private String temp;
    private String condition;
    private String windSpeed;
    private String humidity;

    Now() {
    }

    Now(Now now) {
        this.conditionCode = now.getConditionCode();
        this.temp = now.getTemp();
        this.condition = now.getCondition();
        this.windSpeed = now.getWindSpeed();
        this.humidity = now.getHumidity();
    }

    Now(String conditionCode, String temp, String condition, String windSpeed, String humidity) {
        this.conditionCode = conditionCode;
        this.temp = temp;
        this.condition = condition;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getConditionCode() {
        return this.conditionCode;
    }

    public String getTemp() {
        return this.temp;
    }

    public String getCondition() {
        return this.condition;
    }

    public String getWindSpeed() {
        return this.windSpeed;
    }

    public String getHumidity() {
        return this.humidity;
    }
}
