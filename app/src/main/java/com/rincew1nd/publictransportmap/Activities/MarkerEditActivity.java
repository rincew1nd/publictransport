package com.rincew1nd.publictransportmap.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.MapElements.MapMarkerManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.TransportNode;
import com.rincew1nd.publictransportmap.R;

public class MarkerEditActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MarkerEditFragment fragment = new MarkerEditFragment();
        fragment.SetGraphNode(
                GraphManager.GetInstance().Nodes.get(getIntent().getIntExtra("MarkerId", -1))
        );
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, fragment)
            .commit();
    }

    public static class MarkerEditFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener {

        private GraphNode _node;

        public void SetGraphNode(GraphNode node) { _node = node; }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SetupSettingsDefaultValues();

            addPreferencesFromResource(R.xml.node_edit_fragment);
            SetupPreferenceScreen();
        }

        private void SetupPreferenceScreen() {
            getPreferenceManager().findPreference("marker_name").setSummary(_node.Name);
            getPreferenceManager().findPreference("marker_name").setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("marker_lat").setSummary(""+_node.Lat);
            getPreferenceManager().findPreference("marker_lat").setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("marker_lon").setSummary(""+_node.Lon);
            getPreferenceManager().findPreference("marker_lon").setOnPreferenceChangeListener(this);

            PreferenceCategory pathCategoryFrom =
                    (PreferenceCategory) getPreferenceManager().findPreference("marker_paths_from");
            PreferenceCategory pathCategoryTo =
                    (PreferenceCategory) getPreferenceManager().findPreference("marker_paths_to");
            for(int i = 0; i<GraphManager.GetInstance().Paths.size(); i++) {
                GraphPath path = GraphManager.GetInstance().Paths.get(i);
                if(path.FromNode.Id == _node.Id || path.ToNode.Id == _node.Id) {
                    Preference pref = new Preference(getActivity());
                    pref.setKey("path_"+i);
                    pref.setOnPreferenceClickListener(this);
                    if (path.FromNode.Id == _node.Id) {
                        pref.setTitle(path.ToNode.Name);
                        pathCategoryFrom.addPreference(pref);
                    } else {
                        pref.setTitle(path.FromNode.Name);
                        pathCategoryTo.addPreference(pref);
                    }
                }
            }
        }

        private void SetupSettingsDefaultValues() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor store = prefs.edit();
            store.clear();
            store.putString("marker_name", _node.Name);
            store.putString("marker_lat", ""+_node.Lat);
            store.putString("marker_lon", ""+_node.Lon);
            store.apply();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "marker_name":
                    _node.Name = newValue.toString();
                    ((TransportNode)_node.original).Name = newValue.toString();
                    break;
                case "marker_lat":
                    _node.Lat = Float.parseFloat((String)newValue);
                    ((TransportNode)_node.original).Lat = Float.parseFloat((String)newValue);
                    break;
                case "marker_lon":
                    _node.Lon = Float.parseFloat((String)newValue);
                    ((TransportNode)_node.original).Lon = Float.parseFloat((String)newValue);
                    break;
            }
            preference.setSummary((String)newValue);
            MapMarkerManager.GetInstance().UpdateMarkerImage(_node);
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().contains("path_")) {
                int pathPos = Integer.parseInt(preference.getKey().substring(5));
                Intent i = new Intent(getActivity(), PathEditActivity.class);
                i.putExtra("PathPos", pathPos);
                getActivity().startActivity(i);
            }
            return false;
        }
    }
}
