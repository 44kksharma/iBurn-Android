package com.gaiagps.iburn;

import android.location.Location;

/**
 * Created by davidbrodsky on 8/4/14.
 */
public class Geo {

    public static final double MAN_LAT = 40.788800;
    public static final double MAN_LON = -119.203150;

    private static float[] sResult = new float[1];

    /**
     * @return Returns the distance between a start and end Location in meters
     */
    public static double getDistance(double startLat, double startLon, Location end) {
        Location.distanceBetween(startLat, startLon, end.getLatitude(), end.getLongitude(), sResult);
        return sResult[0];
    }

    /**
     * @return a walking estimate in minutes for a distance in meters
     */
    public static double getWalkingEstimateMinutes(double meters) {
        return 60 * (meters / 5000); // At 5 kph
    }

    /**
     * @return a bicycling estimate in minutes for a distance in meters
     */
    public static double getBikingEstimateMinutes(double meters) {
        return 60 * (meters / 15500); // At 15.5 kph
    }

}
