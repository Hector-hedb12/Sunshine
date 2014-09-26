package com.example.android.sunshine.app.test;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

import java.util.Map;
import java.util.Set;

/**
 * Created by hector on 12/09/14
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
        setUp();
    }

    public void testInsertReadProvider(){

        ContentValues testValues = createLocationValues();

        Uri locationInsertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);

        long locationRowId = ContentUris.parseId(locationInsertUri);

        assertTrue(locationRowId != -1);

        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // location
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        validateCursor(cursor, testValues);

        // location_id
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null
        );

        validateCursor(cursor, testValues);


        // Segundo test:
        ContentValues weatherValues = createWeatherValues(locationRowId);

        Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);

        assertTrue(weatherInsertUri != null);

        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        validateCursor(weatherCursor, weatherValues);

        // agrega los valores de testValues (localizacion) a weatherValues (clima)
        // Esto asegura que el validador del cursor compare todos las columnas
        // incluyendo las de localizacion producto del JOIN
        addAllContentValues(weatherValues, testValues);

        // Solo localizacion
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                null,
                null,
                null,
                null
        );

        validateCursor(weatherCursor, weatherValues);

        // localizacion + fecha inicial
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION, TEST_DATE),
                null,
                null,
                null,
                null
        );

        validateCursor(weatherCursor, weatherValues);

        // localizacion + dia
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE),
                null,
                null,
                null,
                null
        );

        validateCursor(weatherCursor, weatherValues);
    }

    public void testGetType(){
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testUpdateLocation(){
        // Insertar
        ContentValues values = createLocationValues();

        Uri locationUri = mContext.getContentResolver().insert( LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Actualizar
        ContentValues updateValues = new ContentValues(values);
        updateValues.put(LocationEntry._ID, locationRowId);
        updateValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI,
                updateValues,
                LocationEntry._ID + " = ?",
                new String[] {Long.toString(locationRowId)}
        );

        assertEquals(count, 1);

        // Comprobacion de actualizacion
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null
        );

        validateCursor(cursor, updateValues);
    }

    public void testDeleteRecordsAtEnd(){
        deleteAllRecords();
    }

    static ContentValues createWeatherValues(long locationRowId) {

        ContentValues weatherValues = new ContentValues();

        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    static public String TEST_CITY_NAME = "North Pole";
    static public String TEST_LOCATION = "99705";
    static public String TEST_DATE = "20141205";

    static ContentValues createLocationValues() {

        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, 64.772);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, -147.355);

        return values;
    }

    private void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for(Map.Entry<String, Object> entry : valueSet){
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);

            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }

        valueCursor.close();
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source){
        for (String key : source.keySet()){
            destination.put(key,source.getAsString(key));
        }
    }

    public void setUp(){
        deleteAllRecords();
    }

    private void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(0, cursor.getCount());
        cursor.close();
    }







    // Additional test suite for testing content provider delete and update

    static final String KALAMAZOO_LOCATION_SETTING = "kalamazoo";
    static final String KALAMAZOO_WEATHER_START_DATE = "20140625";

    long locationRowId;

    static ContentValues createKalamazooWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, KALAMAZOO_WEATHER_START_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.5);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 85);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 35);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Cats and Dogs");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 3.4);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 42);

        return weatherValues;
    }

    static ContentValues createKalamazooLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, KALAMAZOO_LOCATION_SETTING);
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "Kalamazoo");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 42.2917);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -85.5872);

        return testValues;
    }


    // Inserts both the location and weather data for the Kalamazoo data set.
    public void insertKalamazooData() {
        ContentValues kalamazooLocationValues = createKalamazooLocationValues();
        Uri locationInsertUri = mContext.getContentResolver()
                .insert(LocationEntry.CONTENT_URI, kalamazooLocationValues);
        assertTrue(locationInsertUri != null);

        locationRowId = ContentUris.parseId(locationInsertUri);

        ContentValues kalamazooWeatherValues = createKalamazooWeatherValues(locationRowId);
        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, kalamazooWeatherValues);
        assertTrue(weatherInsertUri != null);
    }

    public void testUpdateAndReadWeather() {
        insertKalamazooData();
        String newDescription = "Cats and Frogs (don't warn the tadpoles!)";

        // Make an update to one value.
        ContentValues kalamazooUpdate = new ContentValues();
        kalamazooUpdate.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

        mContext.getContentResolver().update(
                WeatherEntry.CONTENT_URI, kalamazooUpdate, null, null);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make the same update to the full ContentValues for comparison.
        ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
        kalamazooAltered.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

        validateCursor(weatherCursor, kalamazooAltered);
    }

    public void testRemoveHumidityAndReadWeather() {
        insertKalamazooData();

        mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,
                WeatherEntry.COLUMN_HUMIDITY + " = " + locationRowId, null);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make the same update to the full ContentValues for comparison.
        ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
        kalamazooAltered.remove(WeatherEntry.COLUMN_HUMIDITY);

        validateCursor(weatherCursor, kalamazooAltered);
    }

}
