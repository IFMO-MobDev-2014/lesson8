package ru.ifmo.md.lesson8;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mikhail on 16.01.15.
 */
public class YahooClient {
    public static String YAHOO_GEO_URL = "http://where.yahooapis.com/v1";
    private static String APPID = "dj0yJmk9TWV6OWlZUDZjakxjJmQ9WVdrOVZUSXpjMWRpTjJVbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD1mMg--";

    public static List<City> getCityList(String cityName) {
        List<City> result = new ArrayList<City>();
        HttpURLConnection yahooHttpConn = null;
        try {
            String cName = new String();
            String query = makeQueryCityURL(cityName);
            yahooHttpConn= (HttpURLConnection) (new URL(query)).openConnection();
            yahooHttpConn.connect();
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(yahooHttpConn.getInputStream()));
            int event = parser.getEventType();

            City cty = null;
            String tagName = null;
            String currentTag = null;

            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("place")) {
                        cty = new City();
                    }
                    currentTag = tagName;
                }
                else if (event == XmlPullParser.TEXT) {
                    if ("woeid".equals(currentTag))
                        cty.setWoeid(parser.getText());
                    else if ("name".equals(currentTag))
                        cName = parser.getText();
                    else if ("country".equals(currentTag))
                        cty.setCityName(cName + ", " + parser.getText());
                }
                else if (event == XmlPullParser.END_TAG) {
                    if ("place".equals(tagName))
                        result.add(cty);
                }

                event = parser.next();
            }
        }
        catch(Throwable t) {
            t.printStackTrace();
            // Log.e("Error in getCityList", t.getMessage());
        }
        finally {
            try {
                yahooHttpConn.disconnect();
            }
            catch(Throwable ignore) {}

        }
        return result;
    }

    private static String makeQueryCityURL(String cityName) {
        // We remove spaces in cityName
        cityName = cityName.replaceAll(" ", "%20");
        return YAHOO_GEO_URL + "/places.q(" + cityName + "%2A);count=" + 10 + "?appid=" + APPID;
    }
}
