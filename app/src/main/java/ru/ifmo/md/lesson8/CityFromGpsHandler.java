package ru.ifmo.md.lesson8;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by Mikhail on 16.01.15.
 */
public class CityFromGpsHandler extends DefaultHandler {
    private final StringBuilder characters = new StringBuilder();
    private City city = new City();
    private String cityName;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        final String content = characters.toString().trim();

        if (localName.equalsIgnoreCase("neighborhood")) {
            city.setCityName(content);
        } else if (localName.equalsIgnoreCase("woeid")) {
            city.setWoeid(content);
        }

        characters.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        characters.append(new String(ch, start, length));
    }

    public City getCity() {
        return city;
    }
}
