package com.rincew1nd.publictransportmap.Listeners;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.rincew1nd.publictransportmap.Activities.MapsActivity;
import com.rincew1nd.publictransportmap.MapElements.MapMarkerManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
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

    private Button _fromNodeButton;
    private Button _toNodeButton;
    private TextView _resultTimeView;
    private Button _calculateButton;
    private Button _closeResultButton;

    public int Depth;
    private float _lastZoom;
    private boolean _fromButtonClick;
    private boolean _toButtonClick;
    private int _fromNodeId;
    private int _toNodeId;

    private ArrayList<ShortestPathObj> _spObj;
    private int _spOrder;

    public MapListeners(MapsActivity context) {
        _context = context;
        Depth = 2;
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
        GraphNode node = (GraphNode)marker.getTag();
        if (node == null) {
            Log.d("ERROR", "Null GraphNode marker.");
            return false;
        }
        if (_toButtonClick)
        {
            Button toNodeButton = (Button) _context.findViewById(R.id.to_node_button);
            toNodeButton.setText(node.Name);
            _toNodeId = node.Id;
        } else if (_fromButtonClick)
        {
            Button fromNodeButton = (Button) _context.findViewById(R.id.from_node_button);
            fromNodeButton.setText(node.Name);
            _fromNodeId = node.Id;
        }
        //StringBuilder sb = new StringBuilder();
        //for (GraphPath path: node.Paths)
        //    sb.append(String.format("%d-%d=%d|%d\n",
        //            path.FromNode.Id, path.ToNode.Id, path.Time, path.Delay));
        //Toast.makeText(_context, sb, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        GraphPath path = MapMarkerManager.GetInstance().GetGraphPathByPolyline(polyline);
        Toast.makeText(_context, path.IsTransfer+" "+path.Time+" "+path.Delay, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.from_node_button:
                FromToButtonClick(true, false);
                break;
            case R.id.to_node_button:
                FromToButtonClick(false, true);
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
        _fromNodeButton = (Button) _context.findViewById(R.id.from_node_button);
        _toNodeButton = (Button) _context.findViewById(R.id.to_node_button);
        _resultTimeView = (TextView)_context.findViewById(R.id.total_route_time);
        _calculateButton = (Button) _context.findViewById(R.id.calculate_button);
        _closeResultButton = (Button) _context.findViewById(R.id.close_total_route_time);

        _fromNodeButton.setOnClickListener(this);
        _toNodeButton.setOnClickListener(this);
        _calculateButton.setOnClickListener(this);
        _resultTimeView.setOnClickListener(this);
        _closeResultButton.setOnClickListener(this);
    }

    public void FromToButtonClick(boolean from, boolean to) {
        // TODO цвета в стринг константы
        _fromButtonClick = (from) && !_fromButtonClick;
        _toButtonClick = (to) && !_toButtonClick;
        _toNodeButton.setBackgroundColor(Color.parseColor((_toButtonClick) ?
                "#76778b" : "#b1b7ff"));
        _fromNodeButton.setBackgroundColor(Color.parseColor((_fromButtonClick) ?
                "#76778b" : "#b1b7ff"));
    }

    public void FindPaths() {
        if (_fromNodeId != 0 && _toNodeId != 0)
        {
            _spObj = ShortPathManager.GetInstance().FindShortestPaths(_fromNodeId, _toNodeId, Depth);
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
