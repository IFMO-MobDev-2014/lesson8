package ru.ifmo.md.lesson8;

import java.util.Date;

import ru.ifmo.md.lesson8.json.subcontainers.Main;
import ru.ifmo.md.lesson8.json.subcontainers.Weather;
import ru.ifmo.md.lesson8.json.subcontainers.Wind;

/**
 * Created by dimatomp on 29.11.14.
 */
public class WeatherInfo {
    public final Date date;
    public final Main mainInfo;
    public final Wind wind;
    public final Weather description;

    public WeatherInfo(Date date, Main mainInfo, Wind wind, Weather description) {
        this.date = date;
        this.mainInfo = mainInfo;
        this.wind = wind;
        this.description = description;
    }
}
