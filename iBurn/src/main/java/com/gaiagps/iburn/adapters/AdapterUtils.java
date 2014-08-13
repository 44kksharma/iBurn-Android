package com.gaiagps.iburn.adapters;

import android.location.Location;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.TextView;

import com.gaiagps.iburn.GeoUtils;
import com.gaiagps.iburn.PlayaClient;
import com.gaiagps.iburn.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by davidbrodsky on 8/4/14.
 */
public class AdapterUtils {

    public static ArrayList<String> mEventTypeAbbreviations = new ArrayList<>();
    public static ArrayList<String> mEventTypeNames = new ArrayList<>();

    public static ArrayList<String> mDayAbbreviations = new ArrayList<>();
    public static ArrayList<String> mDayNames = new ArrayList<>();

    static {
        mDayNames        .add("All Days");
        mDayAbbreviations.add(null);
        mDayNames        .add("Monday 8/25");
        mDayAbbreviations.add("8/25");
        mDayNames        .add("Tuesday 8/26");
        mDayAbbreviations.add("8/26");
        mDayNames        .add("Wednesday 8/27");
        mDayAbbreviations.add("8/27");
        mDayNames        .add("Thursday 8/28");
        mDayAbbreviations.add("8/28");
        mDayNames        .add("Friday 8/29");
        mDayAbbreviations.add("8/29");
        mDayNames        .add("Saturday 8/30");
        mDayAbbreviations.add("8/30");
        mDayNames        .add("Sunday 8/31");
        mDayAbbreviations.add("8/31");
        mDayNames        .add("Monday 9/1");
        mDayAbbreviations.add("9/1");
        mDayNames        .add("Monday 9/2");
        mDayAbbreviations.add("9/2");

        mEventTypeAbbreviations.add("work");
        mEventTypeNames        .add("Work");
        mEventTypeAbbreviations.add("game");
        mEventTypeNames        .add("Game");
        mEventTypeAbbreviations.add("adlt");
        mEventTypeNames        .add("Adult");
        mEventTypeAbbreviations.add("prty");
        mEventTypeNames        .add("Party");
        mEventTypeAbbreviations.add("perf");
        mEventTypeNames        .add("Performance");
        mEventTypeAbbreviations.add("kid");
        mEventTypeNames        .add("Kid");
        mEventTypeAbbreviations.add("food");
        mEventTypeNames        .add("Food");
        mEventTypeAbbreviations.add("cere");
        mEventTypeNames        .add("Ceremony");
        mEventTypeAbbreviations.add("care");
        mEventTypeNames        .add("Care");
        mEventTypeAbbreviations.add("fire");
        mEventTypeNames        .add("Fire");
    }

    public static String getStringForEventType(String typeAbbreviation) {
        if (typeAbbreviation == null) return null;
        if (mEventTypeAbbreviations.contains(typeAbbreviation))
            return mEventTypeNames.get(mEventTypeAbbreviations.indexOf(typeAbbreviation));
        return null;
    }

    public static double setDistanceText(Location deviceLocation, TextView textView, double lat, double lon) {
        return setDistanceText(deviceLocation, null, null, null, textView, lat, lon);
    }

    /**
     * Get stylized distance text describing the difference between the given
     * device location and a given Latitude and Longitude. The unique
     * method signature owes itself to the precise data available to
     * a {@link com.gaiagps.iburn.adapters.PlayaItemCursorAdapter}
     *
     * @return a time estimate in minutes.
     */
    public static double setDistanceText(Location deviceLocation, Date nowDate, String startDateStr, String endDateStr, TextView textView, double lat, double lon) {
        if (deviceLocation != null && lat != 0) {
            double milesToTarget = (GeoUtils.getDistance(lat, lon, deviceLocation)) * 0.000621371; // meters to miles
            double minutesToTarget = PlayaClient.getWalkingEstimateMinutes(milesToTarget);
            String distanceText;

            try {
                Spannable spanRange;

                if (milesToTarget < 0.01) {
                    distanceText = "<1 m danger close";
                    spanRange = new SpannableString(distanceText);
                } else {
                    distanceText = String.format("%.0f min %.2f miles", minutesToTarget, milesToTarget);
                    int endSpan = distanceText.length();
                    spanRange = new SpannableString(distanceText);
                    TextAppearanceSpan tas = new TextAppearanceSpan(textView.getContext(), R.style.Subdued);
                    spanRange.setSpan(tas, distanceText.indexOf("min") + 3, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (nowDate != null && startDateStr != null && endDateStr != null) {
                    // If a date is given, attempt to do coloring of the time estimate (e.g: green if arrival estimate before start date)
                    Date startDate = PlayaClient.parseISODate(startDateStr);
                    Date endDate = PlayaClient.parseISODate(endDateStr);
                    long duration = endDate.getTime() - startDate.getTime();

                    if (startDate.before(nowDate) && endDate.after(nowDate)) {
                        // Event already started
                        long timeLeftMinutes = ( endDate.getTime() - nowDate.getTime() ) / 1000 / 60;
                        if ( (timeLeftMinutes - minutesToTarget) >= duration / 2.0f) {
                            // If we'll make at least half the event, Color it yellow
                            int endSpan = distanceText.indexOf("min") + 3;
                            spanRange = new SpannableString(distanceText);
                            TextAppearanceSpan tas = new TextAppearanceSpan(textView.getContext(), R.style.OrangeText);
                            spanRange.setSpan(tas, 0, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } else if (endDate.after(nowDate) && startDate.before(nowDate)) {
                        long timeUntilStartMinutes = ( startDate.getTime() - nowDate.getTime() ) / 1000 / 60;
                        if ( (timeUntilStartMinutes - minutesToTarget) > 0) {
                            // If we'll make the event start, Color it green
                            int endSpan = distanceText.indexOf("min") + 3;
                            TextAppearanceSpan tas = new TextAppearanceSpan(textView.getContext(), R.style.GreenText);
                            spanRange.setSpan(tas, 0, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }

                textView.setText(spanRange);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // If minutes < startDate || minutes < (endDate - now)
            textView.setVisibility(View.VISIBLE);
            return minutesToTarget;
        } else {
            textView.setText("");
            textView.setVisibility(View.GONE);
            return -1;
        }
    }
}
