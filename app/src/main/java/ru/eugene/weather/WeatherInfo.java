package ru.eugene.weather;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by eugene on 12/16/14.
 */
public class WeatherInfo {
    private static final String link = "http://weather.yahooapis.com/forecastrss?";

    public static String getLink(int idCity) {
        return link + "u=c&w=" + idCity;
    }

    public static String getLink(int idCity, char tempKind) {
        return link + "u=" + tempKind + "&w=" + idCity;
    }

    public static String getQuery(String nameCity) {
        String query = null;
        try {
            query = URLEncoder.encode("select name, woeid, admin1.content, " +
                    "country.code from geo.places where text=\"" + nameCity + "*\"", "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String getQuery = "q=" + query + "&format=json&env=store://datatables.org/alltableswithkeys";
        return "https://query.yahooapis.com/v1/public/yql?" + getQuery;
    }
}
