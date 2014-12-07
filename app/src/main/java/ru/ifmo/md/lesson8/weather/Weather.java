package ru.ifmo.md.lesson8.weather;

import android.content.ContentValues;

import ru.ifmo.md.lesson8.content.WeatherContract;

import static ru.ifmo.md.lesson8.content.WeatherContract.*;

/**
 * When adding new type of weather characteristic don't forget to fix:
 * this class, {@link WeatherInfo},
 * and {@link ru.ifmo.md.lesson8.content.ContentHelper}
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class Weather {
    private final Temperature low;
    private final Temperature high;
    private final Temperature current;
    private final String description;
    private final int windSpeed;
    private final int pressure;

    public Weather(Temperature low, Temperature high, Temperature current,
                    String description, int windSpeed, int pressure) {
        this.low = low;
        this.high = high;
        this.current = current;
        this.description = description;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
    }

    @Override
    public String toString() {
        String temp = current != null ? current.representAs(Temperature.celsius())
                : low.representAs(Temperature.celsius()) + "-" +
                high.representAs(Temperature.celsius());
        return temp + ", " + description;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        Temperature current = getCurrent();
        values.put(WeatherInfo.TEMPERATURE_COLUMN,
                (current == null ? null : "" + current.getFahrenheit()));
        Temperature high = getHigh();
        values.put(WeatherInfo.TEMPERATURE_COLUMN,
                (high == null ? null : "" + high.getFahrenheit()));
        Temperature low = getLow();
        values.put(WeatherInfo.TEMPERATURE_COLUMN,
                (low == null ? null : "" + low.getFahrenheit()));
        values.put(WeatherInfo.DESCRIPTION_COLUMN,
                description);
        return values;
    }

    public Temperature getLow() {
        return high;
    }

    public Temperature getHigh() {
        return high;
    }

    public String getDescription() {
        return description;
    }

    public Temperature getCurrent() {
        return current;
    }

    public static class Builder {
        private Temperature low;
        private Temperature high;
        private Temperature current;
        private String description;
        private int windSpeed;
        private int pressure;

        public Builder setLow(Temperature low) {
            this.low = low;
            return this;
        }

        public Builder setHigh(Temperature high) {
            this.high = high;
            return this;
        }

        public Builder setCurrent(Temperature current) {
            this.current = current;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setWindSpeed(int windSpeed) {
            this.windSpeed = windSpeed;
            return this;
        }

        public Builder setPressure(int pressure) {
            this.pressure = pressure;
            return this;
        }

        public Weather createWeather() {
            return new Weather(low, high, current, description, windSpeed, pressure);
        }
    }
}
