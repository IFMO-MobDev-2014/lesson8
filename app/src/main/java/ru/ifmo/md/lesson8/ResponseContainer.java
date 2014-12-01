package ru.ifmo.md.lesson8;

import java.util.List;

/**
 * Created by Евгения on 29.11.2014.
 */
public class ResponseContainer {
    Query query;
}

class Query{
  Results results;
}

class Results {
    Weather weather;
}

class Weather{
    RSS rss;
}

class RSS {
    Channel channel;
}

class Channel {
    WeatherItem item;
    Wind wind;
    Atmosphere atmosphere;
    Location location;
}

class Location{
    String city;
}

class Wind{
    int direction;
    double speed;
}

class Atmosphere{
   int humidity;
    double pressure;
}

class WeatherItem {
    List<ForecastItem> forecast;
    Condition condition;
}

class Condition{
    int code;
    String date;
    int temp;
    String text;
}
class ForecastItem {
    int code;
    String date;
    String day;
    int high;
    int low;
    String text;
}