package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.e(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        Log.e(LOG_TAG, "onStart");
        super.onStart();
    }
    @Override
    protected void onResume() {
        Log.e(LOG_TAG, "onResume");
        super.onResume();
    }
    @Override
    protected void onPause() {
        Log.e(LOG_TAG, "onPause");
        super.onPause();
    }
    @Override
    protected void onStop() {
        Log.e(LOG_TAG, "onStop");
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Log.e(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            startActivity(new Intent(this,SettingsActivity.class));

            return true;
        }

        if (id == R.id.action_show_location){
            showPreferredLocation();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showPreferredLocation() {
        final String MAP_BASE_URI = "geo:0,0?";
        final String QUERY_PARAM = "q";

        SharedPreferences mPreference = PreferenceManager.getDefaultSharedPreferences(this);
        String location = mPreference.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        Uri builtUri = Uri.parse(MAP_BASE_URI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(builtUri);

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        } else {
            Log.e(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed");
        }
    }
}
