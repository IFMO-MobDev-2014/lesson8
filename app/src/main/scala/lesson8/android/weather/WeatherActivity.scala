package lesson8.android.weather

import android.app.Activity
import android.os.Bundle
import android.view.{MenuItem, Menu}
import android.widget.Toast
import com.achep.header2actionbar.FadingActionBarHelper

class WeatherActivity extends Activity {
  private var mFadingActionBarHelper: FadingActionBarHelper = null
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    mFadingActionBarHelper = new FadingActionBarHelper(getActionBar, getResources.getDrawable(R.drawable.actionbar_bg))
    if (savedInstanceState == null)
      getFragmentManager.beginTransaction().add(R.id.container, new WeatherDetailFragment()).commit
  }


  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.main, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
    case R.id.action_refresh =>
      Toast.makeText(this, "TEST", Toast.LENGTH_LONG).show()
      super.onOptionsItemSelected(item)
  }

  def getFadingActionBarHelper(): FadingActionBarHelper = mFadingActionBarHelper
}