package year2013.ifmo.catweather;

import android.net.Uri;
import android.provider.BaseColumns;

public class Weather {
    public static final String AUTHORITY =
            "year2013.ifmo.catweather.provider.weather";

    public static final class JustWeather implements BaseColumns {
        public static final int ID_COLUMN = 0;
        public static final int CITY_COLUMN = 1;
        public static final int TODAY_COLUMN = 2;
        public static final int FUTURE_COLUMN = 3;

        private JustWeather() {}

        public static final String PATH = "weather";

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                AUTHORITY + "/" + JustWeather.PATH);

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.weather.data";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.weather.data";

        public static final String CITY_NAME = "city";

        public static final String TODAY_NAME = "today";

        public static final String FUTURE_NAME = "future";
    }
}

