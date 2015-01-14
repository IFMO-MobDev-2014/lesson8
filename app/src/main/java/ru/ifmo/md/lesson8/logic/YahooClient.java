package ru.ifmo.md.lesson8.logic;

/**
 * Created by sergey on 30.11.14.
 */

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class YahooClient {
    public static final String YAHOO_GEO_URL = "http://where.yahooapis.com/v1";
    public static final String YAHOO_WEATHER_URL = "http://weather.yahooapis.com/forecastrss";

    private static final String APPID = "dj0yJmk9ZGd1YzhOd2VWbW9yJmQ9WVdrOWRVSTNkelJETkRJbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD02MA--";

    public static int getWoeidByCoord(double latitude, double longitude) {
        HttpURLConnection yahooHttpConn = null;
        try {
            String location = Double.toString(latitude) + ", " + Double.toString(longitude);
            String query = "select * from geo.placefinder where text=\"" + location + "\" and gflags=\"R\"";
            String strUrl = "https://query.yahooapis.com/v1/public/yql?q=" + URLEncoder.encode(query, "UTF-8") + "&format=xml";

            yahooHttpConn = (HttpURLConnection) (new URL(strUrl)).openConnection();
            yahooHttpConn.connect();

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(yahooHttpConn.getInputStream()));

            String tagName = null;
            String currentTag = null;
            String cityName = null;
            String countryName = null;
            int woeid = 0;

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();
                if (event == XmlPullParser.START_TAG) {
                    currentTag = tagName;
                } else if (event == XmlPullParser.TEXT) {
                    switch (currentTag) {
                        case "city":
                            cityName = parser.getText();
                            break;
                        case "country":
                            countryName = parser.getText();
                            break;
                        case "woeid":
                            woeid = Integer.parseInt(parser.getText());
                            break;
                    }
                }
                event = parser.next();
            }
            return woeid;
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (yahooHttpConn != null) {
                    yahooHttpConn.disconnect();
                }
            } catch (Throwable ignore) {
            }
        }
        return 0;
    }

    public static List<CityFindResult> getCityList(String cityName) {
        List<CityFindResult> result = new ArrayList<>();
        HttpURLConnection yahooHttpConn = null;
        try {
            String query = makeQueryCityURL(cityName);
            //Log.d("Swa", "URL [" + query + "]");
            yahooHttpConn = (HttpURLConnection) (new URL(query)).openConnection();
            yahooHttpConn.connect();
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(yahooHttpConn.getInputStream()));
//            Log.d("Swa", "XML Parser ok");
            int event = parser.getEventType();

            CityFindResult cty = null;
            String tagName = null;
            String currentTag = null;

            // We start parsing the XML
            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("place")) {
                        cty = new CityFindResult();
                    }
                    currentTag = tagName;
                } else if (event == XmlPullParser.TEXT) {
                    if (cty != null) {
                        switch (currentTag) {
                            case "woeid":
                                cty.setWoeid(parser.getText());
                                break;
                            case "name":
                                cty.setCityName(parser.getText());
                                break;
                            case "country":
                                cty.setCountry(parser.getText());
                                break;
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if ("place".equals(tagName))
                        result.add(cty);
                }
                event = parser.next();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (yahooHttpConn != null) {
                    yahooHttpConn.disconnect();
                }
            } catch (Throwable ignore) {
            }
        }
        return result;
    }

    public static CityWeather getWeather(int woeid) {
        HttpURLConnection yahooHttpConn = null;
        CityWeather result = new CityWeather();
        try {
            String query = makeWeatherURL(String.valueOf(woeid), "c");
            yahooHttpConn = (HttpURLConnection) (new URL(query)).openConnection();
            yahooHttpConn.connect();
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(yahooHttpConn.getInputStream()));

            String tagName = null;
            String currentTag = null;

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (event == XmlPullParser.START_TAG) {
                    switch (tagName) {
                        case "yweather:wind":
                            result.wind.chill = Integer.parseInt(parser.getAttributeValue(null, "chill"));
                            result.wind.direction = Integer.parseInt(parser.getAttributeValue(null, "direction"));
                            result.wind.speed = (int) Float.parseFloat(parser.getAttributeValue(null, "speed"));
                            break;
                        case "yweather:atmosphere":
                            result.atmosphere.humidity = Integer.parseInt(parser.getAttributeValue(null, "humidity"));
//                            result.atmosphere.visibility = Float.parseFloat(parser.getAttributeValue(null, "visibility"));
                            result.atmosphere.pressure = Float.parseFloat(parser.getAttributeValue(null, "pressure"));
                            result.atmosphere.rising = Integer.parseInt(parser.getAttributeValue(null, "rising"));
                            break;
                        case "yweather:forecast":
                            String day = parser.getAttributeValue(null, "day");
                            String date = parser.getAttributeValue(null, "date");
                            String description = parser.getAttributeValue(null, "text");
                            int tempMin = Integer.parseInt(parser.getAttributeValue(null, "low"));
                            int tempMax = Integer.parseInt(parser.getAttributeValue(null, "high"));
                            int code = Integer.parseInt(parser.getAttributeValue(null, "code"));
                            result.addForecast(day, date, description, tempMin, tempMax, code);
                            break;
                        case "yweather:condition":
                            result.condition.code = Integer.parseInt(parser.getAttributeValue(null, "code"));
                            result.condition.description = parser.getAttributeValue(null, "text");
                            result.condition.temp = Integer.parseInt(parser.getAttributeValue(null, "temp"));
                            result.condition.date = parser.getAttributeValue(null, "date");
                            break;
                        case "yweather:units":
                            result.units.temperature = "°" + parser.getAttributeValue(null, "temperature");
                            result.units.pressure = parser.getAttributeValue(null, "pressure");
                            result.units.distance = parser.getAttributeValue(null, "distance");
                            result.units.speed = parser.getAttributeValue(null, "speed");
                            break;
                        case "yweather:location":
                            result.location.name = parser.getAttributeValue(null, "city");
                            result.location.region = parser.getAttributeValue(null, "region");
                            result.location.country = parser.getAttributeValue(null, "country");
                            break;
                        case "image":
                            currentTag = "image";
                            break;
                        case "url":
                            if (currentTag == null) {
                                result.imageUrl = parser.getAttributeValue(null, "src");
                            }
                            break;
                        case "lastBuildDate":
                            currentTag = "update";
                            break;
                        case "yweather:astronomy":
                            result.astronomy.sunRise = parser.getAttributeValue(null, "sunrise");
                            result.astronomy.sunSet = parser.getAttributeValue(null, "sunset");
                            break;
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if ("image".equals(currentTag)) {
                        currentTag = null;
                    }
                } else if (event == XmlPullParser.TEXT) {
                    if ("update".equals(currentTag))
                        result.lastUpdate = parser.getText();
                }
                event = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (yahooHttpConn != null) {
                    yahooHttpConn.disconnect();
                }
            } catch (Throwable ignore) {
            }
        }
        return result;
    }

    private static String makeQueryCityURL(String cityName) {
        // We remove spaces in cityName
        cityName = cityName.replaceAll(" ", "%20");
        final int MAX_CITY_RESULT = 10;
        return YAHOO_GEO_URL + "/places.q(" + cityName + "%2A);count=" + MAX_CITY_RESULT + "?appid=" + APPID;
    }

    private static String makeWeatherURL(String woeid, String unit) {
        return YAHOO_WEATHER_URL + "?w=" + woeid + "&u=" + unit;
    }

    public static interface WeatherClientListener {
        public void onWeatherResponse(CityWeather cityWeather);
    }
}
