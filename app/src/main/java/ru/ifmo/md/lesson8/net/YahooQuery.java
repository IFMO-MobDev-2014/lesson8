package ru.ifmo.md.lesson8.net;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import ru.ifmo.md.lesson8.places.Place;
import ru.ifmo.md.lesson8.places.PlacesListHandler;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class YahooQuery {
    public static URL query(String sql) {
        try {
            String encodedSql = URLEncoder.encode(sql, "UTF-8");
            String basePart = "https://query.yahooapis.com/v1/public/yql?q=";
            return new URL(basePart + encodedSql);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new RuntimeException(e.getMessage()); // TODO: Fix
        }
    }

    public static List<Place> findPlace(String name) {
        return PlacesListHandler.parse(query("select name,country,woeid from geo.places where text=\"*"
                + name + "*\""));
    }
}
