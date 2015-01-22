package weathertogo.sergeybudkov.ru.weathertogo;
import android.app.IntentService;
import android.content.Intent;

//Sergey Budkov 2536

public class WeatherUpdate extends IntentService {
    WeatherDataBase wBase;

    public WeatherUpdate() {
        super("weather");
    }

    public void onCreate() {
        super.onCreate();
        wBase = MyContentProvider.database;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        wBase = new WeatherDataBase(this);
        wBase.open();
        wBase.deleteWeatherTable();
        for (int i = 0; i < MainActivity.addedCities.size(); i++) {
            String urlAdress = "http://export.yandex.ru/weather-ng/forecasts/" + MainActivity.addedCitiesID.get(i).toString() + ".xml";

            Intent intentForWeather = new Intent(this, WeatherIntent.class);
            intentForWeather.putExtra(MainActivity.INTENT, urlAdress);
            intentForWeather.putExtra(MainActivity.CITY, MainActivity.addedCities.get(i).get(MainActivity.CITY));
            intentForWeather.putExtra(MainActivity.COUNTRY, MainActivity.addedCities.get(i).get(MainActivity.COUNTRY));
            intentForWeather.putExtra(MainActivity.YANDEX_ID, MainActivity.addedCitiesID.get(i).toString());

            startService(intentForWeather);
        }
    }
}