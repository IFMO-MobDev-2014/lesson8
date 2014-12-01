package odeen.weatherpredictor.view;

import android.support.v4.app.Fragment;

/**
 * Created by Женя on 27.11.2014.
 */
public class LocationListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new LocationListFragment();
    }
}
