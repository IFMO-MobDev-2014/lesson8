package ru.ifmo.md.lesson8;

/**
 * Created by Svet on 30.11.2014.
 */
public class ItemData {
    public String name;
    public String srcToday;
    public String srcTomorrow;
    public String srcAfter;
    public String descriptionToday;
    public String descriptionTomorrow;
    public String descriptionAfter;

    public double temperatureToday;
    public double temperatureTomorrow;
    public double temperatureAfter;

    public double wind;

    public int humidity;
    public double pressure;

    public ItemData() {}

    public ItemData(String name, double temperatureToday, double temperatureTomorrow, double temperatureAfter,
                    double wind, int humidity, int pressure, String srcToday, String srcTomorrow, String srcAfter,
                    String descriptionToday, String descriptionTomorrow, String descriptionAfter) {
        this.name = name;

        this.temperatureToday = temperatureToday;
        this.temperatureTomorrow = temperatureTomorrow;
        this.temperatureAfter = temperatureAfter;

        this.wind = wind;
        this.humidity = humidity;
        this.pressure = pressure;

        this.srcToday = srcToday;
        this.srcTomorrow = srcTomorrow;
        this.srcAfter = srcAfter;

        this.descriptionToday = descriptionToday;
        this.descriptionTomorrow = descriptionTomorrow;
        this.descriptionAfter = descriptionAfter;
    }
}
