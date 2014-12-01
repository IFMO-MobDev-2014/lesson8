package odeen.weatherpredictor;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Женя on 24.11.2014.
 */

public class Parser {

    public static Weather parseWeather(JSONObject object) throws JSONException {
        Weather res = new Weather();
        JSONObject weatherObj = object.getJSONArray("weather").getJSONObject(0);
        res.setMainName(weatherObj.getString("main"));
        res.setDescription(weatherObj.getString("description"));
        res.setIconId(weatherObj.getString("icon"));
        JSONObject mainObj = object.getJSONObject("main");
        res.setTemperature(mainObj.getDouble("temp"));
        res.setMinTemperature(mainObj.getDouble("temp_min"));
        res.setMaxTemperature(mainObj.getDouble("temp_max"));
        res.setHumidity(mainObj.getDouble("humidity"));
        res.setPressure(mainObj.getDouble("pressure"));
        JSONObject windObj = object.getJSONObject("wind");
        res.setWindSpeed(windObj.getDouble("speed"));
        res.setWindDirection(windObj.getDouble("deg"));
        JSONObject cloudsObj = object.getJSONObject("clouds");
        res.setClouds(cloudsObj.getDouble("all"));
        res.setTime(object.getLong("dt"));

        return res;
    }

    public static Weather parseWeatherFC(JSONObject object) throws JSONException {
        Weather res = new Weather();
        res.setTime(object.getLong("dt"));
        res.setTemperature(object.getJSONObject("temp").getDouble("day"));
        res.setPressure(object.getDouble("pressure"));
        res.setHumidity(object.getDouble("humidity"));
        res.setWindSpeed(object.getDouble("speed"));
        res.setIconId(((JSONObject)object.getJSONArray("weather").get(0)).getString("icon"));
        return res;
/*
        JSONObject weatherObj = object.getJSONArray("weather").getJSONObject(0);
        res.setMainName(weatherObj.getString("main"));
        res.setDescription(weatherObj.getString("description"));
        JSONObject mainObj = object.getJSONObject("main");
        res.setTemperature(mainObj.getDouble("temp"));
        res.setMinTemperature(mainObj.getDouble("temp_min"));
        res.setMaxTemperature(mainObj.getDouble("temp_max"));
        JSONObject windObj = object.getJSONObject("wind");
        res.setWindDirection(windObj.getDouble("deg"));
        JSONObject cloudsObj = object.getJSONObject("clouds");
        res.setClouds(cloudsObj.getDouble("all"));

        return res;
        */
    }

    public static ArrayList<Weather> parseDays(JSONArray array) throws JSONException {
        ArrayList<Weather> res = new ArrayList<Weather>();
        for (int i = 0; i < array.length(); i++) {
            Weather now = parseWeatherFC((JSONObject) array.get(i));
            res.add(now);
        }
        return res;
    }
}






/*
{
---"coord":{"lon":30.31,"lat":59.93},
---"sys":{"type":1,"id":7267,"message":0.0951,"country":"Russia","sunrise":1416809927,"sunset":1416834740},
---"weather":[{"id":804,"main":"Clouds","description":"overcast clouds","icon":"04d"}],
---"base":"cmc stations",
"main":{"temp":272.15,"pressure":1035,"humidity":100,"temp_min":272.15,"temp_max":272.15},
"wind":{"speed":2,"deg":180},
"clouds":{"all":90},
"dt":1416812400,
"id":498817,
"name":"Saint Petersburg",
"cod":200
}
 */
