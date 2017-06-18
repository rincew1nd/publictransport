package com.rincew1nd.publictransportmap.Activities;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.rincew1nd.publictransportmap.Adapters.StationListAdapter;
import com.rincew1nd.publictransportmap.Models.Settings;
import com.rincew1nd.publictransportmap.Models.StationListItem;
import com.rincew1nd.publictransportmap.R;

public class StationListActivity extends ListActivity implements View.OnClickListener {

    public static StationListAdapter StationListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_list);
        initializeAdapter();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        StationListItem item = (StationListItem) getListView().getAdapter().getItem(position);
        if (item != null && item.type == StationListItem.ITEM) {
            Settings.LastSelectedStation = item.id;
            if (Settings.FromStationSelect) {
                Settings.FromStationId = item.id;
                Settings.FromStationSelect = false;
            }
            else if (Settings.ToStationSelect) {
                Settings.ToStationId = item.id;
                Settings.ToStationSelect = false;
            }
            setResult(2, new Intent());
            finish();
        }
    }

    @SuppressLint("NewApi")
    private void initializeAdapter() {
        getListView().setFastScrollEnabled(true);
        getListView().setFastScrollAlwaysVisible(true);
        setListAdapter(StationListAdapter);
    }

    @Override
    public void onClick(View v) {
    }
}