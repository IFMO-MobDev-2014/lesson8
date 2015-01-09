package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
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
public class CityDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_CITY = "city";
    private ListView lv;
    private ItemAdapter adapter;
    private String city;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CityDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        city = getArguments().getString(ARG_CITY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        lv = ((ListView) rootView.findViewById(R.id.item_detail));
        adapter = new ItemAdapter(getActivity(), android.R.layout.simple_list_item_activated_1);
        lv.setAdapter(adapter);

        getActivity().startService(new Intent(getActivity(), ForecastLoadService.class).putExtra(ARG_CITY, city));

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i("", "fragment started loading");
        Uri base = Uri.parse("content://" + MyContentProvider.AUTHORITY);
        Uri uri = Uri.withAppendedPath(base, DatabaseHelper.ITEMS_TABLE_NAME);
        String selection = DatabaseHelper.ITEMS_CITY + " = \"" + city + "\"";
        return new CursorLoader(getActivity(), uri, null, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (adapter == null) {
            adapter = new ItemAdapter(getActivity(), android.R.layout.simple_list_item_1);
        }
        adapter.clear();
        while (cursor.moveToNext()) {
            adapter.add(DatabaseHelper.getItem(cursor));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter = null;
    }
}
