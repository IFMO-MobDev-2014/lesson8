package ru.ifmo.md.lesson8;

public class Weather5Days {
    public String city = "";
    public String date = "";
    public String mi = "", ma = "";
    public String type = "";

    public Weather5Days() {

    }

    public Weather5Days(String city, String date, String mi, String ma, String type) {
        this.city = city;
        this.date = date;
        this.mi = mi;
        this.ma = ma;
        this.type = type;
    }
}
