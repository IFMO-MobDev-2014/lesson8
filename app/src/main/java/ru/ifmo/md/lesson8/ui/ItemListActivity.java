package ru.ifmo.md.lesson8.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.content.ContentHelper;
import ru.ifmo.md.lesson8.net.WeatherDownloadService;
import ru.ifmo.md.lesson8.net.YahooQuery;
import ru.ifmo.md.lesson8.places.Place;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details
 * (if present) is a {@link ItemDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity
        implements ItemListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        final ContentHelper contentHelper = new ContentHelper(this);
        int cnt = 0;
        for (Place place : contentHelper.getPlaces()) {
            cnt++;
        }
        if (cnt == 0) {
            addPlace(true);
        }

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }
    }

    private void addPlace(boolean firstTime) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_place_dialog);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(dialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(params);

        final AutoCompleteTextView editView = (AutoCompleteTextView) dialog.findViewById(R.id.add_place_edit);

        if (firstTime) {
            editView.setText(ContentHelper.autodetectPlace());
        }

        Button okButton = (Button) dialog.findViewById(R.id.add_place_ok_button);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line);
        editView.setAdapter(adapter);
        final int threshold = 5;
        editView.setThreshold(threshold);
        editView.addTextChangedListener(new TextWatcher() {
            private int countCharacters = 1;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String pattern = s.toString();
                countCharacters++;
                if (countCharacters < threshold) {
                    return;
                }
                countCharacters = 1;
                new AsyncTask<String, Void, List<String>>() {
                    @Override
                    protected List<String> doInBackground(String... params) {
                        List<String> names = null;
                        try {
                            names = new ArrayList<>();

                            for (Place place : YahooQuery.findPlace(pattern.trim())) {
                                names.add(place.formattedName());
                            }
                        } catch (IOException ignore) {
                            names = null;
                        }
                        return names;
                    }

                    @Override
                    protected void onPostExecute(List<String> names) {
                        if (names == null) return;
                        adapter.clear();
                        Log.d("ADAPTER", "Add " + names.size() + " names");
                        adapter.addAll(names);
                    }
                }.execute(pattern);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final Context context = this;
        final ContentHelper contentHelper = new ContentHelper(this);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        try {
                            List<Place> places = YahooQuery.findPlace(editView.getText().toString());
                            if (places.isEmpty()) {
                                throw new IOException("Fuck that all");
                            }
                            Place place = places.get(0);
                            contentHelper.addPlace(place);
                            WeatherDownloadService.start(context);
                            return false;
                        } catch (IOException e) {
                            return true;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean fail) {
                        if (fail) {
                            Toast.makeText(context, "Failed to add place", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "New place was successfully added", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.places_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            WeatherDownloadService.start(this);
        } else if (id == R.id.action_add) {
            addPlace(false);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int woeid) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(ItemDetailFragment.ARG_PLACE_WOEID, woeid);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(ItemDetailFragment.ARG_PLACE_WOEID, woeid);
            startActivity(detailIntent);
        }
    }
}
