package lesson8.md.ifmo.ru.weather;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

// takes query string & searches for location with similar name via http://autocomplete.wunderground.com/
// returns array list of complete cities name & zmw's of cities or null if error occurred
public class CityNameLoader extends android.support.v4.content.AsyncTaskLoader<Object> {
    public final String SEARCH_URL = "http://autocomplete.wunderground.com/aq?query=";
    String query;

    public CityNameLoader(Context c, String query) {
        super(c);

        this.query = query;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<Pair<String>> loadInBackground() {
        JSONObject data = null;

        // h=0: don't search among hurricanes
        URL url = null;
        try {
            url = new URL(SEARCH_URL + query + "&h=0");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            data = new JSONObject(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        ArrayList<Pair<String>> result = new ArrayList<>();
        try {
            JSONArray resultArray = data.getJSONArray("RESULTS");
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject row = resultArray.getJSONObject(i);
                result.add(new Pair<String>(row.getString("name"), row.getString("zmw")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
}
