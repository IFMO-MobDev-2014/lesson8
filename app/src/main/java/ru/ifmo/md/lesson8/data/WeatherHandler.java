package ru.ifmo.md.lesson8.data;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariashka on 11/28/14.
 */
public class WeatherHandler extends DefaultHandler {

    private List<WeatherItem> items;
    private WeatherItem currentItem;
    private StringBuilder content;
    private boolean inItem = false;
    private boolean inHourly = false;
    private int currT;
    private int feel;
    private String currC;

    public WeatherHandler() {
        items = new ArrayList();
    }

    public List<WeatherItem> getItems() {
        return items;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        content = new StringBuilder();
        if (localName.equalsIgnoreCase("weather")) {
            inItem = true;
            currentItem = new WeatherItem();
            currentItem.setCurrT(currT);
            currentItem.setCondition(currC);
            currentItem.setFeels(feel);
        }
        if (localName.equalsIgnoreCase("hourly")) {
            inHourly = true;
        }
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equalsIgnoreCase("date")) {
            if (inItem) {
                currentItem.setDate(content.toString());
            }
        } else if (localName.equalsIgnoreCase("FeelsLikeC")) {
            String s = content.toString();
            feel = Integer.parseInt(s);
        } else if (localName.equalsIgnoreCase("temp_C")) {
            String s = content.toString();
            currT = Integer.parseInt(s);
        } else if (localName.equalsIgnoreCase("maxtempC")) {
            if (inItem) {
                String s = content.toString();
                int i = Integer.parseInt(s);
                currentItem.setMax(i);
            }
        } else if (localName.equalsIgnoreCase("mintempC")) {
            if (inItem) {
                String s = content.toString();
                int i = Integer.parseInt(s);
                currentItem.setMin(i);
            }
        } else if (localName.equalsIgnoreCase("weatherIconUrl")) {
            if (inHourly) {
                currentItem.addHourlyC(content.toString());
            }  else {
                currC = content.toString();
            }
        } else if (localName.equalsIgnoreCase("tempC")) {
            if (inHourly) {
                currentItem.addHourlyT(Integer.parseInt(content.toString()));
            }
        } else if (localName.equalsIgnoreCase("hourly")) {
            inHourly = false;
        } else if (localName.equalsIgnoreCase("weather")) {
            inItem = false;
            items.add(currentItem);
            currentItem = new WeatherItem();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }
}
