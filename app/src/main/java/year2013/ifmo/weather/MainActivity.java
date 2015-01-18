package year2013.ifmo.weather;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    public  static final String EXTRA_CITY_NAME = "city_name";
    public static String CITY_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CITY_NAME = getIntent().getStringExtra(EXTRA_CITY_NAME);

        Bundle args = new Bundle();
        args.putString("city_name", CITY_NAME);
        WeatherFragment fragment = new WeatherFragment();
        fragment.setArguments(args);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment).commit();
        }

        //Log.d("MainActivity", "I'm in onCreate method!");
    }

    public void changeCity(String city){
        //Log.d("MainActivity", "I'm in changeCity method!");
        WeatherFragment wf = (WeatherFragment)getSupportFragmentManager()
                .findFragmentById(R.id.container);
        wf.changeCity(city);
    }

    public void onUpdateClick (View view) {
        changeCity(CITY_NAME);
    }

    public void onCityClick (View view) {
        finish();
    }

}
