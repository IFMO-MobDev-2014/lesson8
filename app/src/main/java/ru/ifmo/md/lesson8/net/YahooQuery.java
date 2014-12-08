package ru.ifmo.md.lesson8.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import ru.ifmo.md.lesson8.places.Place;
import ru.ifmo.md.lesson8.places.PlacesListParser;
import ru.ifmo.md.lesson8.weather.Forecast;
import ru.ifmo.md.lesson8.weather.ForecastParser;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class YahooQuery {
    public static URL query(String sql) throws MalformedURLException {
        try {
            String encodedSql = URLEncoder.encode(sql, "UTF-8");
            String basePart = "https://query.yahooapis.com/v1/public/yql?q=";
            return new URL(basePart + encodedSql);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage()); // Should not happen
        }
    }

    public static List<Place> findPlace(String name) throws IOException {
        return PlacesListParser.parse(query("select name,country,woeid from geo.places where text=\"*"
                + name + "*\""));
    }

    public static List<Forecast> fetchWeatherInPlace(Place place) throws IOException {
        return ForecastParser.parse(query("select * from weather.forecast where woeid=" + place.getWoeid()));
    }
}
