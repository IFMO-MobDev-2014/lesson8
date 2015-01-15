package ru.ifmo.md.lesson8.dummy;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static ArrayList<CitiesItem> ITEMS = new ArrayList<CitiesItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<Integer, CitiesItem> ITEM_MAP = new HashMap<Integer, CitiesItem>();

    public static void addItem(CitiesItem item , int position) {
        ITEMS.add(item);
        ITEM_MAP.put(position, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class CitiesItem {
        public int id;
        public String name;
        public String country;
        public int woeid;

        public CitiesItem(int id, String name, String country, int woeid) {
            this.id = id;
            this.name = name;
            this.country = country;
            this.woeid = woeid;
        }

        @Override
        public String toString() {
            if (country.equals("")) {
                return name;
            }
            return (name+", "+country);
        }
    }
    public static ArrayList<WeatherItem> WEATHER_ITEMS = new ArrayList<WeatherItem>();


    public static Map<Integer, WeatherItem> WEATHER_MAP = new HashMap<Integer, WeatherItem>();

    public static class WeatherItem {
        public int id;
        public int woeid;

        public int date;
        public int temp;
        public int humidity;
        public int pressure;
        public int wind;
        public String type;

        public WeatherItem(int id, int woeid, int date, int temp, int humidity, int pressure, int wind, String type) {
            this.id = id;
            this.woeid = woeid;
            this.date = date;
            this.temp = temp;
            this.humidity = humidity;
            this.pressure = pressure;
            this.wind = wind;
            this.type = type;
        }
    }

    public static class ForecastItem {
        public int id;
        public int woeid;

        public int date;
        public int lowTemp;
        public int highTemp;
        public String type;

        public ForecastItem(int id, int woeid, int date, int lowTemp, int highTemp, String type) {
            this.id = id;
            this.woeid = woeid;
            this.date = date;
            this.lowTemp = lowTemp;
            this.highTemp = highTemp;
            this.type = type;
        }
    }

    public static class Pair {
        public ArrayList<ForecastItem> forecast;
        public WeatherItem current;
        public Pair(ArrayList<ForecastItem> forecast, WeatherItem current) {
            this.forecast = forecast;
            this.current = current;
        }
    }
}
