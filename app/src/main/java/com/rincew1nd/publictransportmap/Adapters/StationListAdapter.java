package com.rincew1nd.publictransportmap.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Scheduled.Stop;
import com.rincew1nd.publictransportmap.Models.StationListItem;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
import com.rincew1nd.publictransportmap.Models.WalkingPaths.Node;
import com.rincew1nd.publictransportmap.R;

import java.util.HashMap;

import de.halfbit.pinnedsection.PinnedSectionListView;

public class StationListAdapter extends ArrayAdapter<StationListItem>
        implements PinnedSectionListView.PinnedSectionListAdapter {

    private HashMap<Integer, String> _routeColors;
    private int _lastOrder;
    private int _lastRouteId;
    private Context _context;

    public StationListAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        _routeColors = new HashMap<>();
        _context = context;
        FillDataset(true);
    }

    private void FillDataset(boolean clear) {
        if (clear) clear();

        AddUnscheduledTransport();
        AddScheduledTransport();
        AddWalkingPath();
    }

    private void AddUnscheduledTransport() {
        for (com.rincew1nd.publictransportmap.Models.Unscheduled.Route route:
                GraphManager.GetInstance().TransportGraph.UnscheduledTransport.Routes)
        {
            StationListItem section = new StationListItem(StationListItem.SECTION, route.Name, route.Id);
            section.sectionPosition = route.Id;
            section.listPosition = _lastOrder;
            onSectionAdded(section, _lastOrder);
            add(section);

            _routeColors.put(route.Id, route.Color);

            if (_lastRouteId < route.Id) _lastRouteId = route.Id;
            _lastOrder++;

            for (Station station:
                    GraphManager.GetInstance().TransportGraph.UnscheduledTransport.Stations) {
                if (station.RouteId != route.Id) continue;
                StationListItem item = new StationListItem(StationListItem.ITEM, station.Name, station.Id);
                item.sectionPosition = station.RouteId;
                item.listPosition = _lastOrder;
                add(item);
                _lastOrder++;
            }
        }
    }

    private void AddScheduledTransport() {
        for (com.rincew1nd.publictransportmap.Models.Scheduled.Route route:
                GraphManager.GetInstance().TransportGraph.ScheduledTransport.Routes)
        {
            StationListItem section = new StationListItem(StationListItem.SECTION, route.Name, route.Id);
            section.sectionPosition = route.Id;
            section.listPosition = _lastOrder;
            onSectionAdded(section, _lastOrder);
            add(section);

            _routeColors.put(route.Id, "FF00FF");// route.Color);

            if (_lastRouteId < route.Id) _lastRouteId = route.Id;
            _lastOrder++;

            for (Stop station:
                    GraphManager.GetInstance().NodesFromScheduledTransportRouteId(route.Id)) {
                StationListItem item = new StationListItem(StationListItem.ITEM, station.Name, station.Id);
                item.sectionPosition = route.Id;
                item.listPosition = _lastOrder;
                add(item);
                _lastOrder++;
            }
        }
    }

    private void AddWalkingPath() {
        _lastRouteId++;
        StationListItem section = new StationListItem(StationListItem.SECTION, "Пешие маршруты", _lastRouteId);
        section.sectionPosition = _lastRouteId;
        section.listPosition = _lastOrder;
        onSectionAdded(section, _lastOrder);
        add(section);

        _routeColors.put(_lastRouteId, "FF0000");
        _lastOrder++;

        for (Node node: GraphManager.GetInstance().TransportGraph.WalkingPaths.Nodes) {
            StationListItem item = new StationListItem(StationListItem.ITEM, node.Name, node.Id);
            item.sectionPosition = _lastRouteId;
            item.listPosition = _lastOrder;
            add(item);
            _lastOrder++;
        }
    }

    private void onSectionAdded(StationListItem section, int sectionPosition) { }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        FrameLayout view = (FrameLayout) super.getView(position, convertView, parent);

        TextView text = (TextView) view.findViewById(R.id.station_list_text);
        ImageView image = (ImageView) view.findViewById(R.id.station_list_image);

        StationListItem item = getItem(position);
        if (item.type == StationListItem.SECTION)
        {
            image.setVisibility(View.GONE);
            text.setLayoutParams(
                new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            text.setGravity(Gravity.CENTER);
            text.setTextAppearance(_context, R.style.StationOutline);
            view.setBackgroundColor(Color.parseColor("#FF"+ _routeColors.get(item.sectionPosition)));
        } else {
            ((GradientDrawable)image.getBackground())
                    .setColor(Color.parseColor("#FF"+ _routeColors.get(item.sectionPosition)));
        }
        return view;
    }

    @Override public int getViewTypeCount() {
        return 2;
    }

    @Override public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == StationListItem.SECTION;
    }
}
