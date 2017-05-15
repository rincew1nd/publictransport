package com.rincew1nd.publictransportmap.map;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.MarkerManager;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.MarkersNodes.MapMarkerManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.R;
import com.rincew1nd.publictransportmap.ShortPath.ShortPathManager;
import com.rincew1nd.publictransportmap.ShortPath.ShortestPathObj;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements
    OnCameraIdleListener,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnPolylineClickListener,
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
                    spObj = ShortPathManager.GetInstance()
                        .FindShortestPaths(fromNodeId, toNodeId, 4);
                    spOrder = 0;
                    if (spObj.size() != 0)
                    {
                        MapMarkerManager.GetInstance().HighlightRoute(spObj.get(spOrder).Path);
                        int totalTime = spObj.get(spOrder).Criteria[0];
                        resultTimeView.setText(String.format("Время поездки %d минут %d секунд",
                                totalTime / 60, totalTime % 60));
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
                    spOrder = (spObj.size() > ++spOrder) ? spOrder++ : 0;
                    MapMarkerManager.GetInstance().HighlightRoute(spObj.get(spOrder).Path);
                    int totalTime = spObj.get(spOrder).Criteria[0];
                    resultTimeView.setText(String.format("Время поездки %d минут %d секунд",
                            totalTime / 60, totalTime % 60));
                } else
                    resultTimeView.setText("Пути не найдено");
            }
        });
        closeResultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                findViewById(R.id.total_route_time_layout).setVisibility(View.GONE);
                MapMarkerManager.GetInstance().RestoreHighlight();
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
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        lastZoom = mMap.getCameraPosition().zoom;

        // Add idle camera listener
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolylineClickListener(this);

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
        GraphNode node = (GraphNode)marker.getTag();
        if (node == null) {
            Log.d("ERROR", "Null GraphNode marker.");
            return false;
        }
        if (toButtonClick)
        {
            Button toNodeButton = (Button) findViewById(R.id.to_node_button);
            toNodeButton.setText(node.Name);
            toNodeId = node.Id;
        } else if (fromButtonClick)
        {
            Button fromNodeButton = (Button) findViewById(R.id.from_node_button);
            fromNodeButton.setText(node.Name);
            fromNodeId = node.Id;
        }

        StringBuilder sb = new StringBuilder();
        for (GraphPath path: node.Paths)
            sb.append(String.format("%d-%d=%d|%d\n",
                    path.FromNode.Id, path.ToNode.Id, path.Time, path.Delay));
        Toast.makeText(this, sb, Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        GraphPath path = MapMarkerManager.GetInstance().GetGraphPathByPolyline(polyline);
        Toast.makeText(this, path.IsTransfer+" "+path.Time+" "+path.Delay, Toast.LENGTH_SHORT).show();
    }
}
