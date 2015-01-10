package ru.ifmo.md.lesson8;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by izban on 09.01.15.
 */
public class Item {
    String city;
    String code;
    long date;
    String day;
    String high;
    String low;
    String text;

    Item() {}

    Item(String city, String code, String date, String day, String high, String low, String text) {
        this.city = city;
        this.code = code;
        this.date = Long.parseLong(date);
        this.day = day;
        this.high = high;
        this.low = low;
        this.text = text;
    }

    Item(JSONObject object) throws JSONException {
        city = object.getString("city");
        code = object.getString("code");
        date = new SimpleDateFormat("ddMMMyyyy").parse(object.getString("date"), new ParsePosition(0)).getTime();
        day = object.getString("day");
        high = Integer.toString((int)((object.getInt("high") - 32) / 1.8));
        low = Integer.toString((int)((object.getInt("low") - 32) / 1.8));
        text = object.getString("text");
    }

    public String toString() {
        return new SimpleDateFormat("EEE, dd MMM yyyy").format(new Date(date)) + "\n" + "between " + low + " and " + high + "\n";
    }

    public ContentValues getContentValues() {
        ContentValues res = new ContentValues();
        res.put("city", city);
        res.put("code", code);
        res.put("date", date);
        res.put("day", day);
        res.put("high", high);
        res.put("low", low);
        res.put("text", text);
        return res;
    }
}
