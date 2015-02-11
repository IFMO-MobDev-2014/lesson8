package com.example.alexey.wather;

/**
 * Created by Alexey on 01.12.2014.
 */

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class IService extends IntentService {


    static String RECEIVER = "1";
    static int STATUS_RUNNING = 2;
    static String RECEIVER_DATA = "4";
    static int STATUS_FINISHED = 5;

    public IService() {
        this("Iservice");
    }

    public IService(String name) {
        super(name);
    }


    public void onCreate() {
        super.onCreate();

        Log.i("Started", "IService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("started", "onhandle");
        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
        Boolean importance=true;
        if(intent.getStringExtra("importance").equals("0")){importance=false;}

      //  if (importance)
      //  receiver.send(STATUS_RUNNING,
      //          Bundle.EMPTY);


        final Bundle data = new Bundle();
        Boolean connect=true;
        URL url1= null;
        try {
            url1 = new URL("http://www.google.ru/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            connect=false;
        }
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            urlConnection = (HttpURLConnection) url1.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            connect=false;
        }
        try {
            urlConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
            connect=false;
        }
        try {
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connect=false;
        }

        if (connect) {
            data.putBoolean("status", true);

        }
        else {
            data.putBoolean("status", false);
            receiver.send(STATUS_FINISHED, data);
            return;
        }


        Boolean refresh=false;
        if (intent.getStringExtra("ref").equals("1")) {
            data.putString("ref","1");
            refresh=true;
        }
        String s = intent.getStringExtra("task");
        String name=ImageConverter.hash(s);
        Cursor cursor_2 = getContentResolver().query(provider.CONTENT_URI, null, "( " + provider.HESH + " = '" + name + "' ) AND ( " + provider.TYPE + " = '2' )", null, null);
        if (!refresh&&cursor_2.moveToFirst()) {
            data.putString(RECEIVER_DATA, "Sample result data");
            if (importance)
                receiver.send(STATUS_FINISHED, data);
            return;
        }
        if (refresh)
            getContentResolver().delete(provider.CONTENT_URI, "( " + provider.HESH + " = '" + name + "' ) AND ( " + provider.TYPE + " = '2' )", null);
        cursor_2= getContentResolver().query(provider.CONTENT_URI, null, "( " + provider.HESH + " = '" + name + "' ) AND ( " + provider.TYPE + " = '1' )", null, null);
        cursor_2.moveToFirst();
        try {
            String link=cursor_2.getString(cursor_2.getColumnIndex(provider.DAY));
            URL url =new URL(link);
            String st=getJ(url);
            if (exc) throw (new Exception("Connection priblems"));
            parseJ(st, s);
            data.putString(RECEIVER_DATA, "Sample result data");
        } catch (IOException e) {

            data.putString(RECEIVER_DATA, "error");
        } catch (Exception e) {
            if(exc) data.putString(RECEIVER_DATA, "Connection problems");
            e.printStackTrace();
        }

        if (importance)
        receiver.send(STATUS_FINISHED, data);
    }

    void parseJ(String strJson, String name) {
        String LOG_TAG = "JSON_PARS";
        Log.d(LOG_TAG, strJson);
        JSONObject dataJsonObj = null;
        String work=ImageConverter.hash(name);
        try {
            dataJsonObj = new JSONObject(strJson);

            JSONObject friends = dataJsonObj.getJSONObject("forecast").getJSONObject("txt_forecast");
            JSONArray kol = friends.getJSONArray("forecastday");
            JSONArray simp = dataJsonObj.getJSONObject("forecast").getJSONObject("simpleforecast").getJSONArray("forecastday");
            String data;
            JSONObject temp;
            String day;
            String night;
            ContentValues cv;
            String temper;
            String img1;
            String img2;
            cv = new ContentValues();
            cv.put(provider.TYPE, "2");
            cv.put(provider.HESH, work);
            for (int i = 0; i < 10; i++) {
                temp = simp.getJSONObject(i).getJSONObject("date");
                data = Integer.toString(temp.getInt("day")) + "." + Integer.toString(temp.getInt("month")) + "." + Integer.toString(temp.getInt("year"));
                day = kol.getJSONObject(2 * i).getString("fcttext_metric");
                night = kol.getJSONObject(2 * i + 1).getString("fcttext_metric");
                temper = simp.getJSONObject(i).getJSONObject("high").getString("celsius");
                img1 = kol.getJSONObject(2 * i).getString("icon_url");
                img2 = kol.getJSONObject(2 * i + 1).getString("icon_url");
                Bitmap bm = BitmapFactory.decodeStream((InputStream) new URL(img1).getContent());
                cv.put(provider.DATE, data);
                cv.put(provider.DAY, day);
                cv.put(provider.NIGHT, night);
                cv.put(provider.TEMPERATURE, temper);
                cv.put(provider.FIVE_PATH, ImageConverter.getBytes(bm));
                bm = BitmapFactory.decodeStream((InputStream) new URL(img2).getContent());
                cv.put(provider.SIX_PATH, ImageConverter.getBytes(bm));
                getContentResolver().insert(provider.CONTENT_URI, cv);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    String getJ(URL url) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";


        try {

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            resultJson = buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
            exc=true;
            resultJson=e.getMessage();
        }
        return resultJson;
    }
    public Boolean exc=false;
    void getAuto()
    {
        String Jo_S = null;
        try {
            URL url=new URL("http://api.wunderground.com/api/0c4d0979336b962f/geolookup/q/autoip.json");
            Jo_S=getJ(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            JSONObject dataJsonObj = new JSONObject(Jo_S);
            String shortLink = dataJsonObj.getJSONObject("location").getString("l");
            String name =dataJsonObj.getJSONObject("location").getString("city");
            //ContentValues cv.put(provider.SIX_PATH, ImageConverter.getBytes(bm));
            //getContentResolver().insert(provider.CONTENT_URI, cv);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}




