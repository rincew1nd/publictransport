//package com.rincew1nd.publictransportmap.MarkersNodes;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.PorterDuff;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.Polyline;
//import com.google.android.gms.maps.model.PolylineOptions;
//import com.google.android.gms.maps.model.RoundCap;
//import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
//import com.rincew1nd.publictransportmap.Models.Unscheduled.UnscheduledTransport;
//import com.rincew1nd.publictransportmap.Models.Unscheduled.Path;
//import com.rincew1nd.publictransportmap.Utils.JsonSerializer;
//import com.rincew1nd.publictransportmap.R;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//
//public class MapMarkerManager {
//
//    private static MapMarkerManager instance;
//    private Context _context;
//
//    public UnscheduledTransport _transportMap;
//    private HashMap<Integer, Station> _mapNodes;
//    private HashMap<Integer, HashSet<Path>> _mapPaths;
//
//    private MapMarkerManager() {
//        _mapNodes = new HashMap<>();
//        _mapPaths = new HashMap<>();
//    }
//    public static MapMarkerManager GetInstance() {
//        if (instance == null)
//            instance = new MapMarkerManager();
//        return instance;
//    }
//    public MapMarkerManager SetCotext(Context context) {
//        _context = context;
//        return this;
//    }
//
//    // SetUp markers and path on google map
//    public void SetUpMarkersAndPaths(GoogleMap mMap) {
//        // Place markers on Map
//        for (Station marker: _transportMap.stations) {
//            MarkerOptions markerOptions = new MarkerOptions()
//                    .title(marker.Name)
//                    .icon(BitmapDescriptorFactory.fromBitmap(marker.Icon))
//                    .position(new LatLng(marker.Latitude, marker.Longtitude))
//                    .anchor(0.08f, 0.5f);
//            Marker mapMarker = mMap.addMarker(markerOptions);
//            mapMarker.setTag(marker.Id);
//            marker.Marker = mapMarker;
//            _mapNodes.put(marker.Id, marker);
//        }
//
//        // Draw paths on Map
//        for (Path path: _transportMap.Paths)
//        {
//            Station fromStation = _mapNodes.get(path.FromNode);
//            Station toStation = _mapNodes.get(path.ToNode);
//            int width = path.RouteId == -1 ? 30 : 10;
//            PolylineOptions polylineOptions = new PolylineOptions()
//                    .add(new LatLng(fromStation.Latitude, fromStation.Longtitude))
//                    .add(new LatLng(toStation.Latitude, toStation.Longtitude))
//                    .width(width)
//                    .startCap(new RoundCap())
//                    .endCap(new RoundCap())
//                    .color(Color.parseColor(path.Color));
//            Polyline mapPath = mMap.addPolyline(polylineOptions);
//            path.Line = mapPath;
//
//            HashSet<Path> paths = (_mapPaths.containsKey(path.FromNode)) ?
//                _mapPaths.get(path.FromNode) :
//                new HashSet<Path>();
//            paths.add(path);
//            _mapPaths.put(path.FromNode, paths);
//        }
//    }
//
//    // Rescale markers icons proportional to map zoom
//    public void UpdateMarkers(float zoom) {
//        float markerSize = zoom*zoom / 400;
//        for(Station marker : _transportMap.stations) {
//            Bitmap scaledIcon = Bitmap.createScaledBitmap(
//                    marker.Icon,
//                    (int)(marker.Icon.getWidth()*markerSize),
//                    (int)(marker.Icon.getHeight()*markerSize),
//                    true);
//            marker.Marker.setIcon(BitmapDescriptorFactory.fromBitmap(scaledIcon));
//        }
//    }
//
//    // Generate icons
//    private Bitmap GenerateBitmapIcon(Station marker) {
//        View customMarkerView =
//                ((LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
//                        .inflate(R.layout.custom_marker_view, null);
//        ImageView markerImage = (ImageView) customMarkerView.findViewById(R.id.marker_image);
//        TextView markerText = (TextView) customMarkerView.findViewById(R.id.marker_text);
//
//        markerImage.setBackgroundResource(R.drawable.circle);
//        markerText.setText(marker.Name);
//
//        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
//        customMarkerView.buildDrawingCache();
//
//        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
//                Bitmap.Config.ARGB_8888);
//
//        Canvas canvas = new Canvas(returnedBitmap);
//        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
//        //Drawable drawable = customMarkerView.getBackground();
//        //if (drawable != null)
//        //    drawable.draw(canvas);
//        customMarkerView.draw(canvas);
//        return returnedBitmap;
//    }
//
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
//}