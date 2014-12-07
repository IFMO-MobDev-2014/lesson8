package ru.ifmo.md.lesson8.weather;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class WeatherHandler extends DefaultHandler {

    public static Weather parse(URL url) {
        try {
            InputStream stream = url.openStream();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            WeatherHandler handler = new WeatherHandler();
            parser.parse(stream, handler);
            return handler.getWeather();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e.getMessage()); // TODO: Remove this
        }
    }

    private final Weather.Builder builder = new Weather.Builder();

    private WeatherHandler() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("yweather:condition")) {
            int temp = Integer.parseInt(attributes.getValue("temp"));
            builder.setTemperature(new Temperature(temp, Temperature.fahrenheit()));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    public Weather getWeather() {
        return builder.createWeather();
    }
}
