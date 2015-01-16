package ru.eugene.weather.database;

/**
 * Created by eugene on 12/16/14.
 */
public class CityItem {
    private int id;
    private String city;
    private String link;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
