package com.scenicgo.utils;

/**
 * 地理计算。景区尺度下用球面近似就够，不引 GIS 库。
 */
public class GeoUtils {

    /** 地球平均半径（米） */
    private static final double EARTH_RADIUS_METERS = 6_371_000D;

    private GeoUtils() {
    }

    /** 两点间的球面距离（米），Haversine 公式 */
    public static double distanceMeters(double longitude1, double latitude1,
                                       double longitude2, double latitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double deltaLat = lat2 - lat1;
        double deltaLng = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }
}
