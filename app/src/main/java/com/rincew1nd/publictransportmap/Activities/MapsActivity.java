package com.rincew1nd.publictransportmap.Activities;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.rincew1nd.publictransportmap.Adapters.StationListAdapter;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Listeners.MapListeners;
import com.rincew1nd.publictransportmap.MapElements.MapMarkerManager;
import com.rincew1nd.publictransportmap.Models.Settings;
import com.rincew1nd.publictransportmap.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private MapListeners _mapListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.LoadSettings(this);

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        _mapListeners = new MapListeners(this);
        _mapListeners.LayoutButtonsEvents();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        SettingsActivity.Map = googleMap;

        if (Settings.MapStyleResourceId == -1) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, Settings.MapStyleResourceId));
        }

        // Add idle camera listener
        _mapListeners.SetGoogleMap(mMap);
        mMap.setOnCameraIdleListener(_mapListeners);
        mMap.setOnMarkerClickListener(_mapListeners);
        mMap.setOnPolylineClickListener(_mapListeners);

        // Create marker manager
        GraphManager.GetInstance().SetContext(this);
        GraphManager.GetInstance().LoadGraph();
        MapMarkerManager.GetInstance().SetContextAndMap(this, mMap);
        MapMarkerManager.GetInstance().SetUpMarkersAndPaths();
        StationListActivity.StationListAdapter =
                new StationListAdapter(this, R.layout.station_list_item, R.id.station_list_text);

        // Move camera to center of Moscow
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.748700, 37.617365), 10));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                this.startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.add_marker:
                this.startActivity(
                    new Intent(this, MarkerEditActivity.class).putExtra("IsCreate", true)
                );
                return true;
            case R.id.add_path:
                this.startActivity(
                    new Intent(this, PathEditActivity.class).putExtra("IsCreate", true)
                );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        _mapListeners.OnActivityResult(requestCode, resultCode, data);
    }
}
