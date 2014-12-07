package ru.ifmo.md.lesson8.weather;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ru.ifmo.md.lesson8.content.ContentHelper;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class ForecastParser extends DefaultHandler {

    public static List<Forecast> parse(URL url) {
        try {
            InputStream stream = url.openStream();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ForecastParser handler = new ForecastParser();
            parser.parse(stream, handler);
            return handler.getForecasts();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e.getMessage()); // TODO: Remove this
        }
    }

    private final HashMap<Date, Weather.Builder> weather = new HashMap<>();

    private ForecastParser() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("yweather:forecast")) {
            Date date = parseDate(attributes.getValue("date"));

            int low = Integer.parseInt(attributes.getValue("low"));
            int high = Integer.parseInt(attributes.getValue("high"));

            Weather.Builder builder = getBuilder(date);

            String description = attributes.getValue("text");
            builder.setLow(new Temperature(low, Temperature.fahrenheit()))
                    .setDescription(description);
            builder.setHigh(new Temperature(high, Temperature.fahrenheit()))
                    .setDescription(description);
        } else if (qName.equals("yweather:condition")) {
            Date date = ContentHelper.getCurrentDate();
            Weather.Builder builder = getBuilder(date);
            int temp = Integer.parseInt(attributes.getValue("temp"));
            String desc = attributes.getValue("text");

            builder.setCurrent(new Temperature(temp, Temperature.fahrenheit()))
                    .setDescription(desc);
        }
    }

    private Weather.Builder getBuilder(Date date) {
        Weather.Builder builder = weather.get(date);
        if (builder == null) {
            builder = new Weather.Builder();
            weather.put(date, builder);
        }

        return builder;
    }

    private static Date parseDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Date parsedDate;
        try {
            parsedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage()); // TODO: Remove this
        }
        return parsedDate;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    public List<Forecast> getForecasts() {
        List<Forecast> forecasts = new ArrayList<>();
        for (Map.Entry<Date, Weather.Builder> entry : weather.entrySet()) {
            forecasts.add(new Forecast(entry.getKey(), entry.getValue().createWeather()));
        }

        return forecasts;
    }
}
