package ru.ifmo.md.lesson8.json.subcontainers;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static ru.ifmo.md.lesson8.WeatherColumns.CITY_ID;

/**
 * Created by dimatomp on 30.11.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class City implements WeatherEntry {
    public int id;
    public String name;

    @Override
    public void inflate(ContentValues values) {
        values.put(CITY_ID, id);
    }
}
