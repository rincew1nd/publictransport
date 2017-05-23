package com.rincew1nd.publictransportmap.Activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.rincew1nd.publictransportmap.Activities.MapsActivity;
import com.rincew1nd.publictransportmap.Listeners.MapListeners;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Settings;
import com.rincew1nd.publictransportmap.R;

public class MarkerPopup implements View.OnClickListener{

    private PopupWindow _popupWindow;
    private MapsActivity _context;
    private GraphNode _graphNode;
    private TextView _stationName;

    public MarkerPopup(MapsActivity context, MapListeners listener) {
        _context = context;

        LayoutInflater inflater =
                (LayoutInflater) _context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View markerInfoWindow = inflater.inflate(R.layout.marker_info_window, null);
        _popupWindow = new PopupWindow(
                markerInfoWindow,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        _popupWindow.setOutsideTouchable(true);
        _popupWindow.setFocusable(true);
        _popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        _popupWindow.setOnDismissListener(listener);

        _stationName = (TextView) markerInfoWindow.findViewById(R.id.marker_infowindow_station);
        markerInfoWindow.findViewById(R.id.from_station_button).setOnClickListener(this);
        markerInfoWindow.findViewById(R.id.to_station_button).setOnClickListener(this);
        markerInfoWindow.findViewById(R.id.change_station_button).setOnClickListener(this);
    }

    public void Show(Marker marker) {
        _graphNode = (GraphNode) marker.getTag();
        if (_graphNode != null)
        {
            _stationName.setText(_graphNode.Name);
            _popupWindow.showAtLocation(_context.findViewById(R.id.map), Gravity.CENTER, 0, 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.from_station_button:
                Settings.FromStationId = _graphNode.Id;
                break;
            case R.id.to_station_button:
                Settings.ToStationId = _graphNode.Id;
                break;
            case R.id.change_station_button:
                break;
        }
        _popupWindow.dismiss();
    }
}