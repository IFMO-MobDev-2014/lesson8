package freemahn.com.lesson8;

import org.json.JSONObject;

/**
 * Created by Freemahn on 29.11.2014.
 */
public class Item {
    int code;
    String date;
    String day;

    String text;
    int temp = 273;
    int high = 273;

    public Item() {

    }

    public Item(JSONObject o) {
        try {
            code = o.getInt("code");

            if (o.has("high"))
                high = o.getInt("high");
            if (o.has("text"))
                text = o.getString("text");
            if (o.has("date"))
                date = o.getString("date");

            if (o.has("day"))
                day = o.getString("day");
            if (o.has("temp"))
                temp = o.getInt("temp");
            if (o.has("low"))
                temp = o.getInt("low");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public String toString() {
        return "{" + code + " " + date + " " + text + " " + temp + " " + high + "}";
    }
}
