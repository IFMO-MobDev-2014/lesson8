package year2013.ifmo.weather;

import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by Юлия on 16.01.2015.
 */
public class Forecast implements BaseColumns{

    public static final String AUTHORITY = "year2013.ifmo.weather.provider.forecast";

    public static final int ID_COLUMN = 0;
    public static final int CITY_COLUMN = 1;
    public static final int CURRENT_FORECAST_COLUMN = 2;
    public static final int DAYS_FORECAST_COLUMN = 3;

    private Forecast() {}

    public static final String FORECAST_NAME = "forecast";

    public static final Uri FORECAST_URI = Uri.parse("content://" +
            AUTHORITY + "/" + FORECAST_NAME);

    public static final Uri CONTENT_URI = FORECAST_URI;

    public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.forecast.data";

    public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.forecast.data";

    public static final String CITY_NAME = "city";

    public static final String CURRENT_FORECAST = "current_forecast";

    public static final String DAYS_FORECAST = "days_forecast";

}
