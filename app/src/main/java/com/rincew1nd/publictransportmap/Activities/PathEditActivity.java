package com.rincew1nd.publictransportmap.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.MapElements.MapMarkerManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNodeType;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Settings;
import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;
import com.rincew1nd.publictransportmap.Models.TransportNode;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Path;
import com.rincew1nd.publictransportmap.R;

public class PathEditActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PathEditFragment fragment = new PathEditFragment();
        fragment.SetGraphPath(
            getIntent().getExtras().getInt("PathPos", -2),
            getIntent().getExtras().getBoolean("IsCreate", false)
        );
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, fragment)
            .commit();
    }

    public static class PathEditFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener {

        private GraphPath _path;
        private boolean _isCreate;
        private boolean isFromStation;

        public void SetGraphPath(int pathId, boolean isCreate) {
            _isCreate = isCreate;
            if (pathId == -2)
                _path = new GraphPath(GraphNodeType.None, -1, -1, 0, 0, null);
            else
                _path = GraphManager.GetInstance().Paths.get(pathId);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SetupSettingsDefaultValues();

            addPreferencesFromResource(R.xml.path_edit_fragment);
            SetupPreferenceScreen();
        }

        private void SetupPreferenceScreen() {
            if (_isCreate) {
                getPreferenceManager().findPreference("path_from").setSummary("None");
                getPreferenceManager().findPreference("path_from").setOnPreferenceClickListener(this);
                getPreferenceManager().findPreference("path_to").setSummary("None");
                getPreferenceManager().findPreference("path_to").setOnPreferenceClickListener(this);
            } else {
                getPreferenceManager().findPreference("path_from").setSummary(_path.FromNode.Name);
                getPreferenceManager().findPreference("path_from").setEnabled(false);
                getPreferenceManager().findPreference("path_to").setSummary(_path.ToNode.Name);
                getPreferenceManager().findPreference("path_to").setEnabled(false);
            }
            getPreferenceManager().findPreference("path_time").setSummary(""+_path.Time);
            getPreferenceManager().findPreference("path_time").setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("path_cost").setSummary(""+_path.Cost);
            getPreferenceManager().findPreference("path_cost").setOnPreferenceChangeListener(this);
            getPreferenceManager().findPreference("path_action").setSummary(
                    (_isCreate) ? "Сохранить" : "Удалить"
            );
            getPreferenceManager().findPreference("path_action").setOnPreferenceClickListener(this);

            if (_path.Type != GraphNodeType.Scheduled)
                getPreferenceScreen().findPreference("path_cost").setEnabled(false);
        }

        private void SetupSettingsDefaultValues() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor store = prefs.edit();
            store.clear();
            store.putString("path_from", ""+((_path.FromNode == null) ? -2 : _path.FromNode.Id));
            store.putString("path_to", ""+((_path.ToNode == null) ? -2 : _path.ToNode.Id));
            store.putString("path_time", ""+_path.Time);
            store.putString("path_cost", ""+_path.Cost);
            store.apply();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "path_time":
                    _path.Time = Integer.parseInt((String)newValue);
                    if (!_isCreate)
                        ((Transfer)_path.original).Time = _path.Time;
                    break;
                case "path_cost":
                    _path.Cost = Integer.parseInt((String)newValue);
                    if (!_isCreate)
                        ((Transfer)_path.original).Cost = _path.Cost;
                    break;
            }
            preference.setSummary((String)newValue);
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals("path_from")) {
                isFromStation = true;
                this.startActivityForResult(
                    new Intent(getActivity(), StationListActivity.class), 1
                );
            }
            if (preference.getKey().equals("path_to")) {
                isFromStation = false;
                this.startActivityForResult(
                    new Intent(getActivity(), StationListActivity.class), 1
                );
            }
            if (preference.getKey().equals("path_action"))
                if (_isCreate)
                    CreatePath();
                else
                    DeletePath();
            return false;
        }

        private void DeletePath() {
        }

        private void CreatePath() {
            GraphManager gm = GraphManager.GetInstance();
            if (_path.FromNode != null && _path.ToNode != null) {
                GraphPath path = null;
                for (GraphPath path1 : gm.Paths)
                    if (path1.FromNode.Id == _path.FromNode.Id &&
                            path1.ToNode.Id == _path.ToNode.Id) {
                        path = path1;
                        break;
                    }
                if (path != null) {
                    Toast.makeText(getActivity(), "Данный путь уже существует", Toast.LENGTH_SHORT).show();
                    return;
                }

                gm.Paths.add(_path);
                _path.Type = (_path.FromNode.Type == _path.ToNode.Type) ?
                        _path.FromNode.Type : GraphNodeType.None;
                switch (_path.Type) {
                    case None:
                        Transfer tr = new Transfer(_path);
                        _path.original = tr;
                        gm.TransportGraph.Transfers.add(tr);
                        break;
                    case Unscheduled:
                        com.rincew1nd.publictransportmap.Models.Unscheduled.Path upath =
                                new com.rincew1nd.publictransportmap.Models.Unscheduled.Path(_path);
                        _path.original = upath;
                        gm.TransportGraph.UnscheduledTransport.Paths.add(upath);
                        break;
                    case Walking:
                        com.rincew1nd.publictransportmap.Models.WalkingPaths.Path wpath =
                                new com.rincew1nd.publictransportmap.Models.WalkingPaths.Path(_path);
                        _path.original = wpath;
                        gm.TransportGraph.WalkingPaths.Paths.add(wpath);
                        break;
                }
                MapMarkerManager.GetInstance().SetUpMarkersAndPaths();
                StationListActivity.StationListAdapter.FillDataset(true);
            } else {
                Toast.makeText(getActivity(), "Заполните остановки прибытия и отбытия", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (isFromStation) {
                _path.FromNode = GraphManager.GetInstance().Nodes.get(Settings.LastSelectedStation);
                getPreferenceManager().findPreference("path_from").setSummary(_path.FromNode.Name);
            } else {
                _path.ToNode = GraphManager.GetInstance().Nodes.get(Settings.LastSelectedStation);
                getPreferenceManager().findPreference("path_to").setSummary(_path.ToNode.Name);
            }
        }
    }
}
