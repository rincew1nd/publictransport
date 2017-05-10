package com.rincew1nd.publictransportmap.MarkersNodes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.rincew1nd.publictransportmap.Models.Path;
import com.rincew1nd.publictransportmap.Utils.JsonSerializer;
import com.rincew1nd.publictransportmap.Models.Node;
import com.rincew1nd.publictransportmap.Models.PublicTransportMap;
import com.rincew1nd.publictransportmap.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MapMarkerManager {

    private static MapMarkerManager instance;
    private Context _context;

    public PublicTransportMap _transportMap;
    private HashMap<Integer, Node> _mapNodes;
    private HashMap<Integer, HashSet<Path>> _mapPaths;

    private MapMarkerManager() {
        _mapNodes = new HashMap<>();
        _mapPaths = new HashMap<>();
    }
    public static MapMarkerManager GetInstance() {
        if (instance == null)
            instance = new MapMarkerManager();
        return instance;
    }
    public MapMarkerManager SetCotext(Context context) {
        _context = context;
        return this;
    }

    // Load markers from JSON file and generate icons
    public void LoadMarkers() {
        JsonSerializer reader = new JsonSerializer(_context.getResources(), R.raw.metro);
        _transportMap = reader.constructUsingGson(PublicTransportMap.class);
        for (Node marker: _transportMap.Nodes)
            marker.Icon = GenerateBitmapIcon(marker);
    }

    // SetUp markers and path on google map
    public void SetUpMarkersAndPaths(GoogleMap mMap) {
        // Place markers on Map
        for (Node marker: _transportMap.Nodes) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .title(marker.Name)
                    .icon(BitmapDescriptorFactory.fromBitmap(marker.Icon))
                    .position(new LatLng(marker.Latitude, marker.Longtitude))
                    .anchor(0.08f, 0.5f);
            Marker mapMarker = mMap.addMarker(markerOptions);
            mapMarker.setTag(marker.Id);
            marker.Marker = mapMarker;
            _mapNodes.put(marker.Id, marker);
        }

        // Draw paths on Map
        for (Path path: _transportMap.Paths)
        {
            Node fromNode = _mapNodes.get(path.FromNode);
            Node toNode = _mapNodes.get(path.ToNode);
            int width = path.RouteId == -1 ? 30 : 10;
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(new LatLng(fromNode.Latitude, fromNode.Longtitude))
                    .add(new LatLng(toNode.Latitude, toNode.Longtitude))
                    .width(width)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .color(Color.parseColor(path.Color));
            Polyline mapPath = mMap.addPolyline(polylineOptions);
            path.Line = mapPath;

            HashSet<Path> paths = (_mapPaths.containsKey(path.FromNode)) ?
                _mapPaths.get(path.FromNode) :
                new HashSet<Path>();
            paths.add(path);
            _mapPaths.put(path.FromNode, paths);
        }
    }

    // Rescale markers icons proportional to map zoom
    public void UpdateMarkers(float zoom) {
        float markerSize = zoom*zoom / 400;
        for(Node marker : _transportMap.Nodes) {
            Bitmap scaledIcon = Bitmap.createScaledBitmap(
                    marker.Icon,
                    (int)(marker.Icon.getWidth()*markerSize),
                    (int)(marker.Icon.getHeight()*markerSize),
                    true);
            marker.Marker.setIcon(BitmapDescriptorFactory.fromBitmap(scaledIcon));
        }
    }

    // Generate icons
    private Bitmap GenerateBitmapIcon(Node marker) {
        View customMarkerView =
                ((LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.custom_marker_view, null);
        ImageView markerImage = (ImageView) customMarkerView.findViewById(R.id.marker_image);
        TextView markerText = (TextView) customMarkerView.findViewById(R.id.marker_text);

        markerImage.setBackgroundResource(R.drawable.circle);
        markerText.setText(marker.Name);

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

    public void HighlightRoute(ArrayList<Integer> route)
    {
        if (route == null) return;

        for (Node node: _transportMap.Nodes)
            node.Marker.setAlpha(0.2f);
        for (Path path: _transportMap.Paths)
            path.Line.setColor(0x33000000 | path.Line.getColor() & 0x00FFFFFF);

        for(int i = 0; i < route.size(); i++)
        {
            _mapNodes.get(route.get(i)).Marker.setAlpha(1f);
            if (i != route.size()-1)
            {
                for (Path path: _mapPaths.get(route.get(i)))
                    if (path.ToNode == route.get(i+1))
                        path.Line.setColor(0xFF000000 | path.Line.getColor() & 0x00FFFFFF);
                for (Path path: _mapPaths.get(route.get(i+1)))
                    if (path.ToNode == route.get(i))
                        path.Line.setColor(0xFF000000 | path.Line.getColor() & 0x00FFFFFF);
            }
        }
    }

    public Node GetNodeByMarker(Marker marker) {
        for(Node node: _transportMap.Nodes)
            if (node.Marker == marker)
                return node;
        return null;
    }
}
