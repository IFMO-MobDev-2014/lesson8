package ru.ifmo.md.lesson8.json;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import ru.ifmo.md.lesson8.json.subcontainers.ForecastEntry;

import static ru.ifmo.md.lesson8.WeatherColumns.CITY_ID;

/**
 * Created by dimatomp on 30.11.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class WeatherHistory implements WeatherContainer {
    public int cityId;
    public List<ForecastEntry> list;

    @Override
    public ContentValues[] getWeatherInfo() {
        ContentValues result[] = new ContentValues[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = new ContentValues();
            result[i].put(CITY_ID, cityId);
            list.get(i).inflate(result[i]);
        }
        return result;
    }
}
