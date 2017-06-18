package com.rincew1nd.publictransportmap.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.rincew1nd.publictransportmap.Adapters.StationListAdapter;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.MapElements.MapMarkerManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNodeType;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;
import com.rincew1nd.publictransportmap.Models.TransportNode;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
import com.rincew1nd.publictransportmap.R;

public class MarkerEditActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MarkerEditFragment fragment = new MarkerEditFragment();
        fragment.SetGraphNode(
            getIntent().getIntExtra("MarkerId", -1),
            getIntent().getBooleanExtra("IsCreate", false)
        );
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, fragment)
            .commit();
    }

    public class MarkerEditFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener {

        private GraphNode _node;
        private boolean _isCreate;

        public void SetGraphNode(int nodeId, boolean isCreate) {
            _node = GraphManager.GetInstance().Nodes.get(nodeId);
            _isCreate = isCreate;

            if (_node == null)
                _node = new GraphNode(
                        GraphNodeType.None,
                        GraphManager.GetInstance().NextNodeId(GraphManager.GetInstance().Nodes),
                        "", 0, 0
                );
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SetupSettingsDefaultValues();

            addPreferencesFromResource(R.xml.node_edit_fragment);
            FillMarkerTypes();
            SetupEditScreen();
        }

        private void SetupEditScreen() {
            // Fill default parameters
            getPreferenceManager().findPreference("marker_type").setSummary(_node.Type.toString());
            getPreferenceManager().findPreference("marker_type").setEnabled(_isCreate);
            getPreferenceManager().findPreference("marker_type").setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("marker_name").setSummary(_node.Name);
            getPreferenceManager().findPreference("marker_name").setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("marker_lat").setSummary(""+_node.Lat);
            getPreferenceManager().findPreference("marker_lat").setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("marker_lon").setSummary(""+_node.Lon);
            getPreferenceManager().findPreference("marker_lon").setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("marker_action").setSummary(
                (_isCreate) ? "Сохранить" : "Удалить"
            );
            getPreferenceManager().findPreference("marker_action").setOnPreferenceClickListener(this);

            // Fill path from and to node
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

        private void FillMarkerTypes() {
            CharSequence[] entries = new CharSequence[GraphNodeType.values().length];
            CharSequence[] entryValues = new CharSequence[GraphNodeType.values().length];
            for(int i = 0; i < GraphNodeType.values().length; i++) {
                entries[i] = GraphNodeType.values()[i].toString();
                entryValues[i] = String.valueOf(GraphNodeType.values()[i].ordinal());
            }
            ((ListPreference)getPreferenceManager().findPreference("marker_type")).setEntries(entries);
            ((ListPreference)getPreferenceManager().findPreference("marker_type")).setEntryValues(entryValues);
        }

        private void SetupSettingsDefaultValues() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor store = prefs.edit();
            store.clear();
            store.putString("marker_type", (_node == null) ?
                    String.valueOf(GraphNodeType.None) : String.valueOf(_node.Type.ordinal()));
            store.putString("marker_name", (_node == null) ? "" : _node.Name);
            store.putString("marker_lat", (_node == null) ? "0" : ""+_node.Lat);
            store.putString("marker_lon", (_node == null) ? "0" : ""+_node.Lon);
            store.apply();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "marker_type":
                    _node.Type = GraphNodeType.values()[Integer.parseInt((String)newValue)];
                    preference.setSummary(String.valueOf(_node.Type));
                    break;
                case "marker_name":
                    _node.Name = newValue.toString();
                    if (!_isCreate)
                        ((TransportNode)_node.original).Name = newValue.toString();
                    break;
                case "marker_lat":
                    _node.Lat = Float.parseFloat((String)newValue);
                    if (!_isCreate)
                        ((TransportNode)_node.original).Lat = Float.parseFloat((String)newValue);
                    break;
                case "marker_lon":
                    _node.Lon = Float.parseFloat((String)newValue);
                    if (!_isCreate)
                        ((TransportNode)_node.original).Lon = Float.parseFloat((String)newValue);
                    break;
            }
            if (!preference.getKey().equals("marker_type"))
                preference.setSummary((String)newValue);
            //OnExit
            if (!_isCreate) {
                MapMarkerManager.GetInstance().UpdateMarkerImage(_node);
                StationListActivity.StationListAdapter.FillDataset(true);
            }
            return true;
        }

        private void DeleteNode() {
            GraphManager gm = GraphManager.GetInstance();
            gm.Nodes.remove(_node);
            for(GraphPath path : gm.Paths) {
                if(path.FromNode.Id == _node.Id || path.ToNode.Id == _node.Id) {
                    gm.Paths.remove(path);
                }
            }
            switch (_node.Type) {
                case Unscheduled:
                    for (com.rincew1nd.publictransportmap.Models.Unscheduled.Path p :
                            gm.TransportGraph.UnscheduledTransport.Paths)
                        if (p.FromNode != null || p.ToNode != null ||
                            p.FromNode.Id == _node.Id || p.ToNode.Id == _node.Id)
                            gm.TransportGraph.UnscheduledTransport.Paths.remove(p);
                    for (Transfer p : gm.TransportGraph.Transfers)
                        if (p.FromNodeId == _node.Id || p.ToNodeId == _node.Id)
                            gm.TransportGraph.Transfers.remove(p);
                    gm.TransportGraph.UnscheduledTransport.Stations.remove((Station) _node.original);
                    break;
                case Scheduled:
                    //TODO удаление остановок электропоездов
                    for (Transfer p : gm.TransportGraph.Transfers)
                        if (p.FromNodeId == _node.Id || p.ToNodeId == _node.Id)
                            gm.TransportGraph.Transfers.remove(p);
                    gm.TransportGraph.ScheduledTransport.Stops.remove((TransportNode)_node.original);
                    break;
                case Walking:
                    for (com.rincew1nd.publictransportmap.Models.WalkingPaths.Path p :
                            gm.TransportGraph.WalkingPaths.Paths)
                        if (p.FromNode != null || p.ToNode != null ||
                            p.FromNode.Id == _node.Id || p.ToNode.Id == _node.Id)
                            gm.TransportGraph.UnscheduledTransport.Paths.remove(p);
                    for (Transfer p : gm.TransportGraph.Transfers)
                        if (p.FromNodeId == _node.Id || p.ToNodeId == _node.Id)
                            gm.TransportGraph.Transfers.remove(p);
                    gm.TransportGraph.WalkingPaths.Nodes.remove((TransportNode)_node.original);
                    break;
            }
            MapMarkerManager.GetInstance().SetUpMarkersAndPaths();
            StationListActivity.StationListAdapter.FillDataset(true);
        }

        private void SaveChanges() {
            Object orig;
            _node.SetColor(_node.Type, "000000");
            GraphManager.GetInstance().Nodes.put(_node.Id, _node);
            switch (_node.Type) {
                case Scheduled:
                    orig = new TransportNode(_node);
                    _node.original = orig;
                    GraphManager.GetInstance().TransportGraph.ScheduledTransport.Stops
                            .add((TransportNode)orig);
                    break;
                case Unscheduled:
                    //RouteId
                    orig = new Station(_node);
                    _node.original = orig;
                    GraphManager.GetInstance().TransportGraph.UnscheduledTransport.Stations
                            .add((Station)orig);
                    break;
                case Walking:
                    orig = new TransportNode(_node);
                    _node.original = orig;
                    GraphManager.GetInstance().TransportGraph.WalkingPaths.Nodes
                            .add((TransportNode)orig);
                    break;
            }
            MapMarkerManager.GetInstance().SetUpMarkersAndPaths();
            StationListActivity.StationListAdapter.FillDataset(true);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals("marker_action"))
                if (_isCreate)
                    SaveChanges();
                else
                    DeleteNode();
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
