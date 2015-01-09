package ru.ifmo.md.lesson8;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by izban on 09.01.15.
 */
public class Item {
    String code;
    Date date;
    String day;
    int high;
    int low;

    Item() {}

    Item(JSONObject object) throws JSONException {
        code = object.getString("code");
        date = new SimpleDateFormat("dd MMM yyyy").parse(object.getString("date"), new ParsePosition(0));
        day = object.getString("day");
        high = (int)((object.getInt("high") - 32) / 1.8);
        low = (int)((object.getInt("low") - 32) / 1.8);
    }

    public String toString() {
        return "code: " + code + ", date: " + date + ", day: " + day + ", high: " + high + ", low: " + low;
    }
}
