package weathertogo.sergeybudkov.ru.weathertogo;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class WeatherIntent extends IntentService {
    public static String ACTION_MyIntentService = "ru.sergeybudkov.weathertogo";
    public static String city = "";
    public static String country = "";
    public static String yandex_id = "";

    public WeatherIntent() throws Exception {

        super("weather");
    }

    public void onCreate() {

        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String URLAdress = intent.getStringExtra(MainActivity.INTENT);
        city = intent.getStringExtra(MainActivity.CITY);
        country = intent.getStringExtra(MainActivity.COUNTRY);
        yandex_id = intent.getStringExtra(MainActivity.YANDEX_ID);

        try {
            URL url = new URL(URLAdress);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setConnectTimeout(15000);
            InputStream inputStream = httpConnection.getInputStream();
            byte[] b = new byte[50];
            inputStream.read(b);
            String encoding = "";
            for (int i = 0; i < 50; i++) encoding = encoding + (char)b[i];
            encoding = encoding.substring(encoding.indexOf("encoding=\"") + 10 , encoding.indexOf("\"?>"));
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b);

            inputStream = new SequenceInputStream(byteArrayInputStream, inputStream);

            Reader reader = new InputStreamReader(inputStream, encoding);
            InputSource is = new InputSource(reader);
            is.setEncoding(encoding);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            SAXParser parser = factory.newSAXParser();
            WeatherParser saxXMLParser = new WeatherParser();
            parser.parse(is, saxXMLParser);
        } catch(Exception e) {
            e.printStackTrace();
            Log.d("Handle Intent", "Warning");
        }
        Intent intentResponse = new Intent();
        intentResponse.setAction(ACTION_MyIntentService);
        intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(intentResponse);
    }

    public void onDestroy() {
        super.onDestroy();
    }
}