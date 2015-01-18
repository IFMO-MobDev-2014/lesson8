package ru.ifmo.md.extratask1;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImagesService extends IntentService {
    public static String serviceName = "downloadImagesService";
    public static final String ACTION_RESPONSE = "ru.ifmo.md.lesson8.weatherDownloadService.RESPONSE";
    public static final int RESULT_ERROR = -100;
    public static final String TAG_PERCENT = "percent";
    public static final String requestUrl = "https://api.500px.com/v1/photos?feature=popular&image_size=3&consumer_key=PujHIQ2IOPRZNY4kecSgM3iQPKDVkH70WAE8DxId";

    public DownloadImagesService() {
        super(serviceName);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Intent response = new Intent();
        response.setAction(ACTION_RESPONSE);
        response.addCategory(Intent.CATEGORY_DEFAULT);
        response.putExtra(TAG_PERCENT, 0);
        sendBroadcast(response);

        try {
            getContentResolver().delete(
                    ImagesContentProvider.IMAGES_URI,
                    null,
                    null
            );

            URL url = url = new URL(requestUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();


            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String resultJson = buffer.toString();

            JSONObject dataJsonObj = new JSONObject(resultJson);
            JSONArray images = dataJsonObj.getJSONArray("photos");

            for (int i = 0; i < images.length(); i++) {
                JSONObject image = images.getJSONObject(i);

                URL imgUrl = new URL(image.getString("image_url"));
                Bitmap bmp = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
                String name = image.getString("name");
                String username = image.getJSONObject("user").getString("username");
                ContentValues cv = new ContentValues();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] bArray = bos.toByteArray();

                cv.put(DBImages.COLUMN_PICTURE, bArray);
                cv.put(DBImages.COLUMN_PICTURE_NAME, name);
                cv.put(DBImages.COLUMN_USERNAME, username);

                getContentResolver().insert(ImagesContentProvider.IMAGES_URI, cv);

                Intent response2 = new Intent();
                response2.setAction(ACTION_RESPONSE);
                response2.addCategory(Intent.CATEGORY_DEFAULT);
                response2.putExtra(TAG_PERCENT, Math.round((float) (i + 1) / (float) images.length() * (float) 100));
                sendBroadcast(response2);
            }

        } catch (Exception e) {
            Intent response2 = new Intent();
            response2.setAction(ACTION_RESPONSE);
            response2.addCategory(Intent.CATEGORY_DEFAULT);
            response2.putExtra(TAG_PERCENT, RESULT_ERROR);
            sendBroadcast(response2);
            e.printStackTrace();
        }
    }
}

