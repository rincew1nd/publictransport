package com.rincew1nd.publictransportmap.Activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.rincew1nd.publictransportmap.Models.Settings;
import com.rincew1nd.publictransportmap.R;

public class SettingsActivity extends PreferenceActivity {

    public static GoogleMap Map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, new SettingsFragment(Map))
            .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener {

        private GoogleMap _map;

        public SettingsFragment(GoogleMap map) {
            _map = map;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_fragment);

            getPreferenceManager().findPreference("map_type")
                    .setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("algorithm_depth")
                    .setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("algorithm_depth")
                    .setSummary(String.valueOf(Settings.SearchDepth));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "map_type":
                    int mapStyle = 0;
                    switch ((String) newValue) {
                        case "none":
                            mapStyle = -1;
                            break;
                        case "default":
                            mapStyle = R.raw.map_style;
                            break;
                        case "silver":
                            mapStyle = R.raw.map_style_silver;
                            break;
                        case "retro":
                            mapStyle = R.raw.map_style_retro;
                            break;
                    }
                    if (mapStyle == -1) {
                        _map.setMapType(GoogleMap.MAP_TYPE_NONE);
                    } else {
                        _map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        _map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), mapStyle));
                    }
                    getPreferenceManager().findPreference("map_type").setSummary((String) newValue);
                    break;
                case "algorithm_depth":
                    Settings.SearchDepth = Integer.parseInt((String)newValue);
                    getPreferenceManager().findPreference("algorithm_depth").setSummary((String)newValue);
            }
            return false;
        }
    }
}
