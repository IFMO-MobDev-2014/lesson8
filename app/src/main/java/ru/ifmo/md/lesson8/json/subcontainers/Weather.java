package ru.ifmo.md.lesson8.json.subcontainers;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static ru.ifmo.md.lesson8.WeatherColumns.DESCRIPTION;
import static ru.ifmo.md.lesson8.WeatherColumns.IN_BRIEF;

/**
 * Created by dimatomp on 30.11.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Weather implements WeatherEntry {
    public String main;
    public String description;

    @Override
    public void inflate(ContentValues values) {
        values.put(IN_BRIEF, main);
        values.put(DESCRIPTION, description);
    }
}
