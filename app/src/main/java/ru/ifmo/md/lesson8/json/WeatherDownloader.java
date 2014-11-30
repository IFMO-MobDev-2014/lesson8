package ru.ifmo.md.lesson8.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dimatomp on 30.11.14.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class WeatherDownloader {
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    public static <T> T downloadFromUrl(String url, Class<T> datatype) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            return mapper.readValue(connection.getInputStream(), datatype);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
