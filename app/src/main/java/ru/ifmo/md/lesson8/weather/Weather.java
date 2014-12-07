package ru.ifmo.md.lesson8.weather;

import android.content.ContentValues;

import ru.ifmo.md.lesson8.content.WeatherContract;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class Weather {
    private final Temperature temperature;

    public Weather(Temperature temperature) {
        this.temperature = temperature;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(WeatherContract.WeatherInfo.TEMPERATURE_COLUMN,
                "" + getTemperature().getFahrenheit());
        return values;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public static class Builder {
        private Temperature temperature;

        public Builder setTemperature(Temperature temperature) {
            this.temperature = temperature;
            return this;
        }

        public Weather createWeather() {
            return new Weather(temperature);
        }

        public void clear() {
            this.temperature = null;
        }
    }
}
