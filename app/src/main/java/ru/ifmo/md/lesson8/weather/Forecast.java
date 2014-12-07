package ru.ifmo.md.lesson8.weather;

import java.util.Date;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class Forecast {
    private final Weather weather;
    private final Date date;

    public Forecast(Date date, Weather weather) {
        this.date = date;
        this.weather = weather;
    }

    public Weather getWeather() {
        return weather;
    }

    public Date getDate() {
        return date;
    }
}
