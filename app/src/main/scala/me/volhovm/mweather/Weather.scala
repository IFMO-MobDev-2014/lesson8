package me.volhovm.mweather

import java.util.Date

import android.content.ContentValues

class Weather(val city: String,
              val country: String,
              val temp: (Int, Int),
              val weatherState: WeatherState,
              val humidity: Double,
              val pressure: Int, //mmhg
              val wind: String,
              val date: Date) {
  private def wrapTemp(i: Int): String = if (i > 0) "+" + i.toString else if (i == 0) " " + i.toString else i.toString
  def lowTemp = wrapTemp(temp._1)
  def highTemp = wrapTemp(temp._2)
  def getValues = {
    import me.volhovm.mweather.DatabaseHelper._
    val values: ContentValues = new ContentValues()
    values.put(WEATHER_CITY, city)
    values.put(WEATHER_COUNTRY, country)
    values.put(WEATHER_HIGH_TEMP, Int.box(temp._1))
    values.put(WEATHER_LOW_TEMP, Int.box(temp._2))
    values.put(WEATHER_STATE_CODE, Int.box(weatherState.getCode))
    values.put(WEATHER_STATE_DESC, weatherState.getDesc)
    values.put(WEATHER_HUMIDITY, Int.box((humidity * 100).toInt))
    values.put(WEATHER_PRESSURE, Int.box(pressure))
    values.put(WEATHER_WIND, wind)
    values.put(WEATHER_DATE, Int.box((date.getTime / 1000).toInt))
    values
  }
}

class WeatherState(code: Int, desc: String) {
  def getBackground: Int = code match {
    case a if a == 210 | a == 230 | a == 200 => R.drawable.background_thunderstorm_0
    case a if a < 300 && a >= 200 => R.drawable.background_thunderstorm_1
    case a if a == 300 | a == 301 | a == 310 => R.drawable.background_rain_0
    case a if a == 302 | a == 314 | a == 321 => R.drawable.background_rain_2
    case a if a == 500 | a == 501 | a == 511 | a == 520 => R.drawable.background_rain_0
    case a if a == 502 | a == 504 | a == 521 | a == 522 | a == 531 => R.drawable.background_rain_2
    case a if a > 300 && a < 600 => R.drawable.background_rain_1
    case a if a == 600 | a == 601 | a == 611 | a == 620 => R.drawable.background_snow_0
    case a if a == 622 | a == 602 | a == 612 => R.drawable.background_snow_2
    case a if a == 615 | a == 616 => R.drawable.background_snow_rain_0
    case a if a > 600 && a < 700 => R.drawable.background_snow_1
    case a if a == 800 | a == 801 => R.drawable.background_clouds_0
    case 802 => R.drawable.background_clouds_1
    case 803 => R.drawable.background_clouds_2
    case 804 => R.drawable.background_clouds_3
    case _ => R.drawable.background_unrecognized
  }
  def getIcon: Int = code match {
    case a if a < 300 && a >= 200 => R.drawable.icon_lightening
    case a if a == 300 | a == 301 | a == 310 => R.drawable.icon_rain
    case a if a == 302 | a == 314 | a == 321 => R.drawable.icon_rain
    case a if a == 500 | a == 501 | a == 511 | a == 520 => R.drawable.icon_rain
    case a if a == 502 | a == 504 | a == 521 | a == 522 | a == 531 => R.drawable.icon_rain
    case a if a > 300 && a < 600 => R.drawable.icon_shower
    case a if a == 600 | a == 601 | a == 611 | a == 612 | a == 620 => R.drawable.icon_snow
    case a if a == 615 | a == 616 => R.drawable.icon_rain
    case a if a > 600 && a < 700 => R.drawable.icon_snow
    case a if a == 800 | a == 801 => R.drawable.icon_sun
    case 802 => R.drawable.icon_mostly_cloudy
    case 803 => R.drawable.icon_mostly_cloudy
    case 804 => R.drawable.icon_cloudy
    case _ => R.drawable.icon_cloudy
  }
  def getDesc = desc
  def getCode = code
  //TODO: fill it
}

