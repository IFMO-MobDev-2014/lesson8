package ru.ifmo.md.lesson8;

/**
 * Created by MSviridenkov on 30.11.2014.
 */
public class City {
    private String cityName;
    private String woeid;
    private String id;

    City(String cityName, String woeid) {
        this.cityName = cityName;
        this.woeid = woeid;
    }

    City(City city) {
        this.cityName = city.getCityName();
        this.woeid = city.getWoeid();
    }

    City() {

    }

    public void cutName() {
        this.cityName = this.cityName.split(",")[0];
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setWoeid(String woeid) {
        this.woeid = woeid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCityName() {
        return this.cityName;
    }

    public String getWoeid() {
        return this.woeid;
    }

    public String getId() {
        return this.id;
    }
}
