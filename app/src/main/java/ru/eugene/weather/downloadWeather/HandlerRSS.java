package ru.eugene.weather.downloadWeather;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import ru.eugene.weather.database.WeatherItem;

/**
 * Created by eugene on 11/4/14.
 */
public class HandlerRSS extends DefaultHandler {
    ArrayList<WeatherItem> weatherItems = new ArrayList<>();
    private String textPubDate = "";
    private boolean pubDate = false;
    private WeatherItem weatherItem = new WeatherItem();
    private boolean isFirstForecast = true;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (pubDate) {
            textPubDate += new String(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("lastBuildDate")) {
            if (pubDate) {
                pubDate = false;
                weatherItem.setPubDate(textPubDate);
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("lastBuildDate")) {
            pubDate = true;
        } else if (qName.equalsIgnoreCase("yweather:wind")) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getValue(i).isEmpty())
                    continue;
                if (attributes.getLocalName(i).equalsIgnoreCase("chill")) {
                    weatherItem.setChill(Integer.parseInt(attributes.getValue(i)));
                } else if (attributes.getLocalName(i).equalsIgnoreCase("speed")) {
                    weatherItem.setSpeed(Double.parseDouble(attributes.getValue(i)));
                }
            }
        } else if (qName.equalsIgnoreCase("yweather:atmosphere")) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getValue(i).isEmpty())
                    continue;
                if (attributes.getLocalName(i).equalsIgnoreCase("humidity")) {
                    weatherItem.setHumidity(Integer.parseInt(attributes.getValue(i)));
                } else if (attributes.getLocalName(i).equalsIgnoreCase("visibility")) {
                    weatherItem.setVisibility(Double.parseDouble(attributes.getValue(i)));
                }
            }
        } else if (qName.equalsIgnoreCase("yweather:condition")) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getValue(i).isEmpty())
                    continue;
                if (attributes.getLocalName(i).equalsIgnoreCase("text")) {
                    weatherItem.setText(attributes.getValue(i));
                } else if (attributes.getLocalName(i).equalsIgnoreCase("temp")) {
                    weatherItem.setTemp(Integer.parseInt(attributes.getValue(i)));
                } else if (attributes.getLocalName(i).equalsIgnoreCase("code")) {
                    weatherItem.setCode(Integer.parseInt(attributes.getValue(i)));
                }
            }
        } else if (qName.equalsIgnoreCase("yweather:forecast")) {
            WeatherItem dayItem = new WeatherItem();
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getValue(i).isEmpty())
                    continue;
                if (attributes.getLocalName(i).equalsIgnoreCase("text")) {
                    dayItem.setText(attributes.getValue(i));
                } else if (attributes.getLocalName(i).equalsIgnoreCase("low")) {
                    dayItem.setTempMin(Integer.parseInt(attributes.getValue(i)));
                } else if (attributes.getLocalName(i).equalsIgnoreCase("high")) {
                    dayItem.setTempMax(Integer.parseInt(attributes.getValue(i)));
                } else if (attributes.getLocalName(i).equalsIgnoreCase("code")) {
                    dayItem.setCode(Integer.parseInt(attributes.getValue(i)));
                } else if (attributes.getLocalName(i).equalsIgnoreCase("day")) {
                    dayItem.setPubDate(attributes.getValue(i));
                }
            }

            //first forecast matches with today weather
            if (isFirstForecast) {
                weatherItem.setTempMin(dayItem.getTempMin());
                weatherItem.setTempMax(dayItem.getTempMax());
                weatherItem.setPubDate(dayItem.getPubDate());
                isFirstForecast = false;
                dayItem = weatherItem;
            }
            weatherItems.add(dayItem);
        }
    }

    public ArrayList<WeatherItem> getWeatherItems() {
        return weatherItems;
    }
}

