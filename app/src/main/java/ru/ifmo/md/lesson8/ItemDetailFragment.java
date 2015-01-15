package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ifmo.md.lesson8.dummy.DummyContent;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<DummyContent.Pair>, AppResultReceiver.Receiver {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.CitiesItem mItem;
    private DummyContent.WeatherItem mWeatherItem;

    View rootView;
    SwipeRefreshLayout refreshLayout;
    TextView date;
    TextView humidity;
    TextView other;
    TextView temp;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    private AppResultReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new AppResultReceiver(new Handler());
        mReceiver.setReceiver(this);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments()!=null && getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getInt(ARG_ITEM_ID));
            mWeatherItem = DummyContent.WEATHER_MAP.get(getArguments().getInt(ARG_ITEM_ID));


            Typeface tf = Typeface.createFromAsset(getResources().getAssets(), "font.ttf");
            date = (TextView) rootView.findViewById(R.id.something);
            temp = (TextView) rootView.findViewById(R.id.temp);
            other = (TextView) rootView.findViewById(R.id.other);
            humidity = (TextView) rootView.findViewById(R.id.humadity);
            date.setTypeface(tf);
            temp.setTypeface(tf);
            other.setTypeface(tf);
            humidity.setTypeface(tf);
            date.setVisibility(View.INVISIBLE);
            humidity.setVisibility(View.INVISIBLE);
            other.setVisibility(View.INVISIBLE);
            (rootView.findViewById(R.id.forecastView)).setVisibility(View.INVISIBLE);
            (rootView.findViewById(R.id.icon_weather)).setVisibility(View.INVISIBLE);
            temp.setVisibility(View.INVISIBLE);

            getLoaderManager().initLoader(0, null, this);
            Intent intent = new Intent(getActivity(), UpdaterService.class);
            intent.putExtra("id", mItem.woeid);
            intent.putExtra("name", mItem.name);
            intent.putExtra("receiver", mReceiver);
            getActivity().startService(intent);
            upgrade();

            if (mItem != null) {
                refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Intent intent = new Intent(getActivity(), UpdaterService.class);
                        intent.putExtra("id", mItem.woeid);
                        intent.putExtra("name", mItem.name);
                        intent.putExtra("receiver", mReceiver);
                        getActivity().startService(intent);
                        upgrade();

                    }
                });

            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);
        // Show the dummy content as text in a TextView.


        return rootView;
    }

    @Override
    public Loader<DummyContent.Pair> onCreateLoader(int i, Bundle args) {
        return new WeatherLoader(getActivity(), mItem.woeid);
    }

    @Override
    public void onLoadFinished(Loader<DummyContent.Pair> loader, DummyContent.Pair data) {
        (rootView.findViewById(R.id.temp)).setVisibility(View.VISIBLE);
        (rootView.findViewById(R.id.something)).setVisibility(View.VISIBLE);
        (rootView.findViewById(R.id.humadity)).setVisibility(View.VISIBLE);
        (rootView.findViewById(R.id.other)).setVisibility(View.VISIBLE);
        (rootView.findViewById(R.id.forecastView)).setVisibility(View.VISIBLE);
        (rootView.findViewById(R.id.icon_weather)).setVisibility(View.VISIBLE);
        ((TextView) rootView.findViewById(R.id.temp)).setText(data.current.temp+"°C");
        Date date1 = new Date(data.current.date*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String formattedDate = sdf.format(date1);

        ((TextView) rootView.findViewById(R.id.something)).setText(formattedDate);
        ((TextView) rootView.findViewById(R.id.humadity)).setText("Humidity: " + data.current.humidity+ "% ");
        ((TextView) rootView.findViewById(R.id.other)).setText("Pressure: " + data.current.pressure + " mb " +"\n"+ "Wind: " + data.current.wind + " m/s");
        ((ListView) rootView.findViewById(R.id.forecastView)).setAdapter(new ForecastAdapter(getActivity(), data.forecast));
        ImageView imageView = (ImageView) rootView.findViewById(R.id.icon_weather);
        switch (data.current.type) {
            case "01d":
                imageView.setImageResource(R.drawable.pic01d);
                break;
            case "01n":
                imageView.setImageResource(R.drawable.pic01n);
                break;
            case "02d":
                imageView.setImageResource(R.drawable.pic02d);
                break;
            case "02n":
                imageView.setImageResource(R.drawable.pic02n);
                break;
            case "03d":
                imageView.setImageResource(R.drawable.pic03d);
                break;
            case "03n":
                imageView.setImageResource(R.drawable.pic03d);
                break;
            case "04d":
                imageView.setImageResource(R.drawable.pic03d);
                break;
            case "04n":
                imageView.setImageResource(R.drawable.pic03d);
                break;
            case "09d":
                imageView.setImageResource(R.drawable.pic09d);
                break;
            case "09n":
                imageView.setImageResource(R.drawable.pic09d);
                break;
            case "10d":
                imageView.setImageResource(R.drawable.pic10d);
                break;
            case "10n":
                imageView.setImageResource(R.drawable.pic10n);
                break;
            case "11d":
                imageView.setImageResource(R.drawable.pic11d);
                break;
            case "11n":
                imageView.setImageResource(R.drawable.pic11d);
                break;
            case "13d":
                imageView.setImageResource(R.drawable.pic13d);
                break;
            case "13n":
                imageView.setImageResource(R.drawable.pic13d);
                break;
            case "50d":
                imageView.setImageResource(R.drawable.pic50d);
                break;
            case "50n":
                imageView.setImageResource(R.drawable.pic50d);
                break;
            default:
                imageView.setImageResource(R.drawable.na);
                break;
        }
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<DummyContent.Pair> loader) {
        ((TextView) rootView.findViewById(R.id.temp)).setText("");
        ((TextView) rootView.findViewById(R.id.something)).setText("");
        ImageView imageView = (ImageView) rootView.findViewById(R.id.icon_weather);
        imageView.setImageResource(R.drawable.pic50d);

    }

    public void upgrade() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode) {
            case AppResultReceiver.OK:

                break;
            case AppResultReceiver.ERROR:
                Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
                break;
        }
        getLoaderManager().restartLoader(0, null, this);
    }

}
