package ru.ifmo.md.lesson8;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by MSviridenkov on 30.11.2014.
 */
public class WeatherHandler extends DefaultHandler {
    private Weather weather = new Weather();
    private Now now = new Now();
    private Day day;
    private final StringBuilder characters = new StringBuilder();
    private int count = 0;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("wind")) {
            now.setWindSpeed(attributes.getValue("speed"));
        } else if (localName.equals("atmosphere")) {
            now.setHumidity(attributes.getValue("humidity"));
        } else if (localName.equals("condition")) {
            now.setConditionCode(attributes.getValue("code"));
            now.setTemp(attributes.getValue("temp"));
            now.setCondition(attributes.getValue("text"));
        } else if (localName.equals("forecast")) {
            day = new Day();
            day.setName(attributes.getValue("day"));
            day.setForecast(makeForecast(attributes.getValue("low"), attributes.getValue("high")));
            day.setConditionCode(attributes.getValue("code"));

            if (count == 0) {
                weather.setFirstDay(day);
                count++;
            } else if (count == 1) {
                weather.setSecondDay(day);
                count++;
            } else if (count == 2) {
                weather.setThirdDay(day);
                count++;
            } else if (count == 3) {
                weather.setFourthDay(day);
                count++;
            } else if (count == 4) {
                weather.setFifthDay(day);
                count++;
            }
        }
    }

    private String makeForecast(String low, String high) {
        return low + "\u00b0" + " / " + high + "\u00b0";
    }

    public Weather getWeather() {
        /*Log.i("NOW!!!",
                now.getCondition() + " " +
                now.getTemp() + " " +
                now.getWindSpeed() + " " +
                now.getHumidity());*/
        weather.setNow(now);
        return weather;
    }
}
