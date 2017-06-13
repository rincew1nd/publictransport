package com.rincew1nd.publictransportmap.MapElements;

import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.rincew1nd.publictransportmap.Activities.MapsActivity;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Settings;
import com.rincew1nd.publictransportmap.Utils.BitmapGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapMarkerManager {

    private static MapMarkerManager instance;
    private MapsActivity _context;
    private GoogleMap _map;

    private GraphManager _graphManager;
    private HashMap<Integer, Marker> _markers;
    private HashMap<Marker, Bitmap> _markersImage;
    private HashMap<GraphPath, Polyline> _polyPaths;
    private float lastZoom;

    private MapMarkerManager() {
        _graphManager = GraphManager.GetInstance();
        _markers = new HashMap<>();
        _markersImage = new HashMap<>();
        _polyPaths = new HashMap<>();
        lastZoom = 10;
    }

    public static MapMarkerManager GetInstance() {
        if (instance == null)
            instance = new MapMarkerManager();
        return instance;
    }

    public void SetContextAndMap(MapsActivity context, GoogleMap mMap) {
        _context = context;
        _map = mMap;
    }

    public void SetUpMarkersAndPaths() {
        //GraphOptimization go = new GraphOptimization();
        //go.OptimizeGraph(400);

        _map.clear();
        _markers.clear();
        _markersImage.clear();
        _polyPaths.clear();

        for (GraphNode node: _graphManager.Nodes.values()) { //go.OptimizedNodes.values()) {
            Bitmap mapMarkerIcon =
                    BitmapGenerator.GenerateBitmapIcon(_context, node.Name, node.NodeColor);
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(mapMarkerIcon))
                    .position(new LatLng(node.Lat, node.Lon))
                    .anchor(0.08f, 0.5f);
            Marker mapMarker = _map.addMarker(markerOptions);
            mapMarker.setTag(node);
            _markers.put(node.Id, mapMarker);
            _markersImage.put(mapMarker, mapMarkerIcon);
        }

        for (GraphPath path: _graphManager.Paths) { //go.OptimizedPaths) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .clickable(true)
                    .add(new LatLng(path.FromNode.Lat, path.FromNode.Lon))
                    .add(new LatLng(path.ToNode.Lat, path.ToNode.Lon))
                    .width(path.Width)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .color(path.PathColor);
            Polyline polyPath = _map.addPolyline(polylineOptions);
            _polyPaths.put(path, polyPath);
        }
    }

    public void UpdateMarkerImage(GraphNode node) {
        Marker marker = _markers.get(node.Id);

        LatLng newPosition = new LatLng(node.Lat, node.Lon);
        marker.setPosition(newPosition);
        for(GraphPath path: _graphManager.Paths) {
            if (path.FromNode.Id == node.Id) {
                List<LatLng> line = _polyPaths.get(path).getPoints();
                line.set(0, newPosition);
                _polyPaths.get(path).setPoints(line);
            }
            if (path.ToNode.Id == node.Id) {
                List<LatLng> line = _polyPaths.get(path).getPoints();
                line.set(1, newPosition);
                _polyPaths.get(path).setPoints(line);
            }
        }

        Bitmap mapMarkerIcon =
                BitmapGenerator.GenerateBitmapIcon(_context, node.Name, node.NodeColor);
        _markersImage.put(marker, mapMarkerIcon);
        UpdateMarkers(lastZoom);
    }

    public void UpdateMarkers(float zoom) {
        lastZoom = zoom;
        float markerSize = zoom*zoom / 400;
        for(Map.Entry<Marker, Bitmap> marker: _markersImage.entrySet()) {
            Bitmap scaledIcon = Bitmap.createScaledBitmap(
                    marker.getValue(),
                    (int)(marker.getValue().getWidth()*markerSize),
                    (int)(marker.getValue().getHeight()*markerSize),
                    true);
            marker.getKey().setIcon(BitmapDescriptorFactory.fromBitmap(scaledIcon));
        }
    }

    public void HighlightRoute(ArrayList<Integer> route) {
        if (route == null)
            return;

        for (Marker station : _markersImage.keySet())
            station.setVisible(false);
        for (Polyline path: _polyPaths.values())
            path.setVisible(false);

        for(int i = 0; i < route.size(); i++) {
            _markers.get(route.get(i)).setVisible(true);
            if (i != route.size()-1)
                for (Map.Entry<GraphPath, Polyline> path: _polyPaths.entrySet())
                    if (path.getKey().FromNode.Id == route.get(i) &&
                            path.getKey().ToNode.Id == route.get(i+1))
                        path.getValue().setVisible(true);
        }
    }

    public void RestoreHighlight() {
        for (Marker station : _markersImage.keySet())
            station.setVisible(true);
        for (Polyline path: _polyPaths.values())
            path.setVisible(true);
    }
}