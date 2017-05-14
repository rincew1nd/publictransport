package com.rincew1nd.publictransportmap.GraphManager;

import android.content.Context;

import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Scheduled.Calendar;
import com.rincew1nd.publictransportmap.Models.Scheduled.Route;
import com.rincew1nd.publictransportmap.Models.Scheduled.Stop;
import com.rincew1nd.publictransportmap.Models.Scheduled.StopTime;
import com.rincew1nd.publictransportmap.Models.Scheduled.Trip;
import com.rincew1nd.publictransportmap.Models.Transport;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Path;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
import com.rincew1nd.publictransportmap.Models.WalkingPaths.Node;
import com.rincew1nd.publictransportmap.Utils.JsonSerializer;
import com.rincew1nd.publictransportmap.R;
import java.util.HashSet;

public class GraphManager {
    private static GraphManager _instance;

    private GraphManager() {
        Nodes = new HashSet<>();
        Paths = new HashSet<>();
    }
    public static GraphManager GetInstance() {
        if (_instance == null)
            _instance = new GraphManager();
        return _instance;
    }
    public GraphManager SetCotext(Context context) {
        _context = context;
        return this;
    }

    private Context _context;
    public Transport _transportGraph;

    public HashSet<GraphNode> Nodes;
    public HashSet<GraphPath> Paths;

    // Load markers from JSON file and generate icons
    public void LoadGraph() {
        JsonSerializer reader = new JsonSerializer(_context.getResources(), R.raw.transport);
        _transportGraph = reader.constructUsingGson(Transport.class);
    }

    public void LinkStructures()
    {
        // WalkingPaths
//        for (com.rincew1nd.publictransportmap.Models.WalkingPaths.Route route:
//                _transportGraph.WalkingPaths.Routes)
//        {
//            for (Node node: _transportGraph.WalkingPaths.Nodes)
//                if (node.RouteId == route.Id)
//                    route.Nodes.put(node.Id, node);
//        }
//        for (com.rincew1nd.publictransportmap.Models.WalkingPaths.Path path:
//                _transportGraph.WalkingPaths.Paths)
//        {
//            for (Node node: _transportGraph.WalkingPaths.Nodes)
//                if (node.Id == path.FromNodeId)
//                {
//                    path.FromNode = node;
//                    break;
//                }
//            for (Node node: _transportGraph.WalkingPaths.Nodes)
//                if (node.Id == path.ToNodeId)
//                {
//                    path.ToNode = node;
//                    break;
//                }
//        }

        // ScheduledTransport
        for (com.rincew1nd.publictransportmap.Models.Scheduled.Route route:
                _transportGraph.ScheduledTransport.Routes)
        {
            for (Trip trip: _transportGraph.ScheduledTransport.Trips)
                if (trip.RouteId == route.Id)
                {
                    route.Trips.put(trip.Id, trip);
                    trip.Route = route;
                }
        }
        for (StopTime stopTime:
                _transportGraph.ScheduledTransport.StopTimes)
        {
            for (Stop stop: _transportGraph.ScheduledTransport.Stops)
                if (stopTime.StopId == stop.Id)
                {
                    stopTime.Stop = stop;
                    break;
                }
        }
        for (Trip trip: _transportGraph.ScheduledTransport.Trips)
        {
            for (Calendar calendar: _transportGraph.ScheduledTransport.Calendars)
                if (calendar.Id == trip.CalendarId)
                {
                    trip.Calendar = calendar;
                    break;
                }
            for (StopTime stopTime: _transportGraph.ScheduledTransport.StopTimes)
                if (stopTime.TripId == trip.Id)
                    trip.StopTimes.add(stopTime);
        }

        // UnscheduledTransport
        // Path-FromNode,ToNode, Route-Stations
        for (com.rincew1nd.publictransportmap.Models.Unscheduled.Path path:
                _transportGraph.UnscheduledTransport.Paths)
        {
            for (Station station: _transportGraph.UnscheduledTransport.Stations)
                if (path.FromNodeId == station.Id)
                {
                    path.FromNode = station;
                    break;
                }
            for (Station station: _transportGraph.UnscheduledTransport.Stations)
                if (path.ToNodeId == station.Id)
                {
                    path.ToNode = station;
                    break;
                }
        }
        for (com.rincew1nd.publictransportmap.Models.Unscheduled.Route route:
                _transportGraph.UnscheduledTransport.Routes)
        {
            for (Station station: _transportGraph.UnscheduledTransport.Stations)
                if (station.RouteId == route.Id)
                    route.Stations.put(station.Id, station);
        }
    }

    public void ProcessGraph() {
        for (Station station : _transportGraph.UnscheduledTransport.Stations)
            Nodes.add(new GraphNode(station));
        for (Node node: _transportGraph.WalkingPaths.Nodes)
            Nodes.add(new GraphNode(node));
        //for (Stop stop: _transportGraph.ScheduledTransport.Stops)
        //    Nodes.add(stop, )
    }
}
