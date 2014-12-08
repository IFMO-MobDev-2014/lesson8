package ru.ifmo.md.lesson8;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class Translator {
    private static final String TEXT = "text";
    private static final String requestUrl ="https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20141206T204825Z.aaad018761d8b4f4.d8353ab5afc2cb5f6b708397056658981e341a8f&lang=";

    public static String translate(String lang, String str) {
        try {
            Log.d("debug1", "translating " + str);
            try {
                str = URLEncoder.encode(str, "utf-8");
            } catch (Exception e) {
            }
            java.net.URL url = new URL(requestUrl + lang + "&text=" + str);
            HttpsURLConnection httpConnection = (HttpsURLConnection) url.openConnection();
            httpConnection.connect();
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == 200) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line + '\n');
                }
                JSONTokener tokener = new JSONTokener(sb.toString());
                JSONObject object = (JSONObject) tokener.nextValue();
                String translation = object.getString(TEXT);
                return translation.substring(2, translation.length() - 2);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
