package com.rincew1nd.publictransportmap.Listeners;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.rincew1nd.publictransportmap.Activities.MapsActivity;
import com.rincew1nd.publictransportmap.MapElements.MapMarkerManager;
import com.rincew1nd.publictransportmap.Activities.MarkerPopup;
import com.rincew1nd.publictransportmap.Models.Settings;
import com.rincew1nd.publictransportmap.R;
import com.rincew1nd.publictransportmap.ShortPath.ShortPathManager;
import com.rincew1nd.publictransportmap.ShortPath.ShortestPathObj;

import java.util.ArrayList;

public class MapListeners implements
        View.OnClickListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnPolylineClickListener {

    private MapsActivity _context;
    private GoogleMap _map;
    private MarkerPopup _markerPopup;

    private TextView _resultTimeView;
    private float _lastZoom;

    private ArrayList<ShortestPathObj> _spObj;
    private int _spOrder;

    public MapListeners(MapsActivity context) {
        _context = context;
        _markerPopup = new MarkerPopup(context);
    }

    public void SetGoogleMap(GoogleMap map) {
        _map = map;
        _lastZoom = map.getCameraPosition().zoom;
    }

    @Override
    public void onCameraIdle() {
        if (_lastZoom != _map.getCameraPosition().zoom)
        {
            _lastZoom = _map.getCameraPosition().zoom;
            MapMarkerManager.GetInstance().UpdateMarkers(_lastZoom);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        _markerPopup.Show(marker);
        return false;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.from_station_button:
                //TODO Вызов списка станций
                break;
            case R.id.to_station_button:
                //TODO Вызов списка станций
                break;
            case R.id.calculate_button:
                FindPaths();
                break;
            case R.id.total_route_time:
                NextPath();
                break;
            case R.id.close_total_route_time:
                CloseResultView();
                break;
            default:
                Log.d("UNKNOWN_BUTTON", ""+v.getId());
        }
    }

    public void LayoutButtonsEvents() {
        _resultTimeView = (TextView)_context.findViewById(R.id.total_route_time);
        _resultTimeView.setOnClickListener(this);

        _context.findViewById(R.id.from_station_button).setOnClickListener(this);
        _context.findViewById(R.id.to_station_button).setOnClickListener(this);
        _context.findViewById(R.id.calculate_button).setOnClickListener(this);
        _context.findViewById(R.id.close_total_route_time).setOnClickListener(this);
    }

    public void FindPaths() {
        if (Settings.FromStationId >= 0 && Settings.ToStationId >= 0)
        {
            _spObj = ShortPathManager.GetInstance().FindShortestPaths();
            _spOrder = 0;
            SetResultText();
            _context.findViewById(R.id.total_route_time_layout).setVisibility(View.VISIBLE);
        }
    }

    public void NextPath() {
        _spOrder = (_spObj.size() > ++_spOrder) ? _spOrder++ : 0;
        SetResultText();
    }

    public void SetResultText() {
        if (_spObj.size() != 0)
        {
            MapMarkerManager.GetInstance().HighlightRoute(_spObj.get(_spOrder).Path);
            int hours = _spObj.get(_spOrder).Criteria[0] / 3600;
            int minutes = (_spObj.get(_spOrder).Criteria[0] % 3600) / 60;
            int seconds = _spObj.get(_spOrder).Criteria[0] % 60;
            _resultTimeView.setText(
                String.format(
                    _context.getString(R.string.path_result_string),
                    hours, minutes, seconds
            ));
        } else
            _resultTimeView.setText("Пути не найдено");
    }

    public void CloseResultView() {
        _context.findViewById(R.id.total_route_time_layout).setVisibility(View.GONE);
        MapMarkerManager.GetInstance().RestoreHighlight();
    }
}
