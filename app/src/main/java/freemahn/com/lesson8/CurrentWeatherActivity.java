package freemahn.com.lesson8;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class CurrentWeatherActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<ArrayList<Item>> {

    MyBroadcastReceiver myBroadcastReceiver;
    List<Item> items = null;
    //String i = "Penza";
    static int currentCityId = 0;
    static ArrayList<String> cities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!cities.contains("Penza"))
            cities.add("Penza");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);
        Intent intentMyIntentService = new Intent(this, DownloadForecastService.class);
        intentMyIntentService.putExtra("city", cities.get(currentCityId));
        startService(intentMyIntentService);
        myBroadcastReceiver = new MyBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter(
                DownloadForecastService.ACTION_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);
        getLoaderManager().initLoader(0, null, this);


    }

    int fromCodeToImage(int code) {
        switch (code) {
            case 0:
            case 1:
            case 2:
            case 11:
            case 12:
            case 40:
                return R.drawable.heavy_rain;

            case 3:
            case 4:
            case 37:
            case 38:
                return R.drawable.thunderstorm;

            case 5:
            case 6:
            case 7:
            case 10:
            case 14:
                return R.drawable.rain_snow;

            case 8:
            case 15:
            case 41:
            case 43:
            case 46:
                return R.drawable.heavy_snow;

            case 16:
            case 18:
                return R.drawable.snow;

            case 17:
            case 35:
                return R.drawable.hail;

            case 19:
            case 20:
            case 21:
            case 22:
                return R.drawable.fog;

            case 29:
                return R.drawable.cloud_night;

            case 30:
                return R.drawable.cloud_day;

            case 23:
            case 24:
            case 32:
            case 34:
                return R.drawable.clear_day;

            case 25:
            case 31:
            case 33:
                return R.drawable.clear_night;

            case 44:
                return R.drawable.cloud;

            case 26:
            case 27:
                return R.drawable.heavy_cloud;

            case 28:
                return R.drawable.heavy_cloud_night;

            case 36:
                return R.drawable.hot;

            case 47:
                return R.drawable.partly_rain_day;

            case 39:
            case 9:

                return R.drawable.rain;
            case 45:
                return R.drawable.partly_thunderstorm;

            case 42:
                return R.drawable.partly_snow_day;

            case 13:
                return R.drawable.partly_snow_night;


        }
        return R.drawable.clear_day;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            Intent intentMyIntentService = new Intent(this, DownloadForecastService.class);
            intentMyIntentService.putExtra("city", cities.get(currentCityId));
            startService(intentMyIntentService);
            return true;
        } else if (id == R.id.action_add_city || id == R.id.action_change_city) {
            Intent intent = new Intent(this, ChangeCityActivity.class);
            intent.putExtra("city", currentCityId);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<ArrayList<Item>> onCreateLoader(int id, Bundle args) {
        return new ImagesListLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Item>> loader, ArrayList<Item> data) {
        if (data.size() == 0) return;
        items = data.subList(1, data.size());

        TextView temp = (TextView) findViewById(R.id.temperature);
        TextView text = (TextView) findViewById(R.id.desc_view);
        TextView cityName = (TextView) findViewById(R.id.city);
        ImageView pic = (ImageView) findViewById(R.id.w_pic);
        temp.setText(data.get(0).temp + "  °C");
        text.setText(data.get(0).text);
        cityName.setText(cities.get(currentCityId));
        pic.setImageResource(fromCodeToImage(data.get(0).code));
        GridView gw = (GridView) findViewById(R.id.weekly_forecast);

        gw.setAdapter(new ItemAdapter());

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Item>> loader) {
        new ImagesListLoader(this);
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getLoaderManager().restartLoader(0, null, CurrentWeatherActivity.this);
        }
    }

    public class ItemAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ItemAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final Item item) {
            Log.d("added to adapter", item.toString());
            items.add(0, item);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            System.out.println("getView " + position + " " + convertView);
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.layout_item_view, null);
                holder = new ViewHolder();
                holder.dateView = (TextView) convertView.findViewById(R.id.date_view);
                holder.picView = (ImageView) convertView.findViewById(R.id.pic_view);
                holder.tempView = (TextView) convertView.findViewById(R.id.temp);
                holder.highView = (TextView) convertView.findViewById(R.id.high);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.dateView.setText(items.get(position).date);
            holder.picView.setImageResource(fromCodeToImage(items.get(position).code));
            holder.tempView.setText(items.get(position).temp + " °C");
            holder.highView.setText(items.get(position).high + " °C");
            return convertView;
        }

        public class ViewHolder {
            public TextView dateView;
            public TextView textView;
            public ImageView picView;
            public TextView tempView;
            public TextView highView;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);

    }

}
