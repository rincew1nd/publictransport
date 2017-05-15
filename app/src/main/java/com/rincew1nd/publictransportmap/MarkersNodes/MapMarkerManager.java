package com.rincew1nd.publictransportmap.MarkersNodes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Scheduled.Stop;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
import com.rincew1nd.publictransportmap.Models.WalkingPaths.Node;
import com.rincew1nd.publictransportmap.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MapMarkerManager {

    private static MapMarkerManager instance;
    private Context _context;

    private GraphManager _graphManager;
    private HashMap<Integer, Marker> _markers;
    private HashMap<Marker, Bitmap> _markersImage;
    private HashMap<Polyline, GraphPath> _polyPaths;

    private MapMarkerManager() {
        _graphManager = GraphManager.GetInstance();
        _markers = new HashMap<>();
        _markersImage = new HashMap<>();
        _polyPaths = new HashMap<>();
    }
    public static MapMarkerManager GetInstance() {
        if (instance == null)
            instance = new MapMarkerManager();
        return instance;
    }

    public void SetContext(Context context) {
        _context = context;
    }
    public void SetUpMarkersAndPaths(GoogleMap mMap) {
        for (GraphNode node: _graphManager.Nodes.values()) {
            Bitmap mapMarkerIcon = GenerateBitmapIcon(node.Name, node.NodeColor);
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(mapMarkerIcon))
                    .position(new LatLng(node.Lat, node.Lon))
                    .anchor(0.08f, 0.5f);
            Marker mapMarker = mMap.addMarker(markerOptions);
            mapMarker.setTag(node);
            _markers.put(node.Id, mapMarker);
            _markersImage.put(mapMarker, mapMarkerIcon);
        }

        for (GraphPath path: _graphManager.Paths) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .clickable(true)
                    .add(new LatLng(path.FromNode.Lat, path.FromNode.Lon))
                    .add(new LatLng(path.ToNode.Lat, path.ToNode.Lon))
                    .width(path.Width)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .color(path.PathColor);
            Polyline polyPath = mMap.addPolyline(polylineOptions);
            _polyPaths.put(polyPath, path);
        }
    }
    public void UpdateMarkers(float zoom) {
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

    //TODO вынести в Utils
    private Bitmap GenerateBitmapIcon(String text, int color) {
        View customMarkerView =
                ((LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.custom_marker_view, null);
        ImageView markerImage = (ImageView) customMarkerView.findViewById(R.id.marker_image);
        TextView markerText = (TextView) customMarkerView.findViewById(R.id.marker_text);

        //markerImage.setBackgroundResource(R.drawable.circle);
        ((GradientDrawable)markerImage.getBackground()).setColor(color);
        markerText.setText(text);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();

        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    public void HighlightRoute(ArrayList<Integer> route) {
        if (route == null)
            return;

        for (Marker station : _markersImage.keySet())
            station.setAlpha(0.2f);
        for (Polyline path: _polyPaths.keySet())
            path.setColor(0x33000000 | path.getColor() & 0x00FFFFFF);

        for(int i = 0; i < route.size(); i++) {
            _markers.get(route.get(i)).setAlpha(1f);
            if (i != route.size()-1)
                for (Map.Entry<Polyline, GraphPath> path: _polyPaths.entrySet())
                    if (path.getValue().FromNode.Id == route.get(i) &&
                        path.getValue().ToNode.Id == route.get(i+1))
                        path.getKey().setColor(0xFF000000 | path.getKey().getColor() & 0x00FFFFFF);
        }
    }

    public void RestoreHighlight()
    {
        for (Marker station : _markersImage.keySet())
            station.setAlpha(1f);
        for (Polyline path: _polyPaths.keySet())
            path.setColor(0xFF000000 | path.getColor() & 0x00FFFFFF);
    }

    public GraphPath GetGraphPathByPolyline(Polyline line) {
        return _polyPaths.get(line);
    }
}