package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pva701 on 27.11.14.
 */
public class ForecastListAdapter extends RecyclerView.Adapter<ForecastListAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }
    private OnItemClickListener listener;
    private ArrayList <ShortWeatherData> data = new ArrayList<ShortWeatherData>();
    private ArrayList <View> views = new ArrayList<View>();
    private Activity parent;
    private int prevPos = -1;

    public ForecastListAdapter(Activity parent) {
        this.parent = parent;
    }

    public void add(ShortWeatherData w) {
        data.add(w);
        views.add(null);
    }

    public void clear() {
        data.clear();
        views.clear();
    }

    public ShortWeatherData getItem(int pos) {
        return data.get(pos);
    }

    public void setCurrentItem(int newPos) {
        if (prevPos != newPos) {
            if (prevPos != -1 && views.get(prevPos) != null)
                views.get(prevPos).setBackgroundResource(R.drawable.rounded_view_usual);
            if (views.get(newPos) != null)
                views.get(newPos).setBackgroundResource(R.drawable.rounded_view_selected);
            prevPos = newPos;
        }
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(parent).inflate(R.layout.day_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ShortWeatherData cur = data.get(i);
        ImageView imageViewIcon = (ImageView)viewHolder.root().findViewById(R.id.icon);
        String icon = cur.getIcon() + ".png";
        AssetManager manager = parent.getAssets();
        try {
            Bitmap iconBitmap = BitmapFactory.decodeStream(manager.open(icon));
            DisplayMetrics metrics = new DisplayMetrics();
            parent.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Configuration config = parent.getResources().getConfiguration();
            imageViewIcon.setBackground(new BitmapDrawable(new Resources(manager, metrics, config), iconBitmap));
        } catch (IOException e) {
            e.printStackTrace();
        }
        viewHolder.setTemp(temp(cur.getTemp()));
        viewHolder.setTempMin(temp(cur.getTempMin()));
        viewHolder.setTempMax(temp(cur.getTempMax()));
        viewHolder.setDate(cur.getDate());
        views.set(i, viewHolder.root);
        if (prevPos == i)
            viewHolder.root.setBackgroundResource(R.drawable.rounded_view_selected);
        else
            viewHolder.root.setBackgroundResource(R.drawable.rounded_view_usual);
    }

    public String temp(int x) {
        if (x > 0)
            return "+" + x + "°";
        return "" + x + "°";
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View root;
        private TextView tempMin;
        private TextView tempMax;
        private TextView temp;
        private TextView day;
        //static private SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy, HH:mm");
        //.format(source.getLastUpdate())
        public ViewHolder(View root) {
            super(root);
            this.root = root;
            temp = ((TextView)root.findViewById(R.id.temp));
            tempMin = ((TextView)root.findViewById(R.id.temp_min));
            tempMax = ((TextView)root.findViewById(R.id.temp_max));
            day = ((TextView)root.findViewById(R.id.day));
            root.setOnClickListener(this);
        }
        public void setDate(Date d) {
            day.setText(new SimpleDateFormat("d MMM, EEE", Locale.ENGLISH).format(d));
        }

        public void setTempMin(String t) {
            tempMin.setText(t);
        }

        public void setTempMax(String t) {
            tempMax.setText(t);
        }

        public void setTemp(String t) {
            temp.setText(t);
        }

        public View root() {
            return root;
        }

        @Override
        public void onClick(View view) {
            if (listener != null)
                listener.onItemClick(view, getPosition());
        }
    }
}
