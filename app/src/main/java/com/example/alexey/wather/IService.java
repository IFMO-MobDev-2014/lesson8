package com.example.alexey.wather;

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


    public Boolean exc=false;

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
        ResultReceiver receiver = intent.getParcelableExtra(Consts.RECEIVER);
        Boolean importance=true;

        if(intent.getStringExtra("importance").equals("0")){importance=false;}

        final Bundle data = new Bundle();
        Boolean connect;
        connect=ifExistConnection();
        if (connect) {
            data.putBoolean("status", true);

        }
        else {
            data.putBoolean("status", false);
            receiver.send(Consts.STATUS_FINISHED, data);
            return;
        }
        Boolean refresh=false;
        if (intent.getStringExtra("refresh").equals("1")) {
            data.putString("ref","1");
            refresh=true;
        }
        String cityName = intent.getStringExtra("task");
        String name=ImageConverter.hash(cityName);
        Cursor cursor_2 = getContentResolver().query(provider.CONTENT_URI,
                null,
                "( " + provider.HESH + " = '" + name + "' ) AND ( " + provider.TYPE + " = '2' )",
                null, null);
        if (!refresh&&cursor_2.moveToFirst()) {
            data.putString(Consts.RECEIVER_DATA, "Sample result data");
            if (importance)
                receiver.send(Consts.STATUS_FINISHED, data);
            return;
        }
        if (refresh)
            getContentResolver().delete(provider.CONTENT_URI,
                    "( " + provider.HESH + " = '" + name + "' ) AND ( " + provider.TYPE + " = '2' )",
                    null);
        cursor_2= getContentResolver().query(provider.CONTENT_URI, null,
                "( " + provider.HESH + " = '" + name + "' ) AND ( " + provider.TYPE + " = '1' )",
                null, null);
        cursor_2.moveToFirst();
        try {
            String link=cursor_2.getString(cursor_2.getColumnIndex(provider.DAY));
            URL url =new URL(link);
            String responceFile=getJ(url);
            if (exc) throw (new Exception("Connection priblems"));
            parseJ(responceFile, cityName);
            data.putString(Consts.RECEIVER_DATA, "Sample result data");
        } catch (IOException e) {
            data.putString(Consts.RECEIVER_DATA, "error");
        } catch (Exception e) {
            if(exc) data.putString(Consts.RECEIVER_DATA, "Connection problems");
            e.printStackTrace();
        }

        if (importance)
        receiver.send(Consts.STATUS_FINISHED, data);
    }

    Boolean ifExistConnection(){
        URL url;
        try {
            url = new URL("http://www.google.ru/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            urlConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
            return  false;
        }
        try {
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void parseJ(String strJson, String name) {
        String LOG_TAG = "JSON_PARS";
        Log.d(LOG_TAG, strJson);
        JSONObject dataJsonObj;
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
                cv.put(provider.FIRST_PIC, ImageConverter.getBytes(bm));
                bm = BitmapFactory.decodeStream((InputStream) new URL(img2).getContent());
                cv.put(provider.SECOND_PIC, ImageConverter.getBytes(bm));
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
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String resultJson;
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
            reader.close();
            resultJson = buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            exc=true;
            resultJson=e.getMessage();
        }
        return resultJson;
    }

    //auto-determine city
    void getAuto()
    {
        String JSON = null;
        try {
            URL url=new URL(Consts.AUTO_LINK);
            JSON=getJ(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            JSONObject dataJsonObj = new JSONObject(JSON);
            String shortLink = dataJsonObj.getJSONObject("location").getString("l");
            String name =dataJsonObj.getJSONObject("location").getString("city");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}




