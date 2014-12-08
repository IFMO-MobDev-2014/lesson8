package ru.ifmo.md.lesson8.json;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import ru.ifmo.md.lesson8.json.subcontainers.City;
import ru.ifmo.md.lesson8.json.subcontainers.Main;
import ru.ifmo.md.lesson8.json.subcontainers.Weather;
import ru.ifmo.md.lesson8.json.subcontainers.Wind;

import static ru.ifmo.md.lesson8.WeatherColumns.TIME;

/**
 * Created by dimatomp on 30.11.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class CurrentWeather extends City implements WeatherContainer {
    public List<Weather> weather;
    public Main main;
    public Wind wind;
    public int dt;

    @Override
    public ContentValues[] getWeatherInfo() {
        ContentValues[] result = new ContentValues[weather.size()];
        for (int i = 0; i < weather.size(); i++) {
            result[i] = new ContentValues();
            weather.get(i).inflate(result[i]);
            main.inflate(result[i]);
            wind.inflate(result[i]);
            result[i].put(TIME, dt);
            inflate(result[i]);
        }
        return result;
    }
}
