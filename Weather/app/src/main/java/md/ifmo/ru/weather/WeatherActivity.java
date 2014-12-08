package md.ifmo.ru.weather;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class WeatherActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        parseWeatherCodes();
        Intent intent = getIntent();
        String city = intent.getStringExtra(MainActivity.CODE);
        TextView cityName = (TextView) findViewById(R.id.city_name);
        cityName.setText(city.split(",")[0]);
        new WeatherTask().execute(city);
    }

    HashMap<Integer, String> weatherTexts;
    HashMap<Integer, String> weatherIcons;

    void parseWeatherCodes() {
        try {
            InputStream is = getResources().openRawResource(R.raw.list_of_weather_types);
            int size = is.available();
            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[2 * size];
            isr.read(buffer, 0, buffer.length);
            StringBuilder sb = new StringBuilder();
            for (char c : buffer) {
                if (c != 0) {
                    sb.append(c);
                }
            }
            String text = sb.toString();
            String[] tokens = text.split("\\t|\\n");
            weatherTexts = new HashMap<Integer, String>();
            weatherIcons = new HashMap<Integer, String>();
            for (int i = 0; i < tokens.length; i += 6) {
                int code = Integer.parseInt(tokens[i]);
                String wIcon = tokens[i + 1];
                weatherIcons.put(code, wIcon);
                String wText = tokens[i + 2];
                weatherTexts.put(code, wText);
            }
            isr.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String API_KEY = "9r8bamvx7utesvcyxb993ggm";
    private static final String API_URL = "http://api.worldweatheronline.com/free/v1/weather.ashx?format=json&num_of_days=3&key=" + API_KEY;
    private static final String BAD_LUCK = "Не удалось выполнить операцию. Проверьте подключение к Интернету и попробуйте ещё раз";

    private class WeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String url = API_URL + "&q=" + URLEncoder.encode(params[0]);
                HttpResponse httpResponse = new DefaultHttpClient().execute(new HttpGet(url));
                HttpEntity httpEntity = httpResponse.getEntity();
                String json = EntityUtils.toString(httpEntity, "UTF-8");

                // parsing starts here
                String result = null;
                JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
                json = object.getString("data");
                object = (JSONObject) new JSONTokener(json).nextValue();
                String cond = object.getString("current_condition");
                object = new JSONArray(cond).getJSONObject(0);
                result = "";
                int weatherCode = Integer.parseInt(object.getString("weatherCode"));
                String weather = weatherTexts.get(weatherCode);
                String weatherIcon = weatherIcons.get(weatherCode);
                result += weatherIcon;
                result += "|";
                result += weather + "\n";
                result += object.getString("temp_C") + "°C\n";
                result += "Влажность: " + object.getString("humidity") + "%\n";
                result += "Облачность: " + object.getString("cloudcover") + "%\n";
                String sPressure = object.getString("pressure");
                int pressure = 76000 * Integer.parseInt(sPressure) / 101325;
                result += pressure + " мм. рт. ст.\n";
                String windEng = object.getString("winddir16Point");
                String wind = "";
                for (int i = 0; i < windEng.length(); i++) {
                    switch (windEng.charAt(i)) {
                        case 'N': wind += "С"; break;
                        case 'S': wind += "Ю"; break;
                        case 'E': wind += "В"; break;
                        case 'W': wind += "З"; break;
                    }
                }
                String sWindSpeed = object.getString("windspeedKmph");
                int windSpeed = (10 * Integer.parseInt(sWindSpeed) + 35) / 36;
                result += "Ветер: " + wind + ", " + windSpeed + " м/с\n";
                // go on to forecasts
                object = (JSONObject) new JSONTokener(json).nextValue();
                JSONArray array = new JSONArray(object.getString("weather"));
                for (int id = 0; id < 3; id++) {
                    result += "|";
                    object = array.getJSONObject(id);
                    weatherCode = Integer.parseInt(object.getString("weatherCode"));
                    weather = weatherTexts.get(weatherCode);
                    weatherIcon = weatherIcons.get(weatherCode);
                    result += weatherIcon;
                    result += "|";
                    String dateStr = object.getString("date");
                    SimpleDateFormat sdfOld = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdfOld.parse(dateStr);
                    SimpleDateFormat sdfNew = new SimpleDateFormat("cccc, dd MMMM");
                    String dateRes = sdfNew.format(date);
                    result += dateRes + "\n";
                    result += weather + "\n";
                    result += object.getString("tempMinC") + "°C / " + object.getString("tempMaxC") + "°C\n";
                    windEng = object.getString("winddir16Point");
                    wind = "";
                    for (int i = 0; i < windEng.length(); i++) {
                        switch (windEng.charAt(i)) {
                            case 'N': wind += "С"; break;
                            case 'S': wind += "Ю"; break;
                            case 'E': wind += "В"; break;
                            case 'W': wind += "З"; break;
                        }
                    }
                    sWindSpeed = object.getString("windspeedKmph");
                    windSpeed = 10 * Integer.parseInt(sWindSpeed) / 36;
                    result += "Ветер: " + wind + ", " + windSpeed + " м/с\n";
                }
                // parsing ends here

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            TextView download = (TextView) findViewById(R.id.download);
            if (result == null) {
                download.setText(BAD_LUCK);
            } else {
                TextView now = (TextView) findViewById(R.id.now);
                now.setText("Сейчас");
                TextView now2 = (TextView) findViewById(R.id.now2);
                now2.setText("Сегодня");
                TextView now3 = (TextView) findViewById(R.id.now3);
                now3.setText("Завтра");
                TextView now4 = (TextView) findViewById(R.id.now4);
                now4.setText("Послезавтра");
                String[] weather = result.split("\\|");
                {
                    download.setText(weather[1]);
                    int imageID = getResources().getIdentifier(weather[0], "drawable", getPackageName());
                    Drawable image = getResources().getDrawable(imageID);
                    download.setCompoundDrawablesWithIntrinsicBounds(image, null, null, null);
                }
                {
                    TextView download2 = (TextView) findViewById(R.id.download2);
                    download2.setText(weather[3]);
                    int imageID = getResources().getIdentifier(weather[2], "drawable", getPackageName());
                    Drawable image = getResources().getDrawable(imageID);
                    download2.setCompoundDrawablesWithIntrinsicBounds(image, null, null, null);
                }
                {
                    TextView download3 = (TextView) findViewById(R.id.download3);
                    download3.setText(weather[5]);
                    int imageID = getResources().getIdentifier(weather[4], "drawable", getPackageName());
                    Drawable image = getResources().getDrawable(imageID);
                    download3.setCompoundDrawablesWithIntrinsicBounds(image, null, null, null);
                }
                {
                    TextView download4 = (TextView) findViewById(R.id.download4);
                    download4.setText(weather[7]);
                    int imageID = getResources().getIdentifier(weather[6], "drawable", getPackageName());
                    Drawable image = getResources().getDrawable(imageID);
                    download4.setCompoundDrawablesWithIntrinsicBounds(image, null, null, null);
                }
            }
        }
    }
}