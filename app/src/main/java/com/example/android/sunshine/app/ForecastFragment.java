package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

import java.util.Date;

/**
 * Created by hector on 27/08/14.
 */
public class ForecastFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int COL_WEATHER_ID       = 0;
    public static final int COL_WEATHER_DATE     = 1;
    public static final int COL_WEATHER_DESC     = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;

    private ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( mLocation != null && !Utility.getPreferredLocation(getActivity()).equals(mLocation) ) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Cursor cursor = mForecastAdapter.getCursor();

                if ( cursor != null && cursor.moveToPosition(position) ) {
                    Intent mIntent = new Intent(getActivity(),DetailActivity.class)
                            .putExtra(DetailActivity.DATE_KEY, cursor.getString(COL_WEATHER_DATE));

                    startActivity(mIntent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.action_refresh){
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void updateWeather() {
        String location = Utility.getPreferredLocation(getActivity());
        Log.v("updateWeather()", "Creando FetchWeatherTask");
        new FetchWeatherTask(getActivity()).execute(location);
    }

    // LoaderCallbacks<Cursor>

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        String starDate = WeatherContract.getDbDateString(new Date());

        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());

        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation,
                starDate
        );

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mForecastAdapter.swapCursor(null);
    }

    // FIN: LoaderCallbacks<Cursor>
}
