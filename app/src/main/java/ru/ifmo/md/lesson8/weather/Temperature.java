package ru.ifmo.md.lesson8.weather;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class Temperature {
    /**
     * Store temperature using Fahrenheit Units.
     */
    private final int value;

    public Temperature(int value, Unit unit) {
        this.value = value;
    }

    public String representAs(Unit unit) {
        return unit.representValue(value);
    }

    public static interface Unit {
        String representValue(int value);
        int getFahrenheitValue(int value);
    }

    public static final class Fahrenheit implements Unit {
        private Fahrenheit() {
        }

        @Override
        public String representValue(int value) {
            return value + "\u00b0F";
        }

        @Override
        public int getFahrenheitValue(int value) {
            return value;
        }

        private static Fahrenheit instance = null;
    }

    public static final class Celsius implements Unit {
        private Celsius() {
        }

        @Override
        public String representValue(int value) {
            return fahrenheitToCelsius(value) + "\u00b0C";
        }

        @Override
        public int getFahrenheitValue(int value) {
            return celsiusToFahrenheit(value);
        }

        private static Celsius instance = null;
    }

    public static Celsius celsius() {
        if (Celsius.instance == null) {
            Celsius.instance = new Celsius();
        }
        return Celsius.instance;
    }

    public static Fahrenheit fahrenheit() {
        if (Fahrenheit.instance == null) {
            Fahrenheit.instance = new Fahrenheit();
        }
        return Fahrenheit.instance;
    }

    public static int fahrenheitToCelsius(int value) {
        return (int) Math.round((value - 32) / 1.8);
    }

    public static int celsiusToFahrenheit(int value) {
        return (int) Math.round(1.8 * value + 32);
    }
}
