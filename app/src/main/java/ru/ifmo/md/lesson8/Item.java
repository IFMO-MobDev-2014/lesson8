package ru.ifmo.md.lesson8;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by izban on 09.01.15.
 */
public class Item {
    String city;
    String code;
    String date;
    String day;
    String high;
    String low;

    Item() {}

    Item(String city, String code, String date, String day, String high, String low) {
        this.city = city;
        this.code = code;
        this.date = date;
        this.day = day;
        this.high = high;
        this.low = low;
    }

    Item(JSONObject object) throws JSONException {
        code = object.getString("code");
        date = object.getString("date");
        //date = new SimpleDateFormat("dd MMM yyyy").parse(object.getString("date"), new ParsePosition(0));
        day = object.getString("day");
        high = Integer.toString((int)((object.getInt("high") - 32) / 1.8));
        low = Integer.toString((int)((object.getInt("low") - 32) / 1.8));
    }

    public String toString() {
        return "code: " + code + ", date: " + date + ", day: " + day + ", high: " + high + ", low: " + low;
    }

    public ContentValues getContentValues() {
        ContentValues res = new ContentValues();
        res.put("city", city);
        res.put("code", code);
        res.put("date", date);
        res.put("day", day);
        res.put("high", high);
        res.put("low", low);
        return res;
    }
}
