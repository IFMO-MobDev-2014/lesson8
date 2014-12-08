package weathertogo.sergeybudkov.ru.weathertogo;

import android.content.ContentValues;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


//Sergey Budkov 2536

public class WeatherParser extends DefaultHandler {
    private boolean isFact = false;
    private boolean isDay = false;

    private static String data = "";
    private String day_part = "";
    private String temperature_avg = "";
    private String weather_type = "";
    private String wind_direction = "";
    private String wind_speed = "";
    private String pressure = "";
    private String humidity = "";

    private boolean isData = false;
    private boolean isDayPart = false;
    private boolean isTemperatureAvg = false;
    private boolean isWeatherType = false;
    private boolean isWindDirection = false;
    private boolean isWindSpeed = false;
    private boolean isPressure = false;
    private boolean isHumidity = false;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);

        if (name.equalsIgnoreCase("FACT")) {
            isFact = true;
            day_part = MainActivity.NOW;
        }

        if (name.equalsIgnoreCase("DAY")) {
            isDay = true;
            for (int ind = 0; ind < attributes.getLength(); ++ind) {
                String attName = attributes.getQName(ind);
                if (attName.equals("date")) {
                    data = attributes.getValue(attName);
                }
            }
        }

        if (name.equalsIgnoreCase("DAY_PART")) {
            isDayPart = true;
            for (int ind = 0; ind < attributes.getLength(); ++ind) {
                String attName = attributes.getQName(ind);
                if (attName.equals("type")) {
                    day_part = attributes.getValue(attName);
                }
            }
        }


        if (name.equalsIgnoreCase("OBSERVATION_TIME")) {
            isData = true;
            data = "";
        }

        if (isFact == true && name.equalsIgnoreCase("TEMPERATURE") || isDayPart == true && name.equalsIgnoreCase("AVG")) {
            isTemperatureAvg = true;
            temperature_avg = "";
        }

        if (name.equalsIgnoreCase("WEATHER_TYPE")) {
            isWeatherType = true;
            weather_type = "";
        }
        if (name.equalsIgnoreCase("WIND_DIRECTION")) {
            isWindDirection = true;
            wind_direction = "";
        }
        if (name.equalsIgnoreCase("WIND_SPEED")) {
            isWindSpeed = true;
            wind_speed = "";
        }
        if (name.equalsIgnoreCase("HUMIDITY")) {
            isHumidity = true;
            humidity = "";
        }
        if (name.equalsIgnoreCase("PRESSURE")) {
            isPressure = true;
            pressure = "";
        }
    }

    @Override
    public void endElement(String uri, String localName,
                           String name) throws SAXException {
        if (name.equalsIgnoreCase("FACT") || name.equalsIgnoreCase("DAY_PART")) {
            isFact = false;
            isDayPart = false;
            isDay = false;
            ContentValues values = new ContentValues();
            values.put(WeatherDataBase.CITY, WeatherIntent.city);
            values.put(WeatherDataBase.COUNTRY, WeatherIntent.country);
            values.put(WeatherDataBase.YANDEX_ID, WeatherIntent.yandex_id);
            values.put(WeatherDataBase.TEMPERATURE, temperature_avg);
            values.put(WeatherDataBase.DESCRIPTION, weather_type);
            values.put(WeatherDataBase.PRESSURE, pressure);
            values.put(WeatherDataBase.WIND_DIRECTION, wind_direction);
            values.put(WeatherDataBase.WIND_SPEED, wind_speed);
            values.put(WeatherDataBase.DAY_PART, day_part);
            values.put(WeatherDataBase.DATA, data);
            values.put(WeatherDataBase.HUMIDITY, humidity);
            MainActivity.wBase.insertWeather(WeatherIntent.city, WeatherIntent.country, WeatherIntent.yandex_id, temperature_avg, weather_type,
                    pressure, wind_direction, wind_speed, day_part, data, humidity);
        }
        if (name.equalsIgnoreCase("OBSERVATION_TIME")) {
            isData = false;
        }
        if (isFact == true && name.equalsIgnoreCase("TEMPERATURE") || isDayPart == true && name.equalsIgnoreCase("AVG")) {
            isTemperatureAvg = false;
        }
        if (name.equalsIgnoreCase("WEATHER_TYPE")) {
            isWeatherType = false;
        }
        if (name.equalsIgnoreCase("WIND_DIRECTION")) {
            isWindDirection = false;
        }
        if (name.equalsIgnoreCase("WIND_SPEED")) {
            isWindSpeed = false;
        }
        if (name.equalsIgnoreCase("HUMIDITY")) {
            isHumidity = false;
        }
        if (name.equalsIgnoreCase("PRESSURE")) {
            isPressure = false;
        }
    }

    @Override
    public void characters(char chars[], int start, int length) throws SAXException {
        if (isTemperatureAvg == true) {
            temperature_avg += new String(chars, start, length);
        }
        if (isWeatherType == true) {
            weather_type += new String(chars, start, length);
        }
        if (isWindDirection == true) {
            wind_direction += new String(chars, start, length);
        }
        if (isWindSpeed == true) {
            wind_speed += new String(chars, start, length);
        }
        if (isHumidity == true) {
            humidity += new String(chars, start, length);
        }
        if (isPressure == true) {
            pressure += new String(chars, start, length);
        }
        if (isData == true) {
            data += new String(chars, start, length);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
}