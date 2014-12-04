package com.example.home.superwheather;

/**
 * Created by Home on 04.12.2014.
 */

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import android.database.Cursor;


/**
 * Created by Home on 28.11.2014.
 */

public class AlarmLoaderService extends IntentService {

    public AlarmLoaderService() {
        super("superserviceloader");
    }

    public void onCreate() {
        super.onCreate();
    }

    private static final String apiKey = "54b1a3ede57af76efeacd8d8e5dc7";

    private static String readStream(InputStream data) {
        String ans = "";
        try {
            StringBuilder inStr = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(data));
            String line = br.readLine();
            while (line != null) {
                inStr.append(line);
                inStr.append('\n');
                line = br.readLine();
            }
            ans = inStr.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ans;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String city = intent.getStringExtra("city");

        String queryUrl = null;
        try {
            queryUrl = "http://api.worldweatheronline.com/free/v2/weather.ashx?q=" + URLEncoder.encode(city, "utf-8") +
                    "&format=json&num_of_days=5&key=" + apiKey;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String[] result = new String[]{};
        String[][][] forecast = new String[][][] {};
        String[] dates = new String[5];
        String cityName = city;

        try {
            URL url = new URL(queryUrl);
            URLConnection urlConnection = url.openConnection();
            JSONObject response = new JSONObject(readStream(urlConnection.getInputStream()));
            JSONArray results = response.getJSONObject("data").getJSONArray("current_condition");
            JSONObject res = results.getJSONObject(0);
            result = new String[]{res.getString("cloudcover"), res.getString("temp_C"), res.getString("pressure"), res.getString("humidity"), res.getString("observation_time"),
                    res.getJSONArray("weatherIconUrl").getJSONObject(0).getString("value")};

            cityName = response.getJSONObject("data").getJSONArray("request").getJSONObject(0).getString("query");

            results = response.getJSONObject("data").getJSONArray("weather");
            forecast = new String[5][][];
            for (int i = 0; i < 5; i++) {
                dates[i] = results.getJSONObject(i).getString("date");
                JSONArray hourly = results.getJSONObject(i).getJSONArray("hourly");
                forecast[i] = new String[8][];
                for (int j = 0; j < 8; j++) {
                    JSONObject currentHour = hourly.getJSONObject(j);
                    forecast[i][j] = new String[]{currentHour.getString("cloudcover"), currentHour.getString("tempC"),
                            currentHour.getString("pressure"), currentHour.getString("humidity"), currentHour.getString("time")};
                }
            }

        }  catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        if (result.length > 0) {
            Intent intentResponse = new Intent();

            intentResponse.putExtra("city", city);
            intentResponse.putExtra("cityName", cityName);
            intentResponse.putExtra("temp", result[1]);
            intentResponse.putExtra("cloud", result[0]);
            intentResponse.putExtra("hum", result[3]);
            intentResponse.putExtra("press", result[2]);
            intentResponse.putExtra("1_00:00", forecast[0][0]);
            intentResponse.putExtra("1_12:00", forecast[0][4]);
            intentResponse.putExtra("1_21:00", forecast[0][7]);
            intentResponse.putExtra("2_00:00", forecast[1][0]);
            intentResponse.putExtra("2_12:00", forecast[1][4]);
            intentResponse.putExtra("2_21:00", forecast[1][7]);
            intentResponse.putExtra("3_00:00", forecast[2][0]);
            intentResponse.putExtra("3_12:00", forecast[2][4]);
            intentResponse.putExtra("3_21:00", forecast[2][7]);
            intentResponse.putExtra("4_00:00", forecast[3][0]);
            intentResponse.putExtra("4_12:00", forecast[3][4]);
            intentResponse.putExtra("4_21:00", forecast[3][7]);
            intentResponse.putExtra("5_00:00", forecast[4][0]);
            intentResponse.putExtra("5_12:00", forecast[4][4]);
            intentResponse.putExtra("5_21:00", forecast[4][7]);
            intentResponse.putExtra("dates", dates);
            intentResponse.putExtra("succeed", true);


            ContentValues values = new ContentValues();
            values.put(MyTable.COLUMN_T_ID, "1");
            values.put(MyTable.COLUMN_TITLE, intent.getStringExtra("cityName"));

            getContentResolver().update(MyContentProvider.CONTENT_URI, values, MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?",
                    new String[] {"1", intent.getStringExtra("city")});

            int cloudcover = Integer.valueOf(intent.getStringExtra("cloud"));
            int id;
            if (cloudcover < 25) {
                id = R.drawable.i96_2;
            } else if (cloudcover < 50) {
                id = R.drawable.i96_3;
            } else if (cloudcover < 75) {
                id = R.drawable.i96_4;
            } else {
                id = R.drawable.i96_5;
            }

            values = new ContentValues();

            city = intent.getStringExtra("city");

            values.put(MyTable.COLUMN_T_ID, 2);
            values.put(MyTable.COLUMN_TITLE, city);
            values.put(MyTable.COLUMN_TEMP, intent.getStringExtra("temp") + "°C" );
            values.put(MyTable.COLUMN_CLOUD, "Облачность: " + intent.getStringExtra("cloud") + "%");
            values.put(MyTable.COLUMN_HUM, "Влажность: " + intent.getStringExtra("hum") + "%");
            values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (Integer.valueOf(intent.getStringExtra("press")) / 1.33322368d)) + " мм.рт.ст.");
            values.put(MyTable.COLUMN_PIC_ID, "" + id);

            Cursor cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                            MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                    MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                            "2", city}, null);

            if (cursor.getCount() > 0) {
                getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                        MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                "2", city});
            } else {
                getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
            }

            //*******************************************************************************************

            int cloudcover_1 = (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("1_12:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("1_21:00")[0])) / 3;
            int humidity_1 = (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("1_12:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("1_21:00")[3])) / 3;
            int pressure_1 = (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("1_12:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("1_21:00")[2])) / 3;
            int id_1_1;
            if (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[0]) < 25) {
                id_1_1 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[0]) < 50) {
                id_1_1 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("1_00:00")[0]) < 75) {
                id_1_1 = R.drawable.s96_4;
            } else {
                id_1_1 = R.drawable.s96_5;
            }
            int id_1_2;
            if (Integer.valueOf(intent.getStringArrayExtra("1_12:00")[0]) < 25) {
                id_1_2 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("1_12:00")[0]) < 50) {
                id_1_2 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("1_12:00")[0]) < 75) {
                id_1_2 = R.drawable.s96_4;
            } else {
                id_1_2 = R.drawable.s96_5;
            }
            int id_1_3;
            if (Integer.valueOf(intent.getStringArrayExtra("1_21:00")[0]) < 25) {
                id_1_3 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("1_21:00")[0]) < 50) {
                id_1_3 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("1_21:00")[0]) < 75) {
                id_1_3 = R.drawable.s96_4;
            } else {
                id_1_3 = R.drawable.s96_5;
            }

            values = new ContentValues();

            values.put(MyTable.COLUMN_T_ID, 2 + 1);
            values.put(MyTable.COLUMN_TITLE, city);
            values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("1_00:00")[1] + "°C&" + intent.getStringArrayExtra("1_12:00")[1] + "°C&" + intent.getStringArrayExtra("1_21:00")[1] + "°C");
            values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_1 + "%");
            values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_1 + "%");
            values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_1 / 1.33322368d)) + " мм.рт.ст.");
            values.put(MyTable.COLUMN_PIC_ID, id_1_1 + "&" + id_1_2 + "&" + id_1_3);
            values.put(MyTable.COLUMN_DATE, MainForecaster.normalizeDate(intent.getStringArrayExtra("dates")[0]));

            cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                            MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                    MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                            "3", city}, null);

            if (cursor.getCount() > 0) {
                getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                        MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                "3", city});
            } else {
                getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
            }

            //***************************************************************************************************

            int cloudcover_2 = (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("2_12:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("2_21:00")[0])) / 3;
            int humidity_2 = (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("2_12:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("2_21:00")[3])) / 3;
            int pressure_2 = (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("2_12:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("2_21:00")[2])) / 3;
            int id_2_1;
            if (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[0]) < 25) {
                id_2_1 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[0]) < 50) {
                id_2_1 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("2_00:00")[0]) < 75) {
                id_2_1 = R.drawable.s96_4;
            } else {
                id_2_1 = R.drawable.s96_5;
            }
            int id_2_2;
            if (Integer.valueOf(intent.getStringArrayExtra("2_12:00")[0]) < 25) {
                id_2_2 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("2_12:00")[0]) < 50) {
                id_2_2 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("2_12:00")[0]) < 75) {
                id_2_2 = R.drawable.s96_4;
            } else {
                id_2_2 = R.drawable.s96_5;
            }
            int id_2_3;
            if (Integer.valueOf(intent.getStringArrayExtra("2_21:00")[0]) < 25) {
                id_2_3 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("2_21:00")[0]) < 50) {
                id_2_3 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("2_21:00")[0]) < 75) {
                id_2_3 = R.drawable.s96_4;
            } else {
                id_2_3 = R.drawable.s96_5;
            }

            values = new ContentValues();

            values.put(MyTable.COLUMN_T_ID, 2 + 2);
            values.put(MyTable.COLUMN_TITLE, city);
            values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("2_00:00")[1] + "°C&" + intent.getStringArrayExtra("2_12:00")[1] + "°C&" + intent.getStringArrayExtra("2_21:00")[1] + "°C");
            values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_2 + "%");
            values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_2 + "%");
            values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_2 / 1.33322368d)) + " мм.рт.ст.");
            values.put(MyTable.COLUMN_PIC_ID, id_2_1 + "&" + id_2_2 + "&" + id_2_3);
            values.put(MyTable.COLUMN_DATE, MainForecaster.normalizeDate(intent.getStringArrayExtra("dates")[1]));

            cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                            MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                    MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                            "4", city}, null);

            if (cursor.getCount() > 0) {
                getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                        MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                "4", city});
            } else {
                getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
            }

            //***************************************************************************************************

            int cloudcover_3 = (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("3_12:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("3_21:00")[0])) / 3;
            int humidity_3 = (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("3_12:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("3_21:00")[3])) / 3;
            int pressure_3 = (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("3_12:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("3_21:00")[2])) / 3;
            int id_3_1;
            if (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[0]) < 25) {
                id_3_1 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[0]) < 50) {
                id_3_1 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("3_00:00")[0]) < 75) {
                id_3_1 = R.drawable.s96_4;
            } else {
                id_3_1 = R.drawable.s96_5;
            }
            int id_3_2;
            if (Integer.valueOf(intent.getStringArrayExtra("3_12:00")[0]) < 25) {
                id_3_2 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("3_12:00")[0]) < 50) {
                id_3_2 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("3_12:00")[0]) < 75) {
                id_3_2 = R.drawable.s96_4;
            } else {
                id_3_2 = R.drawable.s96_5;
            }
            int id_3_3;
            if (Integer.valueOf(intent.getStringArrayExtra("3_21:00")[0]) < 25) {
                id_3_3 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("3_21:00")[0]) < 50) {
                id_3_3 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("3_21:00")[0]) < 75) {
                id_3_3 = R.drawable.s96_4;
            } else {
                id_3_3 = R.drawable.s96_5;
            }

            values = new ContentValues();

            values.put(MyTable.COLUMN_T_ID, 2 + 3);
            values.put(MyTable.COLUMN_TITLE, city);
            values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("3_00:00")[1] + "°C&" + intent.getStringArrayExtra("3_12:00")[1] + "°C&" + intent.getStringArrayExtra("3_21:00")[1] + "°C");
            values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_3 + "%");
            values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_3 + "%");
            values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_3 / 1.33322368d)) + " мм.рт.ст.");
            values.put(MyTable.COLUMN_PIC_ID, id_3_1 + "&" + id_3_2 + "&" + id_3_3);
            values.put(MyTable.COLUMN_DATE, MainForecaster.normalizeDate(intent.getStringArrayExtra("dates")[2]));

            cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                            MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                    MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                            "5", city}, null);

            if (cursor.getCount() > 0) {
                getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                        MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                "5", city});
            } else {
                getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
            }

            //***************************************************************************************************

            int cloudcover_4 = (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("4_12:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("4_21:00")[0])) / 3;
            int humidity_4 = (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("4_12:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("4_21:00")[3])) / 3;
            int pressure_4 = (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("4_12:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("4_21:00")[2])) / 3;
            int id_4_1;
            if (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[0]) < 25) {
                id_4_1 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[0]) < 50) {
                id_4_1 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("4_00:00")[0]) < 75) {
                id_4_1 = R.drawable.s96_4;
            } else {
                id_4_1 = R.drawable.s96_5;
            }
            int id_4_2;
            if (Integer.valueOf(intent.getStringArrayExtra("4_12:00")[0]) < 25) {
                id_4_2 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("4_12:00")[0]) < 50) {
                id_4_2 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("4_12:00")[0]) < 75) {
                id_4_2 = R.drawable.s96_4;
            } else {
                id_4_2 = R.drawable.s96_5;
            }
            int id_4_3;
            if (Integer.valueOf(intent.getStringArrayExtra("4_21:00")[0]) < 25) {
                id_4_3 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("4_21:00")[0]) < 50) {
                id_4_3 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("4_21:00")[0]) < 75) {
                id_4_3 = R.drawable.s96_4;
            } else {
                id_4_3 = R.drawable.s96_5;
            }

            values = new ContentValues();

            values.put(MyTable.COLUMN_T_ID, 2 + 4);
            values.put(MyTable.COLUMN_TITLE, city);
            values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("4_00:00")[1] + "°C&" + intent.getStringArrayExtra("4_12:00")[1] + "°C&" + intent.getStringArrayExtra("4_21:00")[1] + "°C");
            values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_4 + "%");
            values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_4 + "%");
            values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_4 / 1.33322368d)) + " мм.рт.ст.");
            values.put(MyTable.COLUMN_PIC_ID, id_4_1 + "&" + id_4_2 + "&" + id_4_3);
            values.put(MyTable.COLUMN_DATE, MainForecaster.normalizeDate(intent.getStringArrayExtra("dates")[3]));

            cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                            MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                    MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                            "6", city}, null);

            if (cursor.getCount() > 0) {
                getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                        MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                "6", city});
            } else {
                getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
            }

            //***************************************************************************************************

            int cloudcover_5 = (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("5_12:00")[0]) +
                    Integer.valueOf(intent.getStringArrayExtra("5_21:00")[0])) / 3;
            int humidity_5 = (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("5_12:00")[3]) +
                    Integer.valueOf(intent.getStringArrayExtra("5_21:00")[3])) / 3;
            int pressure_5 = (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("5_12:00")[2]) +
                    Integer.valueOf(intent.getStringArrayExtra("5_21:00")[2])) / 3;
            int id_5_1;
            if (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[0]) < 25) {
                id_5_1 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[0]) < 50) {
                id_5_1 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("5_00:00")[0]) < 75) {
                id_5_1 = R.drawable.s96_4;
            } else {
                id_5_1 = R.drawable.s96_5;
            }
            int id_5_2;
            if (Integer.valueOf(intent.getStringArrayExtra("5_12:00")[0]) < 25) {
                id_5_2 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("5_12:00")[0]) < 50) {
                id_5_2 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("5_12:00")[0]) < 75) {
                id_5_2 = R.drawable.s96_4;
            } else {
                id_5_2 = R.drawable.s96_5;
            }
            int id_5_3;
            if (Integer.valueOf(intent.getStringArrayExtra("5_21:00")[0]) < 25) {
                id_5_3 = R.drawable.s96_2;
            } else if (Integer.valueOf(intent.getStringArrayExtra("5_21:00")[0]) < 50) {
                id_5_3 = R.drawable.s96_3;
            } else if (Integer.valueOf(intent.getStringArrayExtra("5_21:00")[0]) < 75) {
                id_5_3 = R.drawable.s96_4;
            } else {
                id_5_3 = R.drawable.s96_5;
            }

            values = new ContentValues();

            values.put(MyTable.COLUMN_T_ID, 2 + 5);
            values.put(MyTable.COLUMN_TITLE, city);
            values.put(MyTable.COLUMN_TEMP, intent.getStringArrayExtra("5_00:00")[1] + "°C&" + intent.getStringArrayExtra("5_12:00")[1] + "°C&" + intent.getStringArrayExtra("5_21:00")[1] + "°C");
            values.put(MyTable.COLUMN_CLOUD, "Облачность: " + cloudcover_5 + "%");
            values.put(MyTable.COLUMN_HUM, "Влажность: " + humidity_5 + "%");
            values.put(MyTable.COLUMN_PRESS, "Давление: " + Integer.toString((int) (pressure_5 / 1.33322368d)) + " мм.рт.ст.");
            values.put(MyTable.COLUMN_PIC_ID, id_5_1 + "&" + id_5_2 + "&" + id_5_3);
            values.put(MyTable.COLUMN_DATE, MainForecaster.normalizeDate(intent.getStringArrayExtra("dates")[4]));

            cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{
                            MyTable.COLUMN_TEMP, MyTable.COLUMN_CLOUD, MyTable.COLUMN_HUM, MyTable.COLUMN_PRESS},
                    MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                            "7", city}, null);

            if (cursor.getCount() > 0) {
                getContentResolver().update(MyContentProvider.CONTENT_URI, values,
                        MyTable.COLUMN_T_ID + " = ? AND " + MyTable.COLUMN_TITLE + " = ?", new String[]{
                                "7", city});
            } else {
                getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
            }
            
        }

    }

}
