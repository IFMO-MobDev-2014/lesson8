package ru.ifmo.md.lesson8.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.content.ContentHelper;


public class ItemDetailFragment extends Fragment {
    public static final String ARG_PLACE_WOEID = "place_woeid";

    private int woeid;

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
        ListView forecastsList = (ListView) view.findViewById(R.id.forecasts_list);
        ForecastsListAdapter adapter = new ForecastsListAdapter(getActivity(),
                contentHelper.getForecastsCursor(woeid));
        forecastsList.setAdapter(adapter);

        return view;
    }
}
