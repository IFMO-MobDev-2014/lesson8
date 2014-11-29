package ru.ifmo.md.lesson8;

/**
 * Created by dimatomp on 29.11.14.
 */
public class WeatherInfo {
    public int temperature;
    public float windSpeed;
    public float windAngle;
    public int humidity;
    public int pressure;
    public int weatherDescription;

    public WeatherInfo(int temperature, float windSpeed, float windAngle, int humidity, int pressure, int weatherDescription) {
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.windAngle = windAngle;
        this.humidity = humidity;
        this.pressure = pressure;
        this.weatherDescription = weatherDescription;
    }
}
