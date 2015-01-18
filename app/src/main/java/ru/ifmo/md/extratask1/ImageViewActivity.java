package ru.ifmo.md.extratask1;

import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Window;

import java.util.ArrayList;

public class ImageViewActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<ArrayList<MyImage>> {
    private static final int IMAGES_LOADER_ID = 1;
    public static final String APP_PREFERENCES_POSITION = "position";
    SharedPreferences settings;
    ArrayList<MyImage> list;
    int position;
    ViewPager pager;
    PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_image_view);
        pager = (ViewPager) findViewById(R.id.pager);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.contains(APP_PREFERENCES_POSITION)) {
            position = settings.getInt(APP_PREFERENCES_POSITION, 0);
        } else position = 0;


        getLoaderManager().initLoader(IMAGES_LOADER_ID, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(APP_PREFERENCES_POSITION, pager.getCurrentItem());
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (settings.contains(APP_PREFERENCES_POSITION)) {
            position = settings.getInt(APP_PREFERENCES_POSITION, 0);
        } else position = 0;
        update();
    }

    private void update() {
        if (list != null && position >= 0 && position < list.size()) {
            pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(pagerAdapter);
            pager.setCurrentItem(position);
        }
    }

    public Loader<ArrayList<MyImage>> onCreateLoader(int i, Bundle bundle) {
        return new MyImagesListLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<MyImage>> listLoader, final ArrayList<MyImage> list) {
        this.list = list;
        update();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<MyImage>> listLoader) {
        new MyImagesListLoader(this);
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFullscreenFragment.newInstance(
                    list.get(position).picture,
                    list.get(position).pictureName,
                    list.get(position).username
            );
        }

        @Override
        public int getCount() {
            return list.size();
        }

    }

}
