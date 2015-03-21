package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * @author Aydar Gizatullin a.k.a. lightning95
 */

public class CityListActivity extends ActionBarActivity
        implements CityListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        if (findViewById(R.id.city_detail_container) != null) {
            mTwoPane = true;

            ((CityListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.city_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(CityDetailFragment.ARG_CITY_ID, id);
            CityDetailFragment fragment = new CityDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.city_detail_container, fragment)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, CityDetailActivity.class);
            detailIntent.putExtra(CityDetailFragment.ARG_CITY_ID, id);
            startActivity(detailIntent);
        }
    }
}
