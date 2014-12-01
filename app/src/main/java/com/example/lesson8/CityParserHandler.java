package com.example.lesson8;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class CityParserHandler extends DefaultHandler {

    private ArrayList<City> result;
    private City city;
    private String currentQname;

    @Override
    public void startDocument() throws SAXException {
        result = new ArrayList<City>();
        city = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("city")) {
            city = new City();
        }
        currentQname = qName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String builder = new String(ch, start, length);
        if (currentQname.equals("name"))
            if (city.getCity() == null)
                city.setCity(builder);
        if (currentQname.equals("country"))
            if (city.getCountry() == null)
                city.setCountry(builder);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("city"))
            result.add(city);
    }

    ArrayList<City> getResult() {
        return result;
    }
}
