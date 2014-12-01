package ru.ifmo.md.lesson8.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;

import ru.ifmo.md.lesson8.ItemData;
import ru.ifmo.md.lesson8.MyActivity;
import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.database.DataProvider;
import ru.ifmo.md.lesson8.tasks.LoadInfoTask;

/**
 * Created by Svet on 29.11.2014.
 */
public class WhetherFragment extends Fragment {

    private Context context;
    private DataProvider dp;
    private ItemData current;
    public String title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_main, null);
        TextView temperature = (TextView) v.findViewById(R.id.temperature);
        TextView humidity = (TextView) v.findViewById(R.id.humidity);
        TextView pressure = (TextView) v.findViewById(R.id.pressure);
        TextView wind = (TextView) v.findViewById(R.id.wind);
        TextView description = (TextView) v.findViewById(R.id.description);
        Button update = (Button) v.findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(title != null && !title.isEmpty())
                    new LoadInfoTask(dp).execute(title);
            }
        });

        ImageView image = (ImageView) v.findViewById(R.id.image);

        temperature.setText(getString(R.string.temperature).concat(Double.toString(current.temperatureToday)));
        humidity.setText(getString(R.string.humidity).concat(Integer.toString(current.humidity)));
        pressure.setText(getString(R.string.pressure).concat(Double.toString(current.pressure)));
        wind.setText(getString(R.string.wind_speed).concat(Double.toString(current.wind)));
        description.setText(current.descriptionToday);

        if(current.srcToday == null) {
            current.srcToday = "";
            current.srcTomorrow = "";
            current.srcAfter = "";
        }
        if(!current.srcToday.isEmpty()) {
            int drawableId = getid(current.srcToday);
            if (drawableId != 0) {
                image.setImageResource(drawableId);
            }
        }

        temperature = (TextView) v.findViewById(R.id.temperature_tomorrow);
        description = (TextView) v.findViewById(R.id.description_tomorrow);
        image = (ImageView) v.findViewById(R.id.image1);
        temperature.setText(getString(R.string.temperature).concat(Double.toString(current.temperatureTomorrow)));
        description.setText(current.descriptionTomorrow);
        if(!current.srcTomorrow.isEmpty()) {
            int drawableId = getid(current.srcTomorrow);
            if (drawableId != 0) {
                image.setImageResource(drawableId);
            }
        }

        temperature = (TextView) v.findViewById(R.id.temperature_after);
        description = (TextView) v.findViewById(R.id.description_after);
        image = (ImageView) v.findViewById(R.id.image2);
        temperature.setText(getString(R.string.temperature).concat(Double.toString(current.temperatureAfter)));
        description.setText(current.descriptionAfter);
        if(!current.srcAfter.isEmpty()) {
            int drawableId = getid(current.srcAfter);
            if (drawableId != 0) {
                image.setImageResource(drawableId);
            }
        }

        return v;
    }

    private int getid(String a) {
        int drawableId = 0;
        try {
            Class res = R.drawable.class;
            Field field = res.getField(a);
            drawableId = field.getInt(null);
        } catch (Exception e) {
            Log.e("MyTag", "Failure to get drawable id.", e);
        }
        return drawableId;
    }

    static public WhetherFragment createInstance(Context context, DataProvider dp, int position) {
        WhetherFragment fragment = new WhetherFragment();
        fragment.current = dp.getCityInfoByPosition(position + 1);
        fragment.title = fragment.current.name;
        fragment.context = context;
        fragment.dp = dp;
        Bundle bundle = new Bundle();
        bundle.putInt("number", position);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MyActivity)activity).onSectionAttached(getArguments().getInt("number"));
    }
}
