package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.sunshine.app.data.WeatherContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hector on 16/09/14.
 */
public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric)
        ).equals(context.getString(R.string.pref_units_metric));
    }

    static String formatTemperature(Context context, double temperature, boolean isMetric){
        double temp;

        if ( !isMetric ){
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }

        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(String dateString){
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }

    // is the format used to store dates in database.
    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String getFriendlyDayString(Context context, String dateStr) {
        // Results:
        // today: "Today, June 8"
        // tomorrow: "Tomorrow"
        // Next five days: "Wednesday" (or name that correspond)
        // rest of days: "Mon Jun 8" (after one week for now)

        Date todayDate = new Date();
        String todayStr = WeatherContract.getDbDateString(todayDate);
        Date inputDate = WeatherContract.getDateFromDb(dateStr);

        // If input date is today
        if ( todayStr.equals(dateStr) ) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;

            return String.format(
                    context.getString(formatId,today,getFormattedMonthDay(context, dateStr))
            );
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = WeatherContract.getDbDateString(cal.getTime());

            if ( dateStr.compareTo(weekFutureString) < 0 ) {
                return getDayName(context, dateStr);
            } else {
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    // Return name of a day or 'today' or 'tomorrow'
    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);

        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();

            if (WeatherContract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);

                Date tomorrowDate = cal.getTime();

                if (WeatherContract.getDbDateString(tomorrowDate).equals(dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    // Return String in format 'Month day'
    public static String getFormattedMonthDay(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            return  monthDayFormat.format(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFormattedWind(Context context, float windSpeed, float degress) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        String direction = "Unknown";

        if (degress >= 337.5 || degress < 22.5) {
            direction = "N";
        } else if(degress >= 22.5 && degress < 67.5) {
            direction = "NE";
        } else if (degress >= 67.5 && degress < 112.5) {
            direction = "E";
        } else if (degress >= 112.5 && degress < 157.5) {
            direction = "SE";
        } else if (degress >= 157.5 && degress < 202.5) {
            direction = "S";
        } else if (degress >= 202.5 && degress < 247.5) {
            direction = "SW";
        } else if (degress >= 247.5 && degress < 292.5) {
            direction = "W";
        } else if (degress >= 292.5 || degress < 22.5) {
            direction = "NW";
        }

        return String.format(context.getString(windFormat), windSpeed, direction);
    }
}
