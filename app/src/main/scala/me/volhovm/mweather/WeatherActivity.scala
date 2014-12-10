package me.volhovm.mweather

import java.lang.ref.WeakReference
import java.util

import android.app.AlertDialog.Builder
import android.app._
import android.content.DialogInterface.OnClickListener
import android.content.{Context, DialogInterface, Intent}
import android.location.{Location, LocationListener, LocationManager}
import android.os.{Bundle, Handler}
import android.support.v13.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.util.Log
import android.view.{Menu, MenuItem, View, ViewGroup}
import android.widget.AdapterView.{OnItemClickListener, OnItemLongClickListener}
import android.widget._
import com.achep.header2actionbar.FadingActionBarHelper

class WeatherActivity extends Activity with Receiver {
  private var mLocationManager: LocationManager = null
  private var mLocationListener: SimpleLocationListener = null
  private var mCityNames: List[String] = null
  private var mFadingActionBarHelper: FadingActionBarHelper = null
  private var mViewPager: ViewPager = null
  private var mSwipeAdapter: SwipeAdapter = null
  private var mDBHelper: DatabaseHelper = null

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
    mDBHelper = new DatabaseHelper(getApplicationContext)
    mLocationManager = cast(getSystemService(Context.LOCATION_SERVICE))
    mLocationListener = new SimpleLocationListener
    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener)
    //    mCityNames = mDBHelper.mWrapper.getCities()
    mCityNames = new HelperWrapper(getContentResolver).getCities()
    if (mCityNames.length == 0) {
      mCityNames = List("Saint Petersburg", "Kerch")
      mCityNames.foreach((a: String) => mDBHelper.mWrapper.addCity(a))
    }
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
    val intent: Intent = new Intent(Intent.ACTION_SYNC, null, getApplicationContext, classOf[WeatherLoadService])
    intent.putExtra(WeatherLoadService.SERVICE_MODE, WeatherLoadService.GLOBAL_REFRESH_MODE)
    cast[Object, AlarmManager](getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
      AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_HALF_HOUR,
      PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
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

  class SimpleLocationListener extends LocationListener {
    override def onLocationChanged(loc: Location): Unit = {
      //      Toast.makeText(WeatherActivity.this, "Location: " + loc.getLatitude.toString + " " + loc.getLongitude.toString, Toast.LENGTH_SHORT).show()
    }
    override def onProviderEnabled(p1: String): Unit = Toast.makeText(WeatherActivity.this, "Location enabled", Toast.LENGTH_SHORT).show()
    override def onProviderDisabled(p1: String): Unit = Toast.makeText(WeatherActivity.this, "Location disabled", Toast.LENGTH_SHORT).show()
    override def onStatusChanged(p1: String, p2: Int, p3: Bundle): Unit = {}
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
          val dialogView = getLayoutInflater.inflate(R.layout.edit_view, null, false)
          val editText = cast[View, EditText](dialogView.findViewById(R.id.new_city_edittext))
          weatherAddDialogBuilder.setView(dialogView)
          weatherAddDialogBuilder.setTitle("Adding new city")
          weatherAddDialogBuilder.setPositiveButton("Add city", new OnClickListener {
            override def onClick(p1: DialogInterface, p2: Int): Unit = {
              if (editText.getText != "") {
                addCity(editText.getText.toString.trim.capitalize)
              }
            }
          })
          weatherAddDialogBuilder.setNeutralButton("Autodetect", new OnClickListener {
            override def onClick(p1: DialogInterface, p2: Int): Unit = {
              val location: Location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
              if (location != null) {
                import me.volhovm.mweather.WeatherLoadService._
                val intent: Intent = new Intent(Intent.ACTION_SYNC, null, WeatherActivity.this, classOf[WeatherLoadService])
                Toast.makeText(WeatherActivity.this, "Refreshing", Toast.LENGTH_SHORT).show()
                intent.putExtra(LATITUDE, location.getLatitude)
                intent.putExtra(LONGITUDE, location.getLongitude)
                intent.putExtra(SERVICE_MODE, COORD_MODE)
                val receiver = new WeatherLoadReceiver(new Handler())
                receiver.setReceiver(cast[Activity, Receiver](WeatherActivity.this))
                intent.putExtra(RECEIVER, receiver)
                WeatherActivity.this.startService(intent)
                Toast.makeText(WeatherActivity.this, location.getLatitude.toString + " " + location.getLongitude.toString, Toast.LENGTH_SHORT)
              } else
                Toast.makeText(WeatherActivity.this, "Couldn't retrieve location", Toast.LENGTH_SHORT)
            }
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
      listView.setOnItemLongClickListener(new OnItemLongClickListener {
        override def onItemLongClick(p1: AdapterView[_], p2: View, position: Int, p4: Long): Boolean = {
          val builder = new Builder(WeatherActivity.this)
          builder.setTitle("Deleting location")
          builder.setMessage("Remove " + mCityNames(position) + "?")
          builder.setNegativeButton("Dismiss", new OnClickListener {
            override def onClick(p1: DialogInterface, p2: Int): Unit = {}
          })
          builder.setPositiveButton("Delete", new OnClickListener {
            override def onClick(p1: DialogInterface, p2: Int): Unit = {
              removeCity(position)
              dialog.dismiss()
            }
          })
          builder.show()
          true
        }
      })
      dialog.show()
      super.onOptionsItemSelected(item)
  }

  private def addCity(cityname: String) = {
    if (!mCityNames.contains(cityname)) {
      mCityNames = mCityNames ::: List(cityname)
      mSwipeAdapter.notifyDataSetChanged()
      mViewPager.setCurrentItem(mCityNames.length - 1, !Global.performanceOn)
      mDBHelper.mWrapper.addCity(cityname)
    } else Toast.makeText(WeatherActivity.this, "This city already exist", Toast.LENGTH_SHORT).show()
  }

  private def removeCity(position: Int) = {
    if (mCityNames.length > 1) {
      val curr = mCityNames(position)
      val currentPos = mViewPager.getCurrentItem
      val nextPos: Int = currentPos match {
        case i if i == mCityNames.length - 1 => mCityNames.length - 2
        case a => if (position > a) a else a - 1
      }
      mCityNames = mCityNames.diff(List(curr))
      mSwipeAdapter.notifyDataSetChanged()
      mViewPager.setCurrentItem(0, false)
      mSwipeAdapter.destroyItem(mViewPager, position, getContainerFragment(position))
      mSwipeAdapter.notifyDataSetChanged()
      mViewPager.setAdapter(null)
      mViewPager.setAdapter(mSwipeAdapter)
      mViewPager.setCurrentItem(nextPos, false)
      mDBHelper.mWrapper.deleteCity(curr)
    } else {
      Toast.makeText(WeatherActivity.this, "Can't delete the only weather", Toast.LENGTH_SHORT).show()
    }
  }

  def getCurrentContainerFragment(): WeatherDetailFragment = {
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

  import me.volhovm.mweather.WeatherLoadService._

  override def onReceiveResult(resCode: Int, resData: Bundle): Unit = resCode match {
    case STATUS_ERROR => Toast.makeText(this, "Failed to load data: " +
      resData.getString(NEW_CITY), Toast.LENGTH_LONG).show()
    case STATUS_FINISHED =>
      resData.getInt(SERVICE_MODE) match {
        case CITY_MODE =>
          val newCity = resData.getString(NEW_CITY)
          val oldCity = resData.getString(OLD_CITY)
          Toast.makeText(this, "Refreshed successfully", Toast.LENGTH_SHORT).show()
          if (mCityNames.contains(newCity) && oldCity != newCity) {
            removeCity(mCityNames.indexOf(oldCity))
            mViewPager.setCurrentItem(mCityNames.indexOf(newCity))
            Toast.makeText(this, oldCity + " is the same as " + newCity, Toast.LENGTH_SHORT).show()
          } else {
            val frag = getContainerFragment(resData.getInt(FRAGMENT_ID))
            frag.setCity(resData.getString(NEW_CITY))
            if (newCity != oldCity)
            mDBHelper.mWrapper.updateCity(oldCity, newCity)
            mCityNames = mCityNames.updated(resData.getInt(FRAGMENT_ID), newCity)
            if (frag.isAdded) {
              Log.d("WeatherActivity", "Restarting loader of fragment " + frag + " because of download result received")
              frag.getLoaderManager.restartLoader(0, null, frag).forceLoad()
            }
          }
        case COORD_MODE =>
          val city = resData.getString(NEW_CITY)
          Toast.makeText(this, "Loaded city: " + city, Toast.LENGTH_SHORT).show()
          addCity(city)
      }
    case _ => ()
  }
}
