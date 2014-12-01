package odeen.weatherpredictor.view;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import odeen.weatherpredictor.Location;
import odeen.weatherpredictor.R;

/**
 * Created by Женя on 30.11.2014.
 */
public class WeatherPagerActivity extends ActionBarActivity {
    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_CITY_ID = "id";
    MyAdapter mAdapter;
    ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getIntent().getStringExtra(EXTRA_CITY);
        int id = getIntent().getIntExtra(EXTRA_CITY_ID, -1);
        int color = getIntent().getIntExtra(CurrentWeatherActivity.EXTRA_CITY_COLOR, -1);
        setContentView(R.layout.pager_layout);
        mAdapter = new MyAdapter(getSupportFragmentManager(), new Location(id, name, color));
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setId(R.id.viewPager);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mPager.setAdapter(mAdapter);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }
        };
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Current")
                        .setTabListener(tabListener));
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Forecast")
                        .setTabListener(tabListener));
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        private Location mLoc;
        public MyAdapter(FragmentManager fm, Location loc) {
            super(fm);
            mLoc = loc;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return CurrentWeatherFragment.getInstance(mLoc.getCity(), mLoc.getId(), mLoc.getColor());
            return ForecastWeatherFragment.getInstance(mLoc.getCity(), mLoc.getId(), mLoc.getColor());
        }
    }


}
