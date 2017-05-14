package com.rincew1nd.publictransportmap.map;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.MarkersNodes.MapMarkerManager;
import com.rincew1nd.publictransportmap.R;
import com.rincew1nd.publictransportmap.ShortPath.ShortestPathObj;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements
    OnCameraIdleListener,
    GoogleMap.OnMarkerClickListener,
    OnMapReadyCallback{

    private GoogleMap mMap;
    private float lastZoom;
    private boolean fromButtonClick;
    private boolean toButtonClick;
    private int fromNodeId;
    private int toNodeId;
    private ArrayList<ShortestPathObj> spObj;
    private int spOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Button fromNodeButton = (Button) findViewById(R.id.from_node_button);
        final Button toNodeButton = (Button) findViewById(R.id.to_node_button);
        final TextView resultTimeView = (TextView)findViewById(R.id.total_route_time);
        Button calculateButton = (Button) findViewById(R.id.calculate_button);
        Button closeResultButton = (Button) findViewById(R.id.close_total_route_time);

        fromNodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fromButtonClick = !fromButtonClick;
                toButtonClick = false;
                toNodeButton.setBackgroundColor(Color.parseColor((toButtonClick) ?
                        "#76778b" : "#b1b7ff"));
                fromNodeButton.setBackgroundColor(Color.parseColor((fromButtonClick) ?
                        "#76778b" : "#b1b7ff"));
            }
        });
        toNodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fromButtonClick = false;
                toButtonClick = !toButtonClick;
                toNodeButton.setBackgroundColor(Color.parseColor((toButtonClick) ?
                        "#76778b" : "#b1b7ff"));
                fromNodeButton.setBackgroundColor(Color.parseColor((fromButtonClick) ?
                        "#76778b" : "#b1b7ff"));
            }
        });
        calculateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (fromNodeId != 0 && toNodeId != 0)
                {
                    //spObj = ShortPathManager.GetInstance()
                        //.FindShortestPaths(fromNodeId, toNodeId, 2);
                    spOrder = 0;
                    if (spObj.size() != 0)
                    {
                        //_markerManager.HighlightRoute(spObj.get(spOrder).Path);
                        int totaltime = spObj.get(spOrder).Criteria[0];
                        resultTimeView.setText(String.format("Время поездки %d минут %d секунд",
                                totaltime / 60, totaltime % 60));
                    } else
                        resultTimeView.setText("Пути не найдено");
                    findViewById(R.id.total_route_time_layout).setVisibility(View.VISIBLE);
                }
            }
        });
        resultTimeView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (spObj.size() != 0)
                {
                    spOrder = (spObj.size() > spOrder++) ? spOrder++ : 0;
                    //_markerManager.HighlightRoute(spObj.get(spOrder).Path);
                    int totaltime = spObj.get(spOrder).Criteria[0];
                    resultTimeView.setText(String.format("Время поездки %d минут %d секунд",
                            totaltime / 60, totaltime % 60));
                } else
                    resultTimeView.setText("Пути не найдено");
            }
        });
        closeResultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                findViewById(R.id.total_route_time_layout).setVisibility(View.GONE);
                //_markerManager.RestoreHighlight();
            }
        });
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

        //mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(this));
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        // Set last zoom
        lastZoom = mMap.getCameraPosition().zoom;

        // Add idle camera listener
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);

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
    public void onCameraIdle() {
        if (lastZoom != mMap.getCameraPosition().zoom)
        {
            lastZoom = mMap.getCameraPosition().zoom;
            MapMarkerManager.GetInstance().UpdateMarkers(lastZoom);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (toButtonClick)
        {
            Button toNodeButton = (Button) findViewById(R.id.to_node_button);
            toNodeButton.setText(marker.getTitle());
            toNodeId = (int)marker.getTag();
        } else if (fromButtonClick)
        {
            Button fromNodeButton = (Button) findViewById(R.id.from_node_button);
            fromNodeButton.setText(marker.getTitle());
            fromNodeId = (int)marker.getTag();
        }
        Toast.makeText(this, marker.getTitle(), 10).show();
        return false;
    }

    // TODO Move to Utils
    public float Clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}
