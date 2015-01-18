package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;


public class WeatherFragment extends Fragment {
    private static final String CITY_ID = "cityId";
    private static final String CITY_NAME = "cityName";
    private int cityId;
    private String cityName;
    private ListView forecastList;
    private View forecastView;
    private OnFragmentInteractionListener mListener;

    public WeatherFragment() {
    }

    public static int getIcon(int code, int cloudiness, boolean night) {
        int GOOD_VISIBILITY_LIMIT = 37;
        int MIDDLE_VISIBILITY_LIMIT = 62;
        switch (code) {

            case 200:

                if (cloudiness < GOOD_VISIBILITY_LIMIT) {
                    if (night) {
                        return R.drawable.cloudrainlightningmoon;
                    } else {
                        return R.drawable.cloudrainlightningsun;
                    }
                }
            case 201:
                if (cloudiness < MIDDLE_VISIBILITY_LIMIT) {
                    return R.drawable.cloudrainlightningsun2;
                }
            case 202:
                return R.drawable.cloudrainlightning;
            case 210:
                if (cloudiness < GOOD_VISIBILITY_LIMIT) {
                    if (night) {
                        return R.drawable.cloudlightningmoon;
                    } else {
                        return R.drawable.cloudlightningsun;
                    }
                }
            case 211:
                if (cloudiness < MIDDLE_VISIBILITY_LIMIT) {
                    return R.drawable.cloudlightningsun2;
                }
            case 212:
                return R.drawable.cloudlightning;
            case 221:
                return R.drawable.lightning;
            case 230:
                if (cloudiness < GOOD_VISIBILITY_LIMIT) {
                    if (night) {
                        return R.drawable.clouddrizzlelightningmoon;
                    } else {
                        return R.drawable.clouddrizzlelightningsun;
                    }
                }
            case 231:
                if (cloudiness < MIDDLE_VISIBILITY_LIMIT) {
                    return R.drawable.clouddrizzlelightningsun2;
                }
            case 232:
                return R.drawable.clouddrizzlelightning;

            case 300:
                if (cloudiness < GOOD_VISIBILITY_LIMIT) {
                    return R.drawable.clouddrizzlesun;
                }
            case 301:
                if (cloudiness < MIDDLE_VISIBILITY_LIMIT) {
                    if (night) {
                        return R.drawable.clouddrizzlemoon;
                    } else {
                        return R.drawable.clouddrizzlesun2;
                    }
                }
            case 302:
                return R.drawable.clouddrizzle;
            case 310:
                return R.drawable.clouddrizzlesun;
            case 311:
            case 312:
            case 313:
                return R.drawable.umbrelladrizzle;
            case 314:
            case 321:
                return R.drawable.drizzle;


            case 500:
                if (cloudiness < GOOD_VISIBILITY_LIMIT) {
                    if (night) {
                        return R.drawable.cloudrainmoon;
                    } else {
                        return R.drawable.cloudrainsun;
                    }
                }
            case 501:
                if (cloudiness < MIDDLE_VISIBILITY_LIMIT) {
                    return R.drawable.cloudrainsun2;
                }
            case 502:
            case 503:
                return R.drawable.cloudrain;
            case 504:
            case 511:
                return R.drawable.umbrella;
            case 520:
            case 521:
                return R.drawable.cloudrain;
            case 522:
            case 531:
                return R.drawable.rain;


            case 600:
                if (cloudiness < GOOD_VISIBILITY_LIMIT) {
                    if (night) {
                        return R.drawable.cloudsnowmoon;
                    } else {
                        return R.drawable.cloudsnowsun;
                    }
                }
            case 601:
                if (cloudiness < MIDDLE_VISIBILITY_LIMIT) {
                    return R.drawable.cloudsnowsun2;
                }
            case 602:
                return R.drawable.cloudsnow;
            case 611:
            case 612:
                return R.drawable.cloudsleet;
            case 615:
                if (cloudiness < GOOD_VISIBILITY_LIMIT) {
                    if (night) {
                        return R.drawable.cloudsleetmoon;
                    } else {
                        return R.drawable.cloudsleetsun;
                    }
                }
            case 616:
                if (cloudiness < MIDDLE_VISIBILITY_LIMIT) {
                    return R.drawable.cloudsleetsun2;
                }
            case 620:
                return R.drawable.cloudsleet;
            case 621:
            case 622:
                return R.drawable.sleet;


            case 701:
            case 711:
            case 721:
            case 731:
            case 741:
                return R.drawable.fog;
            case 751:
            case 761:
                return R.drawable.cloudfog;
            case 762:
                return R.drawable.suneclipse;
            case 771:
            case 781:
                return R.drawable.cloudfog2;


            case 800:
                if (night) {
                    return R.drawable.moonstars;
                } else {
                    return R.drawable.sun;
                }
            case 801:
                if (night) {
                    return R.drawable.cloudmoon;
                } else {
                    return R.drawable.cloudsun;
                }
            case 802:
                if (night) {
                    return R.drawable.cloudsmoon;
                } else {
                    return R.drawable.cloudssun;
                }
            case 803:
                return R.drawable.cloud;
            case 804:
                return R.drawable.clouds;


            case 900:
                return R.drawable.tornado;
            case 901:
            case 902:
                return R.drawable.wind;
            case 903:
                return R.drawable.thermometer25;
            case 904:
                return R.drawable.thermometer100;
            case 905:
            case 906:
            case 951:
                return R.drawable.cloudwind2;
            case 952:
            case 953:
            case 954:
            case 955:
                if (night) {
                    return R.drawable.cloudwind2moon;
                } else {
                    return R.drawable.cloudwind2sun;
                }
            case 956:
            case 957:
            case 958:
            case 959:
                return R.drawable.cloudwind;
            case 960:
            case 961:
            case 962:
                return R.drawable.wind;
        }
        return R.drawable.cloud;
    }

    public static int getDescription(int code) {
        switch (code) {
            case 200:
                return R.string.thunderstorm_with_light_rain;
            case 201:
                return R.string.thunderstorm_with_rain;
            case 202:
                return R.string.thunderstorm_with_heavy_rain;
            case 210:
                return R.string.light_thunderstorm;
            case 211:
                return R.string.thunderstorm;
            case 212:
                return R.string.heavy_thunderstorm;
            case 221:
                return R.string.ragged_thunderstorm;
            case 230:
                return R.string.thunderstorm_with_light_drizzle;
            case 231:
                return R.string.thunderstorm_with_drizzle;
            case 232:
                return R.string.thunderstorm_with_heavy_drizzle;
            case 300:
                return R.string.light_intensity_drizzle;
            case 301:
                return R.string.drizzle;
            case 302:
                return R.string.heavy_intensity_drizzle;
            case 310:
                return R.string.light_intensity_drizzle_rain;
            case 311:
                return R.string.drizzle_rain;
            case 312:
                return R.string.heavy_intensity_drizzle_rain;
            case 313:
                return R.string.shower_rain_and_drizzle;
            case 314:
                return R.string.heavy_shower_rain_and_drizzle;
            case 321:
                return R.string.shower_drizzle;
            case 500:
                return R.string.light_rain;
            case 501:
                return R.string.moderate_rain;
            case 502:
                return R.string.heavy_intensity_rain;
            case 503:
                return R.string.very_heavy_rain;
            case 504:
                return R.string.extreme_rain;
            case 511:
                return R.string.freezing_rain;
            case 520:
                return R.string.light_intensity_shower_rain;
            case 521:
                return R.string.shower_rain;
            case 522:
                return R.string.heavy_intensity_shower_rain;
            case 531:
                return R.string.ragged_shower_rain;
            case 600:
                return R.string.light_snow;
            case 601:
                return R.string.snow;
            case 602:
                return R.string.heavy_snow;
            case 611:
                return R.string.sleet;
            case 612:
                return R.string.shower_sleet;
            case 615:
                return R.string.light_rain_and_snow;
            case 616:
                return R.string.rain_and_snow;
            case 620:
                return R.string.light_shower_snow;
            case 621:
                return R.string.shower_snow;
            case 622:
                return R.string.heavy_shower_snow;
            case 701:
                return R.string.mist;
            case 711:
                return R.string.smoke;
            case 721:
                return R.string.haze;
            case 731:
                return R.string.sand_dust_whirls;
            case 741:
                return R.string.fog;
            case 751:
                return R.string.sand;
            case 761:
                return R.string.dust;
            case 762:
                return R.string.volcanic_ash;
            case 771:
                return R.string.squalls;
            case 781:
                return R.string.tornado;
            case 800:
                return R.string.clear_sky;
            case 801:
                return R.string.few_clouds;
            case 802:
                return R.string.scattered_clouds;
            case 803:
                return R.string.broken_clouds;
            case 804:
                return R.string.overcast_clouds;
            case 900:
                return R.string.tornado;
            case 901:
                return R.string.tropical_storm;
            case 902:
                return R.string.hurricane;
            case 903:
                return R.string.cold;
            case 904:
                return R.string.hot;
            case 905:
                return R.string.windy;
            case 906:
                return R.string.hail;
            case 951:
                return R.string.calm;
            case 952:
                return R.string.light_breeze;
            case 953:
                return R.string.gentle_breeze;
            case 954:
                return R.string.moderate_breeze;
            case 955:
                return R.string.fresh_breeze;
            case 956:
                return R.string.strong_breeze;
            case 957:
                return R.string.high_wind_near_gale;
            case 958:
                return R.string.gale;
            case 959:
                return R.string.severe_gale;
            case 960:
                return R.string.storm;
            case 961:
                return R.string.violent_storm;
            case 962:
                return R.string.hurricane;
        }
        return R.string.no_information;
    }

    public static String getWindDirection(int degrees) {
        final String[] windDirs = {"north", "northeast", "east", "southeast",
                "south", "southwest", "west", "northwest"};
        return windDirs[((int) ((degrees + 22.5f) / 45.0f)) % windDirs.length];
    }

    public static WeatherFragment newInstance(int cityId, String cityName) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putInt(CITY_ID, cityId);
        args.putString(CITY_NAME, cityName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cityId = getArguments().getInt(CITY_ID);
            cityName = getArguments().getString(CITY_NAME);
        } else {
            throw new NullPointerException();
        }
        mListener.setCityName(cityName);

        getLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity().getApplicationContext(),
                        DatabaseContentProvider.URI_CITY_DIR.buildUpon().appendPath(cityId + "").build(),
                        WeatherTable.FULL_WEATHER_PROJECTION,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                ((CursorAdapter) forecastList.getAdapter()).swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });

        LoaderManager.LoaderCallbacks<Cursor> myCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity().getApplicationContext(),
                        DatabaseContentProvider.URI_CITY_DIR,
                        CitiesTable.FULL_CITY_PROJECTION,
                        CitiesTable.URL + " = ?",
                        new String[]{"" + cityId},
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                onFreshCityData(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        };

        getLoaderManager().initLoader(2, null, myCallbacks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        forecastView = view;
        forecastList = (ListView) view.findViewById(R.id.forecastList);
        forecastList.setAdapter(new WeatherCursorAdapter(getActivity().getApplicationContext(), null, false));
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onFreshCityData(Cursor cursor) {
        cursor.moveToFirst();
        if (cursor.isAfterLast()) {
            return;
        }
        ImageView imageView = (ImageView) forecastView.findViewById(R.id.weatherImage);
        TextView tempText = (TextView) forecastView.findViewById(R.id.tempText);
        TextView windText = (TextView) forecastView.findViewById(R.id.windText);
        TextView pressHumText = (TextView) forecastView.findViewById(R.id.pressHumText);
        TextView weatherSubText = (TextView) forecastView.findViewById(R.id.weatherSubText);
        TextView weatherDayText = (TextView) forecastView.findViewById(R.id.weatherDayText);
        imageView.setImageResource(getIcon(cursor.getInt(cursor.getColumnIndex(CitiesTable.DESCRIPTION)), cursor.getInt(cursor.getColumnIndex(CitiesTable.CLOUDS)), false));
        tempText.setText(cursor.getInt(cursor.getColumnIndex(CitiesTable.TEMPERATURE)) / 10.0f + " °C");
        windText.setText(cursor.getInt(cursor.getColumnIndex(CitiesTable.WIND)) / 10.0f + " m/s " + getWindDirection(cursor.getInt(cursor.getColumnIndex(CitiesTable.WIND_DIR))));
        pressHumText.setText(((int) (cursor.getInt(cursor.getColumnIndex(CitiesTable.PRESSURE)) * 0.75d)) + " mmHg " + cursor.getInt(cursor.getColumnIndex(CitiesTable.HUMIDITY)) + "%");
        weatherSubText.setText(getDescription(cursor.getInt(cursor.getColumnIndex(CitiesTable.DESCRIPTION))));
        weatherDayText.setText(getActivity().getString(R.string.now));
    }

    public interface OnFragmentInteractionListener {
        public void setCityName(String cityName);
    }

    private class WeatherCursorAdapter extends CursorAdapter {

        public WeatherCursorAdapter(Context context, Cursor cursor, boolean autoRequery) {
            super(context, cursor, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.layout_weather_day, parent, false);
            bindView(view, context, cursor);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) view.findViewById(R.id.weatherImage);
            TextView tempMin = (TextView) view.findViewById(R.id.tempMin);
            TextView tempMax = (TextView) view.findViewById(R.id.tempMax);
            TextView windText = (TextView) view.findViewById(R.id.windText);
            TextView pressHumText = (TextView) view.findViewById(R.id.pressHumText);
            TextView weatherSubText = (TextView) view.findViewById(R.id.weatherSubText);
            TextView weatherDayText = (TextView) view.findViewById(R.id.weatherDayText);
            imageView.setImageResource(getIcon(cursor.getInt(cursor.getColumnIndex(WeatherTable.DESCRIPTION)), cursor.getInt(cursor.getColumnIndex(WeatherTable.CLOUDS)), false));
            try {
                tempMin.setText(cursor.getInt(cursor.getColumnIndex(WeatherTable.TEMPERATURE_MIN)) / 10.0f + " °C");
                tempMax.setText(cursor.getInt(cursor.getColumnIndex(WeatherTable.TEMPERATURE_MAX)) / 10.0f + " °C");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            windText.setText(cursor.getInt(cursor.getColumnIndex(WeatherTable.WIND)) / 10.0f + " m/s " + getWindDirection(cursor.getInt(cursor.getColumnIndex(WeatherTable.WIND_DIR))));
            pressHumText.setText(((int) (cursor.getInt(cursor.getColumnIndex(WeatherTable.PRESSURE)) * 0.75d)) + " mmHg " + cursor.getInt(cursor.getColumnIndex(WeatherTable.HUMIDITY)) + "%");
            weatherSubText.setText(getDescription(cursor.getInt(cursor.getColumnIndex(WeatherTable.DESCRIPTION))));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(WeatherTable.TIME)) * 1000L);
            weatherDayText.setText(DateFormat.getDateInstance().format(calendar.getTime()));
        }
    }

}
