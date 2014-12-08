package me.volhovm.mweather

import java.lang.ref.WeakReference
import java.util

import android.app.{FragmentManager, Fragment, Activity}
import android.content.Intent
import android.os.Bundle
import android.support.v13.app.{FragmentPagerAdapter, FragmentStatePagerAdapter}
import android.support.v4.view.ViewPager
import android.util.{SparseArray, Log}
import android.view.{ViewGroup, Menu, MenuItem}
import android.widget.Toast
import com.achep.header2actionbar.FadingActionBarHelper

class WeatherActivity extends Activity with Receiver {
  private var mCityNames: List[String] = null
  private var mFadingActionBarHelper: FadingActionBarHelper = null
  private var mViewPager: ViewPager = null
  private var mSwipeAdapter: SwipeAdapter = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    Log.i("CONF:", "Config of the device:" + getResources.getConfiguration.toString)
    // MOTO G
    //    {1.0 250mcc99mnc en_AU ldltr sw360dp w360dp h567dp 320dpi nrml port finger -keyb/v/h -nav/h s.6?spn}
    // HTC ONE V
    //    {1.0 250mcc99mnc uk_UA ldltr sw320dp w320dp h508dp 240dpi nrml long port finger -keyb/v/h -nav/h s.30 themeResource=system}
    // Z COMPACT
    //    {1.0 250mcc2mnc ru_RU ldltr sw360dp w360dp h567dp 320dpi nrml port finger -keyb/v/h -nav/h s.87 skinPackageSeq.1}

    setContentView(R.layout.activity_swipe)
    mCityNames = new DatabaseHelper(this).getCities()
    if (mCityNames.length == 0) mCityNames = List("Saint Petersburg", "Moscow").reverse
    mSwipeAdapter = new SwipeAdapter(getFragmentManager)
    mViewPager = cast(findViewById(R.id.pager))
    mFadingActionBarHelper = new FadingActionBarHelper(getActionBar, getResources.getDrawable(R.drawable.actionbar_bg))
    mViewPager.setAdapter(mSwipeAdapter)
  }

    class SwipeAdapter(manager: FragmentManager) extends FragmentStatePagerAdapter(manager) {
      private val mFragmentMap: util.HashMap[Int, WeakReference[WeatherDetailFragment]] = new util.HashMap[Int, WeakReference[WeatherDetailFragment]]()
      override def getItem(position: Int): Fragment = {
        val ret = WeatherDetailFragment.newInstance(mCityNames(position))
        mFragmentMap.put(position, new WeakReference[WeatherDetailFragment](ret))
        ret
      }
      override def getCount: Int = mCityNames.length

      override def destroyItem(container: ViewGroup, position: Int, `object`: scala.Any): Unit = {
        super.destroyItem(container, position, `object`)
        mFragmentMap.remove(position)
      }
      def findRef(id: Int): Fragment = mFragmentMap.get(id).get()
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
  //    cast[Fragment, WeatherDetailFragment](getFragmentManager.findFragmentById(R.id.pager))
  //    cast[Fragment, WeatherDetailFragment](getFragmentManager.findFragmentByTag("android:switcher:" + R.id.pager + ":0"))
  {
    val out = cast[AnyRef, WeatherDetailFragment](mSwipeAdapter.findRef(mViewPager.getCurrentItem))
    Log.d("WeatherActivity", "getContainerFragment: " + out.cityName + " on position " + mViewPager.getCurrentItem)
    out
  }

  def getFadingActionBarHelper(): FadingActionBarHelper = mFadingActionBarHelper

  override def onReceiveResult(resCode: Int, resData: Bundle): Unit = resCode match {
    case WeatherLoadService.STATUS_ERROR => Toast.makeText(this, "Failed to load data: " +
      resData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show()
    case WeatherLoadService.STATUS_FINISHED => {
      Toast.makeText(this, "Refreshed succesfully", Toast.LENGTH_SHORT).show()
      val frag = getContainerFragment()
      frag.setCity(resData.getString(Intent.EXTRA_TEXT))
      if (frag.isAdded) {
        Log.d("WeatherActivity", "Restarting loader of fragment " + frag + " because of download result received")
        frag.getLoaderManager.restartLoader(0, null, frag).forceLoad()
      }
    }
    case _ => ()
  }
}