package ru.ifmo.md.lesson8.json;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import ru.ifmo.md.lesson8.json.subcontainers.City;
import ru.ifmo.md.lesson8.json.subcontainers.ForecastEntry;

/**
 * Created by dimatomp on 30.11.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class WeatherForecast implements WeatherContainer {
    public City city;
    public List<ForecastEntry> list;

    @Override
    public ContentValues[] getWeatherInfo() {
        ContentValues[] result = new ContentValues[list != null ? list.size() : 0];
        for (int i = 0; i < result.length; i++) {
            result[i] = new ContentValues();
            city.inflate(result[i]);
            list.get(i).inflate(result[i]);
        }
        return result;
    }
}
