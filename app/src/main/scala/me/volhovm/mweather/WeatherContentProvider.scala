package me.volhovm.mweather

import android.content.{ContentProvider, ContentValues, UriMatcher}
import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteQueryBuilder}
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils

object WeatherProvider {
  val AUTHORITY: String = "me.volhovm.mweather.WeatherProvider"
  //  val TABLE_NAME: String = "weather"
  val MAIN_CONTENT_URI: Uri = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.WEATHER_TABLE_NAME)
  val CITIES_CONTENT_URI: Uri = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.CITIES_TABLE_NAME)
  val WEATHER_ITEMS = 1
  val WEATHER_ITEM_ID = 2
  val CITIES = 3
  val CITY_ID = 4
  private val sUriMatcher: UriMatcher = new UriMatcher(0)
  sUriMatcher.addURI(AUTHORITY, DatabaseHelper.WEATHER_TABLE_NAME, WEATHER_ITEMS)
  sUriMatcher.addURI(AUTHORITY, DatabaseHelper.WEATHER_TABLE_NAME + "/#", WEATHER_ITEM_ID)
  sUriMatcher.addURI(AUTHORITY, DatabaseHelper.CITIES_TABLE_NAME, CITIES)
  sUriMatcher.addURI(AUTHORITY, DatabaseHelper.CITIES_TABLE_NAME + "/#", CITY_ID)
}

class WeatherProvider extends ContentProvider {

  import me.volhovm.mweather.WeatherProvider._

  private var mDbHelper: DatabaseHelper = null
  private var mDataBase: SQLiteDatabase = null

  override def onCreate(): Boolean = {
    mDbHelper = new DatabaseHelper(getContext)
    false
  }

  override def getType(uri: Uri): String = sUriMatcher.`match`(uri) match {
    case WEATHER_ITEMS => "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DatabaseHelper.WEATHER_TABLE_NAME
    case WEATHER_ITEM_ID => "vnd.android.cursor.item/vnd" + AUTHORITY + "." + DatabaseHelper.WEATHER_TABLE_NAME
    case CITIES => "vnd.android.cursor.item/vnd" + AUTHORITY + "." + DatabaseHelper.CITIES_TABLE_NAME
    case CITY_ID => "vnd.android.cursor.item/vnd" + AUTHORITY + "." + DatabaseHelper.CITIES_TABLE_NAME
  }

  override def update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    val ret = sUriMatcher.`match`(uri) match {
      case WEATHER_ITEMS => mDbHelper.getWritableDatabase.update(DatabaseHelper.WEATHER_TABLE_NAME, values, selection, selectionArgs)
      case WEATHER_ITEM_ID =>
        if (TextUtils.isEmpty(selection))
          mDbHelper.getWritableDatabase.update(DatabaseHelper.WEATHER_TABLE_NAME, values, BaseColumns._ID + "=" + uri.getLastPathSegment, null)
        else
          mDbHelper.getWritableDatabase.update(DatabaseHelper.WEATHER_TABLE_NAME, values, BaseColumns._ID + "=" + uri.getLastPathSegment + " and " + selection, selectionArgs)
      case CITIES => mDbHelper.getWritableDatabase.update(DatabaseHelper.CITIES_TABLE_NAME, values, selection, selectionArgs)
      case CITY_ID =>
        if (TextUtils.isEmpty(selection))
          mDbHelper.getWritableDatabase.update(DatabaseHelper.CITIES_TABLE_NAME, values, BaseColumns._ID + "=" + uri.getLastPathSegment, null)
        else
          mDbHelper.getWritableDatabase.update(DatabaseHelper.CITIES_TABLE_NAME, values, BaseColumns._ID + "=" + uri.getLastPathSegment + " and " + selection, selectionArgs)
      case _ => throw new IllegalArgumentException("URI IS WRONG: " + uri.toString)
    }
    getContext.getContentResolver.notifyChange(uri, null)
    ret
  }

  override def insert(uri: Uri, values: ContentValues): Uri = {
    val ret = sUriMatcher.`match`(uri) match {
      case WEATHER_ITEMS => Uri.parse(DatabaseHelper.WEATHER_TABLE_NAME + "/" + mDbHelper.getWritableDatabase.insert(DatabaseHelper.WEATHER_TABLE_NAME, null, values))
      case CITIES => Uri.parse(DatabaseHelper.CITIES_TABLE_NAME + "/" + mDbHelper.getWritableDatabase.insert(DatabaseHelper.CITIES_TABLE_NAME, null, values))
      case a => throw new IllegalArgumentException("URI IS WRONG: " + uri.toString)
    }
    getContext.getContentResolver.notifyChange(uri, null)
    ret
  }

  override def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
    val ret = sUriMatcher.`match`(uri) match {
      case WEATHER_ITEMS => mDbHelper.getWritableDatabase.delete(DatabaseHelper.WEATHER_TABLE_NAME, selection, selectionArgs)
      case WEATHER_ITEM_ID =>
        if (TextUtils.isEmpty(selection))
          mDbHelper.getWritableDatabase.delete(DatabaseHelper.WEATHER_TABLE_NAME, BaseColumns._ID + "=" + uri.getLastPathSegment, null)
        else
          mDbHelper.getWritableDatabase.delete(DatabaseHelper.WEATHER_TABLE_NAME, BaseColumns._ID + "=" + uri.getLastPathSegment + " and " + selection, selectionArgs)
      case CITIES => mDbHelper.getWritableDatabase.delete(DatabaseHelper.CITIES_TABLE_NAME, selection, selectionArgs)
      case CITY_ID =>
        if (TextUtils.isEmpty(selection))
          mDbHelper.getWritableDatabase.delete(DatabaseHelper.CITIES_TABLE_NAME, BaseColumns._ID + "=" + uri.getLastPathSegment, null)
        else
          mDbHelper.getWritableDatabase.delete(DatabaseHelper.CITIES_TABLE_NAME, BaseColumns._ID + "=" + uri.getLastPathSegment + " and " + selection, selectionArgs)
      case _ => throw new IllegalArgumentException("URI IS WRONG: " + uri.toString)
    }
    getContext.getContentResolver.notifyChange(uri, null)
    ret
  }

  override def query(uri: Uri, projection: Array[String], selection: String,
                     selectionArgs: Array[String], sortOrder: String): Cursor = {
    val builder = new SQLiteQueryBuilder()
    sUriMatcher.`match`(uri) match {
      case WEATHER_ITEM_ID =>
        builder.setTables(DatabaseHelper.WEATHER_TABLE_NAME)
        builder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment)
      case WEATHER_ITEMS =>
        builder.setTables(DatabaseHelper.WEATHER_TABLE_NAME)
      case CITY_ID =>
        builder.setTables(DatabaseHelper.CITIES_TABLE_NAME)
        builder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment)
      case CITIES =>
        builder.setTables(DatabaseHelper.CITIES_TABLE_NAME)
      case _ => throw new IllegalArgumentException("WRONG URI: " + uri.toString)
    }
    val cursor = builder.query(mDbHelper.getReadableDatabase, projection, selection, selectionArgs, null, null, sortOrder)
    cursor.setNotificationUri(getContext.getContentResolver, uri)
    cursor
  }

}