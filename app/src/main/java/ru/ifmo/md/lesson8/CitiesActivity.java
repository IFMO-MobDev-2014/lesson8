package ru.ifmo.md.lesson8;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CityDetailsActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CitiesFragment} and the item details
 * (if present) is a {@link CityDetailsFragment}.
 * <p/>
 * This activity also implements the required
 * {@link CitiesFragment.Callbacks} interface
 * to listen for item selections.
 */
public class CitiesActivity extends FragmentActivity
        implements CitiesFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((CitiesFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }
        Log.i("", "citiesactivity created");
        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link CitiesFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String city) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(CityDetailsFragment.ARG_CITY, city);
            CityDetailsFragment fragment = new CityDetailsFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, CityDetailsActivity.class);
            detailIntent.putExtra(CityDetailsFragment.ARG_CITY, city);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                final CharSequence[] items = {"Add city"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose an action");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            startActivity(new Intent(CitiesActivity.this, AddCityActivity.class));
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
        }

        return super.onKeyDown(keycode, e);
    }
}
