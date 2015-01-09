package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link CitiesActivity}
 * in two-pane mode (on tablets) or a {@link CityDetailsActivity}
 * on handsets.
 */
public class CityDetailsFragment extends Fragment {
    public static final String ARG_CITY = "city";
    private ListView lv;
    private ItemAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CityDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        lv = ((ListView) rootView.findViewById(R.id.item_detail));
        adapter = new ItemAdapter(getActivity(), android.R.layout.simple_list_item_activated_1);
        lv.setAdapter(adapter);

        getActivity().startService(new Intent(getActivity(), ForecastLoadService.class));

        return rootView;
    }
}
