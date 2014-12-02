package lesson8.android.weather

import java.util.Date

import android.util.Xml

object Weather {
  def getWeather(xml: Xml): Weather = ???
  def getForecast(xml: Xml): Array[Weather] = ???
}

class Weather(val city: String,
              val country: String,
              val temp: String,
              val tempUnit: String,
              val cloudness: String,
              val humidity: String,
              val pressure: String,
              val wind: String,
              val date: Date) {
}

