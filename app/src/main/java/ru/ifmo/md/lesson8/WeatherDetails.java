package ru.ifmo.md.lesson8;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.ifmo.md.lesson8.data.WeatherItem;

/**
 * Created by mariashka on 12/2/14.
 */
public class WeatherDetails extends Fragment{
    private WeatherItem item;

    public void setItem(WeatherItem i) {
        item = i;
    }

    public WeatherItem getItem() {
        return item;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View l = inflater.inflate(R.layout.fragment_weather_details, container, false);
        TextView text = (TextView) l.findViewById(R.id.name);
        text.setText(" " + item.getName());
        text = (TextView) l.findViewById(R.id.currDate);
        text.setText(" " + item.getDate());
        text = (TextView) l.findViewById(R.id.currTmp);
        text.setText(" Temperature: " + Integer.toString(item.getCurrT()) + " C");
        text = (TextView) l.findViewById(R.id.feel);
        text.setText(Integer.toString(item.getFeels()) + "");
        ImageView img = (ImageView) l.findViewById(R.id.currImg);
        String s = item.getCondition();
        s = s.substring(59, s.length() - 4);
        int resId = getResources().getIdentifier(s, "drawable", getActivity().getPackageName());
        img.setImageDrawable(getResources().getDrawable(resId));

        List<String> hC = item.getHourlyC();
        List<Integer> hT = item.getHourlyT();

        img = (ImageView) l.findViewById(R.id.hourImg1);
        text = (TextView) l.findViewById(R.id.hour1);
        text.setText("    " + hT.get(0) + " C");
        s = hC.get(0);
        s = s.substring(59, s.length() - 4);
        resId = getResources().getIdentifier(s, "drawable", getActivity().getPackageName());
        img.setImageDrawable(getResources().getDrawable(resId));

        img = (ImageView) l.findViewById(R.id.hourImg2);
        text = (TextView) l.findViewById(R.id.hour2);
        text.setText("    " + hT.get(1) + " C");
        s = hC.get(1);
        s = s.substring(59, s.length() - 4);
        resId = getResources().getIdentifier(s, "drawable", getActivity().getPackageName());
        img.setImageDrawable(getResources().getDrawable(resId));

        img = (ImageView) l.findViewById(R.id.hourImg3);
        text = (TextView) l.findViewById(R.id.hour3);
        text.setText("    " + hT.get(2) + " C");
        s = hC.get(2);
        s = s.substring(59, s.length() - 4);
        resId = getResources().getIdentifier(s, "drawable", getActivity().getPackageName());
        img.setImageDrawable(getResources().getDrawable(resId));

        img = (ImageView) l.findViewById(R.id.hourImg4);
        text = (TextView) l.findViewById(R.id.hour4);
        text.setText("    " + hT.get(3) + " C");
        s = hC.get(3);
        s = s.substring(59, s.length() - 4);
        resId = getResources().getIdentifier(s, "drawable", getActivity().getPackageName());
        img.setImageDrawable(getResources().getDrawable(resId));

        List<WeatherItem> next = item.getNext();
        img = (ImageView) l.findViewById(R.id.nextImg1);
        text = (TextView) l.findViewById(R.id.min1);
        TextView text2 = (TextView) l.findViewById(R.id.max1);
        text.setText(" Min: " + next.get(0).getMin() + " C");
        text2.setText(" Max: " + next.get(0).getMax() + " C");
        s = next.get(0).getCondition();
        s = s.substring(59, s.length() - 4);
        resId = getResources().getIdentifier(s, "drawable", getActivity().getPackageName());
        img.setImageDrawable(getResources().getDrawable(resId));

        img = (ImageView) l.findViewById(R.id.nextImg2);
        text = (TextView) l.findViewById(R.id.min2);
        text2 = (TextView) l.findViewById(R.id.max2);
        text.setText(" Min: " + next.get(1).getMin() + " C");
        text2.setText(" Max: " + next.get(1).getMax() + " C");
        s = next.get(1).getCondition();
        s = s.substring(59, s.length() - 4);
        resId = getResources().getIdentifier(s, "drawable", getActivity().getPackageName());
        img.setImageDrawable(getResources().getDrawable(resId));

        img = (ImageView) l.findViewById(R.id.nextImg3);
        text = (TextView) l.findViewById(R.id.min3);
        text2 = (TextView) l.findViewById(R.id.max3);
        text.setText(" Min: " + next.get(2).getMin() + " C");
        text2.setText(" Max: " + next.get(2).getMax() + " C");
        s = next.get(2).getCondition();
        s = s.substring(59, s.length() - 4);
        resId = getResources().getIdentifier(s, "drawable", getActivity().getPackageName());
        img.setImageDrawable(getResources().getDrawable(resId));

        return l;
    }

}
