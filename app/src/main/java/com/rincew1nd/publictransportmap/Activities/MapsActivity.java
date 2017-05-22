package com.rincew1nd.publictransportmap.Activities;

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
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        // Add idle camera listener
        _mapListeners.SetGoogleMap(mMap);
        mMap.setOnCameraIdleListener(_mapListeners);
        mMap.setOnMarkerClickListener(_mapListeners);
        mMap.setOnPolylineClickListener(_mapListeners);

        // Create marker manager
        GraphManager.GetInstance().SetContext(this);
        GraphManager.GetInstance().LoadGraph();
        GraphManager.GetInstance().LinkStructures();
        GraphManager.GetInstance().ProcessGraph();
        MapMarkerManager.GetInstance().SetContext(this);
        MapMarkerManager.GetInstance().SetUpMarkersAndPaths(mMap);

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
            case R.id.map_type_none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                return true;
            case R.id.default_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
                return true;
            case R.id.silver_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_silver));
                return true;
            case R.id.retro_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_retro));
                return true;
            case R.id.depth_2:
                Settings.SearchDepth = 2;
                return true;
            case R.id.depth_3:
                Settings.SearchDepth = 3;
                return true;
            case R.id.depth_4:
                Settings.SearchDepth = 4;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
