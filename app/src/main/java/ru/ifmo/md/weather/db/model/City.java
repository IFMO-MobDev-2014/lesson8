package ru.ifmo.md.weather.db.model;

/**
 * Created by Kirill on 01.12.2014.
 */
public class City {

    private String name;
    private String id;
    private String country;
    private int dbId;
    private Weather weatherNow;
    double lon;
    double lat;

    public City() {
        this.lon = 0;
        this.lat = 0;
    }

    public City(String name, String id, String country, double lon, double lat, Weather weatherNow, int dbId) {
        this.name = name;
        this.id = id;
        this.country = country;
        this.lon = lon;
        this.lat = lat;
        this.weatherNow = weatherNow;
        this.dbId = dbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Weather getWeatherNow() {
        return weatherNow;
    }

    public void setWeatherNow(Weather weatherNow) {
        this.weatherNow = weatherNow;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", country='" + country + '\'' +
                ", lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}
