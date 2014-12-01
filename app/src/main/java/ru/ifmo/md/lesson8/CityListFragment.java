package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import ru.ifmo.md.lesson8.data.WeatherItem;
import ru.ifmo.md.lesson8.data.WeatherListAdapter;

/**
 * Created by mariashka on 12/1/14.
 */
public class CityListFragment extends ListFragment {
    private ListListener listener;
    private WeatherListAdapter adapter;
    private List<WeatherItem> items;

    public void setList(List<WeatherItem> i) {
        items = i;
    }

    public void notifyData() {
        Log.d("notification", "lol");
        adapter.notifyDataSetChanged();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        adapter = new WeatherListAdapter(items);
        setListAdapter(adapter);
        return inflater.inflate(R.layout.cities, container, false);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }


    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        listener.solveSelection(position);
    }
}
