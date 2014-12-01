package odeen.weatherpredictor.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Женя on 28.11.2014.
 */
public class CurrentWeatherActivity extends SingleFragmentActivity {
    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_CITY_ID = "id";
    public static final String EXTRA_CITY_COLOR = "color";

    private String mName;
    private int mId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        mName = getIntent().getStringExtra(EXTRA_CITY);
        mId = getIntent().getIntExtra(EXTRA_CITY_ID, -1);
        return CurrentWeatherFragment.getInstance(mName, mId, 0);
    }
}
