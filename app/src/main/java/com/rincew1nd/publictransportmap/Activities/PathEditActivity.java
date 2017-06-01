package com.rincew1nd.publictransportmap.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.MapElements.MapMarkerManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.TransportNode;
import com.rincew1nd.publictransportmap.R;

public class PathEditActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PathEditFragment fragment = new PathEditFragment();
        fragment.SetGraphPath(GraphManager.GetInstance()
                .Paths.get(getIntent().getExtras().getInt("PathPos")));
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, fragment)
            .commit();
    }

    public static class PathEditFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener{

        private GraphPath _path;

        public void SetGraphPath(GraphPath path) { _path = path; }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SetupSettingsDefaultValues();

            addPreferencesFromResource(R.xml.path_edit_fragment);
            SetupPreferenceScreen();
        }

        private void SetupPreferenceScreen() {
            getPreferenceManager().findPreference("path_from").setSummary(_path.FromNode.Name);
            getPreferenceManager().findPreference("path_to").setSummary(_path.ToNode.Name);
            getPreferenceManager().findPreference("path_time").setSummary(""+_path.Time);
            getPreferenceManager().findPreference("path_time").setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("path_cost").setSummary(""+_path.Cost);
            getPreferenceManager().findPreference("path_cost").setOnPreferenceChangeListener(this);
        }

        private void SetupSettingsDefaultValues() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor store = prefs.edit();
            store.clear();
            store.putString("path_from", ""+_path.FromNode.Id);
            store.putString("path_to", ""+_path.ToNode.Id);
            store.putString("path_time", ""+_path.Time);
            store.putString("path_cost", ""+_path.Cost);
            store.apply();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            //switch (preference.getKey()) {
            //    case "marker_name":
            //        _node.Name = newValue.toString();
            //        ((TransportNode)_node.original).Name = newValue.toString();
            //        break;
            //    case "marker_lat":
            //        _node.Lat = Float.parseFloat((String)newValue);
            //        ((TransportNode)_node.original).Lat = Float.parseFloat((String)newValue);
            //        break;
            //    case "marker_lon":
            //        _node.Lon = Float.parseFloat((String)newValue);
            //        ((TransportNode)_node.original).Lon = Float.parseFloat((String)newValue);
            //        break;
            //}
            //preference.setSummary((String)newValue);
            //MapMarkerManager.GetInstance().UpdateMarkerImage(_node);
            return true;
        }
    }
}
