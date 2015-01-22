package ru.ifmo.md.lesson8;

import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

/**
 * An activity representing a list of Cities. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CityDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CityListFragment} and the item details
 * (if present) is a {@link CityDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link CityListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class CityListActivity extends ActionBarActivity implements CityListFragment.Callbacks {
    private boolean mTwoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        if (findViewById(R.id.city_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            ((CityListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.city_list))
                    .setActivateOnItemClick(true);
        }
    }
    /**
     * Callback method from {@link CityListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(CityDetailFragment.ARG_CITY_ID, id);
            CityDetailFragment fragment = new CityDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.city_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, CityDetailActivity.class);
            detailIntent.putExtra(CityDetailFragment.ARG_CITY_ID, id);
            startActivity(detailIntent);
        }
    }
}