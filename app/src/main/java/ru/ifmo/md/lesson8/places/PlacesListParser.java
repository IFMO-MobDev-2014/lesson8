package ru.ifmo.md.lesson8.places;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class PlacesListParser extends DefaultHandler {
    public static List<Place> parse(URL url) {
        try {
            InputStream stream = url.openStream();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            PlacesListParser handler = new PlacesListParser();
            parser.parse(stream, handler);
            return handler.places;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e.getMessage()); // TODO: No nice
        }
    }

    private final List<Place> places = new ArrayList<>();
    private final Place.Builder placeBuilder = new Place.Builder();
    private StringBuilder textBuilder;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("place")) {
            placeBuilder.clean();
        }

        textBuilder = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        switch (qName) {
            case "country":
                placeBuilder.setCountry(textBuilder.toString());
                break;
            case "woeid":
                placeBuilder.setWoeid(Long.parseLong(textBuilder.toString()));
            case "name":
                placeBuilder.setName(textBuilder.toString());
                break;
            case "place":
                places.add(placeBuilder.createPlace());
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        textBuilder.append(ch, start, length);
    }
}
