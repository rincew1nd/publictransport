package com.rincew1nd.publictransportmap.MarkersNodes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.rincew1nd.publictransportmap.R;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mymarkerview;
    private final TextView stationName;

    public MarkerInfoWindowAdapter(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        mymarkerview = inflater.inflate(R.layout.marker_info_window, null);
        stationName = (TextView) mymarkerview.findViewById(R.id.marker_infowindow_station);
    }

    public View getInfoWindow(Marker marker) {
        stationName.setText(marker.getTitle());
        render(marker, mymarkerview);
        return mymarkerview;
    }

    public View getInfoContents(Marker marker) {
        return null;
    }

    private void render(Marker marker, View view) {
    }
}