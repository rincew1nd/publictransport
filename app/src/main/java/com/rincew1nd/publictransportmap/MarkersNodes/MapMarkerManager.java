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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Scheduled.Stop;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
import com.rincew1nd.publictransportmap.Models.WalkingPaths.Node;
import com.rincew1nd.publictransportmap.R;

import java.util.HashMap;
import java.util.Map;

public class MapMarkerManager {

    private static MapMarkerManager instance;
    private Context _context;

    private GraphManager _graphManager;
    private HashMap<Marker, Bitmap> _markersImage;

    private MapMarkerManager() {
        _graphManager = GraphManager.GetInstance();
        _markersImage = new HashMap<>();
    }
    public static MapMarkerManager GetInstance() {
        if (instance == null)
            instance = new MapMarkerManager();
        return instance;
    }
    public void SetContext(Context context) {
        _context = context;
    }

    // SetUp markers and path on google map
    public void SetUpMarkersAndPaths(GoogleMap mMap) {
        // Place markers on map
        for (GraphNode node: _graphManager.Nodes.values()) {
            Bitmap mapMarkerIcon = GenerateBitmapIcon(node.Name, node.NodeColor);
            MarkerOptions markerOptions = new MarkerOptions()
                    .title(node.Name)
                    .icon(BitmapDescriptorFactory.fromBitmap(mapMarkerIcon))
                    .position(new LatLng(node.Lat, node.Lon))
                    .anchor(0.08f, 0.5f);
            Marker mapMarker = mMap.addMarker(markerOptions);
            mapMarker.setTag(node);
            _markersImage.put(mapMarker, mapMarkerIcon);
        }

//        // Place markers on Map
//        for (Station marker: _graphManager.TransportGraph.UnscheduledTransport.Stations) {
//            Bitmap mapMarkerIcon = GenerateBitmapIcon(marker.Name, "#0000FF");
//            MarkerOptions markerOptions = new MarkerOptions()
//                    .title(marker.Name)
//                    .icon(BitmapDescriptorFactory.fromBitmap(mapMarkerIcon))
//                    .position(new LatLng(marker.Lat, marker.Lon))
//                    .anchor(0.08f, 0.5f);
//            Marker mapMarker = mMap.addMarker(markerOptions);
//            mapMarker.setTag(marker);
//            _markersImage.put(mapMarker, mapMarkerIcon);
//        }
//
//        for (Stop stop: _graphManager.TransportGraph.ScheduledTransport.Stops) {
//            Bitmap mapMarkerIcon = GenerateBitmapIcon(stop.Name, "#00FF00");
//            MarkerOptions markerOptions = new MarkerOptions()
//                    .title(stop.Name)
//                    .icon(BitmapDescriptorFactory.fromBitmap(mapMarkerIcon))
//                    .position(new LatLng(stop.Lat, stop.Lon))
//                    .anchor(0.08f, 0.5f);
//            Marker mapMarker = mMap.addMarker(markerOptions);
//            mapMarker.setTag(stop);
//            _markersImage.put(mapMarker, mapMarkerIcon);
//        }
//
//        for (Node node: _graphManager.TransportGraph.WalkingPaths.Nodes) {
//            Bitmap mapMarkerIcon = GenerateBitmapIcon(node.Name, "#FF0000");
//            MarkerOptions markerOptions = new MarkerOptions()
//                    .title(node.Name)
//                    .icon(BitmapDescriptorFactory.fromBitmap(mapMarkerIcon))
//                    .position(new LatLng(node.Lat, node.Lon))
//                    .anchor(0.08f, 0.5f);
//            Marker mapMarker = mMap.addMarker(markerOptions);
//            mapMarker.setTag(node);
//            _markersImage.put(mapMarker, mapMarkerIcon);
//        }

        // Draw paths on Map
        for (GraphPath path: _graphManager.Paths) {
            int width = path.IsTransfer ? 30 : 10;
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(new LatLng(path.FromNode.Lat, path.FromNode.Lon))
                    .add(new LatLng(path.ToNode.Lat, path.ToNode.Lon))
                    .width(width)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .color(path.PathColor);
            mMap.addPolyline(polylineOptions);
        }

//        for (com.rincew1nd.publictransportmap.Models.Unscheduled.Path path:
//                _graphManager.TransportGraph.UnscheduledTransport.Paths) {
//            int width = path.RouteId == -1 ? 30 : 10;
//            PolylineOptions polylineOptions = new PolylineOptions()
//                    .add(new LatLng(path.FromNode.Lat, path.FromNode.Lon))
//                    .add(new LatLng(path.ToNode.Lat, path.ToNode.Lon))
//                    .width(width)
//                    .startCap(new RoundCap())
//                    .endCap(new RoundCap())
//                    .color(Color.parseColor(path.Color));
//            mMap.addPolyline(polylineOptions);
//        }
//
//        for (com.rincew1nd.publictransportmap.Models.WalkingPaths.Path path:
//                _graphManager.TransportGraph.WalkingPaths.Paths) {
//            PolylineOptions polylineOptions = new PolylineOptions()
//                    .add(new LatLng(path.FromNode.Lat, path.FromNode.Lon))
//                    .add(new LatLng(path.ToNode.Lat, path.ToNode.Lon))
//                    .width(10)
//                    .startCap(new RoundCap())
//                    .endCap(new RoundCap())
//                    .color(Color.parseColor("#FF0000"));
//            mMap.addPolyline(polylineOptions);
//        }
    }

    // Rescale markers icons proportional to map zoom
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

    // Generate icons
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
        //Drawable drawable = customMarkerView.getBackground();
        //if (drawable != null)
        //    drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

//    public void HighlightRoute(ArrayList<Integer> route) {
//        if (route == null) return;
//
//        for (Station station : _transportMap.stations)
//            station.Marker.setAlpha(0.2f);
//        for (Path path: _transportMap.Paths)
//            path.Line.setColor(0x33000000 | path.Line.getColor() & 0x00FFFFFF);
//
//        for(int i = 0; i < route.size(); i++)
//        {
//            _mapNodes.get(route.get(i)).Marker.setAlpha(1f);
//            if (i != route.size()-1)
//            {
//                if (_mapPaths.get(route.get(i)) != null) {
//                    for (Path path: _mapPaths.get(route.get(i)))
//                        if (path.ToNode == route.get(i+1))
//                            path.Line.setColor(0xFF000000 | path.Line.getColor() & 0x00FFFFFF);
//                } else
//                    Log.d("FAIL", String.format("Dafaq? %d %d", i, route.get(i)));
//                if (_mapPaths.get(route.get(i+1)) != null) {
//                    for (Path path: _mapPaths.get(route.get(i+1)))
//                        if (path.ToNode == route.get(i))
//                            path.Line.setColor(0xFF000000 | path.Line.getColor() & 0x00FFFFFF);
//                } else
//                    Log.d("FAIL", String.format("Dafaq? %d %d", i+1, route.get(i+1)));
//            }
//        }
//    }
//
//    public void RestoreHighlight()
//    {
//        for (Station station : _transportMap.stations)
//            station.Marker.setAlpha(1f);
//        for (Path path: _transportMap.Paths)
//            path.Line.setColor(0xFF000000 | path.Line.getColor() & 0x00FFFFFF);
//    }
//
//    public Station GetNodeByMarker(Marker marker) {
//        for(Station station : _transportMap.stations)
//            if (station.Marker == marker)
//                return station;
//        return null;
//    }
}