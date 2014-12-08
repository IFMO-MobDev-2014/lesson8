package ru.ifmo.md.lesson8.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.List;

import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.content.ContentHelper;
import ru.ifmo.md.lesson8.weather.Forecast;


public class ItemDetailFragment extends Fragment {
    public static final String ARG_PLACE_WOEID = "place_woeid";

    int woeid;

    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        woeid = getArguments().getInt(ARG_PLACE_WOEID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ContentHelper contentHelper = new ContentHelper(getActivity());
        View view = inflater.inflate(R.layout.fragment_item_detail, container, false);
        TextView cityName = (TextView) view.findViewById(R.id.city_name);
        LinearLayout forecastsList = (LinearLayout) view.findViewById(R.id.forecasts_list);
        forecastsList.removeAllViews();
        List<Forecast> forecasts = contentHelper.getForecasts(woeid);
        Forecast today = forecasts.get(0);
        cityName.setText(contentHelper.getPlaceByWoeid(woeid).formattedName() + ": " +
                today.getWeather().getDescription());
        for (Forecast forecast : forecasts) {
            TextView child = new TextView(getActivity());
            child.setText(forecast.getDate() + ": " + forecast.getWeather().getDescription());
            forecastsList.addView(child);
        }
        return view;
    }
}
