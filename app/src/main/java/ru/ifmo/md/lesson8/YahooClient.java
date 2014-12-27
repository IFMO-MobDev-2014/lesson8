package ru.ifmo.md.lesson8;

/**
 * Created by sergey on 30.11.14.
 */

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YahooClient {
    public static final String YAHOO_GEO_URL = "http://where.yahooapis.com/v1";
    public static final String YAHOO_WEATHER_URL = "http://weather.yahooapis.com/forecastrss";

    private static final String APPID = "dj0yJmk9ZGd1YzhOd2VWbW9yJmQ9WVdrOWRVSTNkelJETkRJbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD02MA--";

    public static List<CityResult> getCityList(String cityName) {
        List<CityResult> result = new ArrayList<>();
        HttpURLConnection yahooHttpConn = null;
        try {
            String query = makeQueryCityURL(cityName);
            //Log.d("Swa", "URL [" + query + "]");
            yahooHttpConn = (HttpURLConnection) (new URL(query)).openConnection();
            yahooHttpConn.connect();
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(yahooHttpConn.getInputStream()));
            Log.d("Swa", "XML Parser ok");
            int event = parser.getEventType();

            CityResult cty = null;
            String tagName = null;
            String currentTag = null;

            // We start parsing the XML
            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("place")) {
                        // place Tag Found so we create a new CityResult
                        cty = new CityResult();
                        //  Log.d("Swa", "New City found");
                    }
                    currentTag = tagName;
                    // Log.d("Swa", "Tag ["+tagName+"]");
                } else if (event == XmlPullParser.TEXT) {
                    // We found some text. let's see the tagName to know the tag related to the text
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
                    // We don't want to analyze other tag at the moment
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

    public static void getWeather(String woeid, String unit, RequestQueue rq, final WeatherClientListener listener) {
        String url2Call = makeWeatherURL(woeid, unit);
        final CityWeather result = new CityWeather();
        StringRequest req = new StringRequest(Request.Method.GET, url2Call, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                parseResponse(s, result);
                listener.onWeatherResponse(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        rq.add(req);
    }

    private static CityWeather parseResponse(String resp, CityWeather result) {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(resp));

            String tagName = null;
            String currentTag = null;

            int event = parser.getEventType();
            boolean isFirstDayForecast = true;
            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("yweather:wind")) {
                        result.wind.chill = Integer.parseInt(parser.getAttributeValue(null, "chill"));
                        result.wind.direction = Integer.parseInt(parser.getAttributeValue(null, "direction"));
                        result.wind.speed = (int) Float.parseFloat(parser.getAttributeValue(null, "speed"));
                    } else if (tagName.equals("yweather:atmosphere")) {
                        result.atmosphere.humidity = Integer.parseInt(parser.getAttributeValue(null, "humidity"));
                        result.atmosphere.visibility = Float.parseFloat(parser.getAttributeValue(null, "visibility"));
                        result.atmosphere.pressure = Float.parseFloat(parser.getAttributeValue(null, "pressure"));
                        result.atmosphere.rising = Integer.parseInt(parser.getAttributeValue(null, "rising"));
                    } else if (tagName.equals("yweather:forecast")) {
                        String day = parser.getAttributeValue(null, "day");
                        String date = parser.getAttributeValue(null, "date");
                        String description = parser.getAttributeValue(null, "text");
                        int tempMin = Integer.parseInt(parser.getAttributeValue(null, "low"));
                        int tempMax = Integer.parseInt(parser.getAttributeValue(null, "high"));
                        int code = Integer.parseInt(parser.getAttributeValue(null, "code"));
                        result.addForecast(day, date, description, tempMin, tempMax, code);
                    } else if (tagName.equals("yweather:condition")) {
                        result.condition.code = Integer.parseInt(parser.getAttributeValue(null, "code"));
                        result.condition.description = parser.getAttributeValue(null, "text");
                        result.condition.temp = Integer.parseInt(parser.getAttributeValue(null, "temp"));
                        result.condition.date = parser.getAttributeValue(null, "date");
                    } else if (tagName.equals("yweather:units")) {
                        result.units.temperature = "°" + parser.getAttributeValue(null, "temperature");
                        result.units.pressure = parser.getAttributeValue(null, "pressure");
                        result.units.distance = parser.getAttributeValue(null, "distance");
                        result.units.speed = parser.getAttributeValue(null, "speed");
                    } else if (tagName.equals("yweather:location")) {
                        result.location.name = parser.getAttributeValue(null, "city");
                        result.location.region = parser.getAttributeValue(null, "region");
                        result.location.country = parser.getAttributeValue(null, "country");
                    } else if (tagName.equals("image"))
                        currentTag = "image";
                    else if (tagName.equals("url")) {
                        if (currentTag == null) {
                            result.imageUrl = parser.getAttributeValue(null, "src");
                        }
                    } else if (tagName.equals("lastBuildDate")) {
                        currentTag = "update";
                    } else if (tagName.equals("yweather:astronomy")) {
                        result.astronomy.sunRise = parser.getAttributeValue(null, "sunrise");
                        result.astronomy.sunSet = parser.getAttributeValue(null, "sunset");
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
        } catch (Throwable t) {
            t.printStackTrace();
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
