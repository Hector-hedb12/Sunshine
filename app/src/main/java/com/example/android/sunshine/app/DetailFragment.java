package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

/**
 * Created by hector on 04/10/14.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOCATION_KEY = "location";

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private String mLocation;
    private String mForecast;

    private static final int DETAIL_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // not lose mLocation when change orientation
        outState.putString(LOCATION_KEY, mLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( mLocation != null && !Utility.getPreferredLocation(getActivity()).equals(mLocation) ) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v(LOG_TAG, "in onCreateOptionsMenu");

        inflater.inflate(R.menu.detailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);

        return shareIntent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);

        if ( savedInstanceState != null ) {
            // recover mLocation
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "in onCreateLoader");

        Intent intent = getActivity().getIntent();

        if (intent == null || !intent.hasExtra(DetailActivity.DATE_KEY) ) {
            return null;
        }

        String forecastDate = intent.getStringExtra(DetailActivity.DATE_KEY);

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, forecastDate);

        Log.v(LOG_TAG, weatherForLocationUri.toString());

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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "in onLoadFinished");

        if ( data != null && data.moveToFirst() ) {

            int weatherId = data.getInt(data.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            String date = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT));
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);

            mFriendlyDateView.setText(friendlyDateText);
            mDateView.setText(dateText);

            String description = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            mDescriptionView.setText(description);

            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
            String highString = Utility.formatTemperature(getActivity(), high, isMetric);
            mHighTempView.setText(highString);

            double low = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
            String lowString = Utility.formatTemperature(getActivity(), low, isMetric);
            mLowTempView.setText(lowString);

            float humidity = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY));
            mHumidityView.setText(getActivity().getString(R.string.format_humitidy, humidity));

            float windSpeedStr = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED));
            float windDirStr = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_DEGREES));
            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

            float pressure = data.getFloat(data.getColumnIndex(WeatherEntry.COLUMN_PRESSURE));
            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

            // for the share intent
            mForecast = String.format("%s - %s - %s / %s", dateText, description, high, low);
            Log.v(LOG_TAG, "Forecast String: " + mForecast);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now
            if ( mShareActionProvider != null ) {
                mShareActionProvider.setShareIntent( createShareForecastIntent() );
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) { }
}