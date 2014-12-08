package ru.ifmo.md.lesson8;

import android.os.AsyncTask;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * Created by Евгения on 28.09.2014.
 */

public class GsonGetter<TResult> {

    private Class<TResult> classObj;

    public interface Callback<T> {
        void onComplete(GsonGetter<T> sender, T result);
    }

    public GsonGetter(Class<TResult> classObj) {
        this.classObj = classObj;
    }

    public void get(String url, Callback<TResult> resultHandler) {
        new GetGsonTask(resultHandler).execute(url);
    }

    private class GetGsonTask extends AsyncTask<String, Void, TResult> {

        Callback<TResult> resultHandler;

        GetGsonTask(Callback<TResult> resultHandler) {
            this.resultHandler = resultHandler;
        }

        @Override
        protected TResult doInBackground(String... params) {
            URI uri;
            try {
                if (params.length > 0) {
                    String strUri = params[0].replace("*", URLEncoder.encode("*", "UTF-8"));
                    uri = new URI(strUri);
                }
                else
                    return null;
            } catch (URISyntaxException | UnsupportedEncodingException e) {
                return null;
            }
            TResult result = null;
            try {
                HttpGet request = new HttpGet(uri);
                HttpResponse response = new DefaultHttpClient().execute(request);
                InputStreamReader content = new InputStreamReader(response.getEntity().getContent());
                Gson g = new Gson();
                result = g.fromJson(content, classObj);
            } catch (IOException ignored) { }
            return result;
        }

        @Override
        protected void onPostExecute(TResult result) {
            super.onPostExecute(result);
            resultHandler.onComplete(GsonGetter.this, result);
        }
    }
}

