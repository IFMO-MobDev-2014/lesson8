package ru.ifmo.md.lesson8;

import net.aksingh.java.api.owm.DailyForecastData;

import java.util.Date;

/**
 * Created by pva701 on 23.11.14.
 */
public class ShortWeatherData {
    public static final float T = (float)0;

    private int tempMin;
    private int tempMax;
    private int temp;
    private int windSpeed;
    private int humidity;
    private String weatherMain;
    private String weatherDescription;
    private String icon;
    private Date date;
    private int conditionCode;
    private int cityId;

    public ShortWeatherData(DailyForecastData.Forecast wd, int cityId) {
        tempMin = (int)(wd.getTemperature_Object().getMinimumTemperature() - T);
        tempMax = (int)(wd.getTemperature_Object().getMaximumTemperature() - T);
        temp = (int)(wd.getTemperature_Object().getDayTemperature() - T);
        windSpeed = (int)wd.getWindSpeed();
        humidity = (int)wd.getHumidity();
        weatherMain = wd.getWeather_List().get(0).getWeatherName();
        weatherDescription = wd.getWeather_List().get(0).getWeatherDescription();
        icon = wd.getWeather_List().get(0).getWeatherIconName();
        date = wd.getDateTime();
        conditionCode = wd.getWeather_List().get(0).getWeatherCode();
        this.cityId = cityId;
    }

    public ShortWeatherData(int tmn, int tmx, int tm, int ws,
                            int hu, String wmain, String wdes, String ic, int dt,
                            int condCode, int cid) {
        tempMin = tmn;
        tempMax = tmx;
        temp = tm;
        windSpeed = ws;
        humidity = hu;
        weatherMain = wmain;
        weatherDescription = wdes;
        icon = ic;
        date = new Date(dt * 1000L);
        conditionCode = condCode;
        cityId = cid;
    }

    public int getTempMin() {
        return tempMin;
    }

    public int getTempMax() {
        return tempMax;
    }

    public int getTemp() {
        return temp;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getWeatherMain() {
        return weatherMain;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public String getIcon() {
        return icon;
    }

    public Date getDate() {
        return date;
    }

    public int getCityId() {
        return cityId;
    }

    public int getConditionCode() {
        return conditionCode;
    }
}
