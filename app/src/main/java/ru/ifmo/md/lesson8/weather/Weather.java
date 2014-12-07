package ru.ifmo.md.lesson8.weather;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class Weather {
    private final Temperature temperature;

    public Weather(Temperature temperature) {
        this.temperature = temperature;
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
