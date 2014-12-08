package me.volhovm.mweather

import java.lang.ref.WeakReference
import java.util

import android.app.AlertDialog.Builder
import android.app._
import android.content.DialogInterface.OnClickListener
import android.content.{DialogInterface, Intent}
import android.os.Bundle
import android.support.v13.app.{FragmentPagerAdapter, FragmentStatePagerAdapter}
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.util.{SparseArray, Log}
import android.view.{View, ViewGroup, Menu, MenuItem}
import android.widget.AdapterView.OnItemClickListener
import android.widget._
import com.achep.header2actionbar.FadingActionBarHelper

class WeatherActivity extends Activity with Receiver  {
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
    if (mCityNames.length == 0) mCityNames = List("Saint Petersburg", "Kerch")
    mSwipeAdapter = new SwipeAdapter(getFragmentManager)
    mViewPager = cast(findViewById(R.id.pager))
    if (!Global.performanceOn) mViewPager.setOffscreenPageLimit(5) //10?
    mViewPager.setOnPageChangeListener(new OnPageChangeListener {
      override def onPageScrollStateChanged(state: Int): Unit = {}
      // TODO: add onPageScrolled
      override def onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int): Unit = {}
      override def onPageSelected(position: Int): Unit = {}
    })
    mFadingActionBarHelper = new FadingActionBarHelper(getActionBar, getResources.getDrawable(R.drawable.actionbar_bg))
    mViewPager.setAdapter(mSwipeAdapter)
  }

  class SwipeAdapter(manager: FragmentManager) extends FragmentStatePagerAdapter(manager) {
    private val mFragmentMap: util.HashMap[Int, WeakReference[WeatherDetailFragment]] = new util.HashMap[Int, WeakReference[WeatherDetailFragment]]()
    override def getItem(position: Int): Fragment = {
      val ret = WeatherDetailFragment.newInstance(mCityNames(position), position)
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
      val frg = getCurrentContainerFragment()
      frg.reloadContents()
      super.onOptionsItemSelected(item)
    case R.id.action_cities_list =>
      val dialog = new Builder(this).create()
      val listView: ListView = new ListView(this)
      listView.setAdapter(new ArrayAdapter[String](this, android.R.layout.simple_list_item_1, mCityNames.toArray))
      dialog.setView(listView)
      dialog.setTitle("Available locations")
      dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Add location", new OnClickListener {
        override def onClick(p1: DialogInterface, p2: Int): Unit = {
          val weatherAddDialogBuilder = new Builder(WeatherActivity.this)
          val editText = new EditText(WeatherActivity.this)
          weatherAddDialogBuilder.setView(editText)
          weatherAddDialogBuilder.setTitle("Adding new city")
          weatherAddDialogBuilder.setPositiveButton("Add city", new OnClickListener {
            override def onClick(p1: DialogInterface, p2: Int): Unit = {
              if (editText.getText != "") {
                mCityNames = mCityNames ::: List(editText.getText.toString)
                mSwipeAdapter.notifyDataSetChanged()
                mViewPager.setCurrentItem(mCityNames.length - 1, !Global.performanceOn)
              }
            }
          })
          weatherAddDialogBuilder.setNeutralButton("Autodetect", new OnClickListener {
            override def onClick(p1: DialogInterface, p2: Int): Unit = {}
          })
          weatherAddDialogBuilder.show()
        }
      })
      dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Dismiss", new OnClickListener {
        override def onClick(p1: DialogInterface, p2: Int): Unit = {}
      })
      listView.setOnItemClickListener(new OnItemClickListener {
        override def onItemClick(p1: AdapterView[_], p2: View, position: Int, p4: Long): Unit = {
          mViewPager.setCurrentItem(position, !Global.performanceOn)
          dialog.dismiss()
        }
      })
      dialog.show()
      super.onOptionsItemSelected(item)
  }

  def getCurrentContainerFragment(): WeatherDetailFragment =
  //    cast[Fragment, WeatherDetailFragment](getFragmentManager.findFragmentById(R.id.pager))
  //    cast[Fragment, WeatherDetailFragment](getFragmentManager.findFragmentByTag("android:switcher:" + R.id.pager + ":0")) {
  {
    val out = cast[AnyRef, WeatherDetailFragment](mSwipeAdapter.findRef(mViewPager.getCurrentItem))
    Log.d("WeatherActivity", "getContainerFragment: " + out.cityName + " on position " + mViewPager.getCurrentItem)
    out
  }

  def getContainerFragment(id: Int): WeatherDetailFragment = {
    val out = cast[AnyRef, WeatherDetailFragment](mSwipeAdapter.findRef(id))
    Log.d("WeatherActivity", "getContainerFragment: " + out.cityName + " on position " + id)
    out
  }

  def getFadingActionBarHelper(): FadingActionBarHelper = mFadingActionBarHelper

  override def onReceiveResult(resCode: Int, resData: Bundle): Unit = resCode match {
    case WeatherLoadService.STATUS_ERROR => Toast.makeText(this, "Failed to load data: " +
      resData.getString(WeatherLoadService.NEW_CITY), Toast.LENGTH_LONG).show()
    case WeatherLoadService.STATUS_FINISHED => {
      Toast.makeText(this, "Refreshed succesfully", Toast.LENGTH_SHORT).show()
      val frag = getContainerFragment(resData.getInt(WeatherLoadService.FRAGMENT_ID))
      frag.setCity(resData.getString(WeatherLoadService.NEW_CITY))
      mCityNames = mCityNames.updated(resData.getInt(WeatherLoadService.FRAGMENT_ID), resData.getString(WeatherLoadService.NEW_CITY))
      if (frag.isAdded) {
        Log.d("WeatherActivity", "Restarting loader of fragment " + frag + " because of download result received")
        frag.getLoaderManager.restartLoader(0, null, frag).forceLoad()
      }
    }
    case _ => ()
  }
}