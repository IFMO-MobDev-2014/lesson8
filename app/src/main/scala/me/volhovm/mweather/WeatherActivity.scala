package me.volhovm.mweather

import android.app.{Fragment, Activity}
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.{Menu, MenuItem}
import android.widget.Toast
import com.achep.header2actionbar.FadingActionBarHelper

class WeatherActivity extends Activity with NameProvider with Receiver {
  var mCurrentCityName: String = "Saint-Petersburg"
  override def getElem: String = mCurrentCityName
  override def setElem(elem: String): Unit = mCurrentCityName = elem

  private var mFadingActionBarHelper: FadingActionBarHelper = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    Log.i("CONF:", "Config of the device:" + getResources.getConfiguration.toString)
//    {1.0 250mcc99mnc en_AU ldltr sw360dp w360dp h567dp 320dpi nrml port finger -keyb/v/h -nav/h s.6?spn}
//    {1.0 250mcc99mnc uk_UA ldltr sw320dp w320dp h508dp 240dpi nrml long port finger -keyb/v/h -nav/h s.30 themeResource=system}
//    {1.0 250mcc2mnc ru_RU ldltr sw360dp w360dp h567dp 320dpi nrml port finger -keyb/v/h -nav/h s.87 skinPackageSeq.1}
    setContentView(R.layout.activity_main)
    mFadingActionBarHelper = new FadingActionBarHelper(getActionBar, getResources.getDrawable(R.drawable.actionbar_bg))
    if (savedInstanceState == null)
      getFragmentManager.beginTransaction().add(R.id.container, new WeatherDetailFragment).commit
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.main, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
    case R.id.action_refresh =>
      val frg = getContainerFragment()
      frg.reloadContents()
      super.onOptionsItemSelected(item)
  }

  def getContainerFragment(): WeatherDetailFragment =
    cast[Fragment, WeatherDetailFragment](getFragmentManager.findFragmentById(R.id.container))

  def getFadingActionBarHelper(): FadingActionBarHelper = mFadingActionBarHelper

  override def onReceiveResult(resCode: Int, resData: Bundle): Unit = resCode match {
    case WeatherLoadService.STATUS_ERROR => Toast.makeText(this, "Failed to load data: " +
      resData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show()
    case WeatherLoadService.STATUS_FINISHED => {
      setElem(resData.getString(Intent.EXTRA_TEXT))
      Toast.makeText(this, "Refreshed succesfully", Toast.LENGTH_SHORT).show()
      getContainerFragment().getLoaderManager.restartLoader(0, null, getContainerFragment()).forceLoad()
    }
    case _ => ()
  }
}