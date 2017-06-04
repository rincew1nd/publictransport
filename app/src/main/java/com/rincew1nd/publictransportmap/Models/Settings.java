package com.rincew1nd.publictransportmap.Models;

import android.content.Context;
import android.preference.PreferenceManager;

import com.rincew1nd.publictransportmap.R;

public class Settings {
    public static int FromStationId = -1;
    public static int ToStationId = -1;

    public static boolean FromStationSelect = false;
    public static boolean ToStationSelect = false;

    public static int SearchDepth;
    public static int MapStyleResourceId;

    public static void LoadSettings(Context context) {
        SearchDepth = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getString("algorithm_depth", "2"));
        MapStyleResourceId =
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt("map_style_id", R.raw.map_style);
    }
}
