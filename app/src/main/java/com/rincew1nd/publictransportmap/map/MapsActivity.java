package com.rincew1nd.publictransportmap.map;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.rincew1nd.publictransportmap.MarkersNodes.MapMarkerManager;
import com.rincew1nd.publictransportmap.MarkersNodes.MarkerInfoWindowAdapter;
import com.rincew1nd.publictransportmap.Models.Node;
import com.rincew1nd.publictransportmap.Models.Path;
import com.rincew1nd.publictransportmap.R;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        OnCameraIdleListener,
        OnMapReadyCallback {

    private GoogleMap mMap;
    private HashMap<Node, Marker> _markersById;
    private float lastZoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        _markersById = new HashMap<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(this));
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        // Set last zoom
        lastZoom = mMap.getCameraPosition().zoom;

        // Add idle camera listener
        mMap.setOnCameraIdleListener(this);

        // Create marker manager
        MapMarkerManager markerManager = new MapMarkerManager(this);
        markerManager.LoadMarkers();

        // Place markers on map
        for (Node marker: markerManager._markers.Nodes) {
            MarkerOptions markerOptions = new MarkerOptions()
                .title(marker.Name)
                .icon(BitmapDescriptorFactory.fromBitmap(marker.icon))
                .position(new LatLng(marker.Latitude, marker.Longtitude))
                .anchor(0.08f, 0.5f);
            Marker mapMarker = mMap.addMarker(markerOptions);

            _markersById.put(marker, mapMarker);
        }

        // Draw paths on map
        for (Path path: markerManager._markers.Paths)
        {
            Node fromNode = markerManager.GetNode(path.FromNode);
            Node toNode = markerManager.GetNode(path.ToNode);
            int width = path.RouteId == -1 ? 30 : 10;
            PolylineOptions polylineOptions = new PolylineOptions()
                .add(new LatLng(fromNode.Latitude, fromNode.Longtitude))
                .add(new LatLng(toNode.Latitude, toNode.Longtitude))
                .width(width)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .color(Color.parseColor(path.Color));
            mMap.addPolyline(polylineOptions);
        }

        // Move camera to center of Moscow
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.748700, 37.617365), 10));
    }

    @Override
    public void onCameraIdle() {
        if (lastZoom != mMap.getCameraPosition().zoom)
        {
            lastZoom = mMap.getCameraPosition().zoom;

            float markerSize = lastZoom*lastZoom / 400;
            for(Map.Entry<Node, Marker> entry : _markersById.entrySet()) {
                Node marker = entry.getKey();
                Marker mapMarker = entry.getValue();

                Bitmap scaledIcon = Bitmap.createScaledBitmap(
                        marker.icon,
                        (int)(marker.icon.getWidth()*markerSize),
                        (int)(marker.icon.getHeight()*markerSize),
                        true);
                mapMarker.setIcon(BitmapDescriptorFactory.fromBitmap(scaledIcon));
            }
        }
    }

    // TODO Move to Utils
    public float Clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}