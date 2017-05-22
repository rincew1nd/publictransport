package com.rincew1nd.publictransportmap.GraphManager;

import android.content.Context;
import android.util.Log;

import com.rincew1nd.publictransportmap.Activities.MapsActivity;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Scheduled.Calendar;
import com.rincew1nd.publictransportmap.Models.Scheduled.Stop;
import com.rincew1nd.publictransportmap.Models.Scheduled.StopTime;
import com.rincew1nd.publictransportmap.Models.Scheduled.Trip;
import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;
import com.rincew1nd.publictransportmap.Models.Transport;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
import com.rincew1nd.publictransportmap.Models.WalkingPaths.Node;
import com.rincew1nd.publictransportmap.Utils.JsonSerializer;
import com.rincew1nd.publictransportmap.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class GraphManager {
    private static GraphManager _instance;

    private GraphManager() {
        Nodes = new HashMap<>();
        Paths = new HashSet<>();
    }
    public static GraphManager GetInstance() {
        if (_instance == null)
            _instance = new GraphManager();
        return _instance;
    }
    public void SetContext(MapsActivity context) {
        _context = context;
    }

    private MapsActivity _context;
    public Transport TransportGraph;

    public HashMap<Integer, GraphNode> Nodes;
    public HashSet<GraphPath> Paths;

    // Load markers from JSON file and generate icons
    public void LoadGraph() {
        JsonSerializer reader = new JsonSerializer(_context.getResources(), R.raw.transport);
        TransportGraph = reader.constructUsingGson(Transport.class);
    }

    public void LinkStructures() {
        // WalkingPaths
        for (com.rincew1nd.publictransportmap.Models.WalkingPaths.Path path:
                TransportGraph.WalkingPaths.Paths)
        {
            for (Node node: TransportGraph.WalkingPaths.Nodes)
                if (node.Id == path.FromNodeId)
                {
                    path.FromNode = node;
                    break;
                }
            for (Node node: TransportGraph.WalkingPaths.Nodes)
                if (node.Id == path.ToNodeId)
                {
                    path.ToNode = node;
                    break;
                }
        }

        // ScheduledTransport
        for (com.rincew1nd.publictransportmap.Models.Scheduled.Route route:
                TransportGraph.ScheduledTransport.Routes)
        {
            for (Trip trip: TransportGraph.ScheduledTransport.Trips)
                if (trip.RouteId == route.Id)
                {
                    route.Trips.put(trip.Id, trip);
                    trip.Route = route;
                }
        }
        for (StopTime stopTime:
                TransportGraph.ScheduledTransport.StopTimes)
        {
            for (Stop stop: TransportGraph.ScheduledTransport.Stops)
                if (stopTime.StopId == stop.Id)
                {
                    stopTime.Stop = stop;
                    break;
                }
        }
        for (Trip trip: TransportGraph.ScheduledTransport.Trips)
        {
            for (Calendar calendar: TransportGraph.ScheduledTransport.Calendars)
                if (calendar.Id == trip.CalendarId)
                {
                    trip.Calendar = calendar;
                    break;
                }
            for (StopTime stopTime: TransportGraph.ScheduledTransport.StopTimes)
                if (stopTime.TripId == trip.Id)
                    trip.StopTimes.add(stopTime);

            Collections.sort(trip.StopTimes);
        }

        // UnscheduledTransport
        // Path-FromNode,ToNode, Route-Stations
        for (com.rincew1nd.publictransportmap.Models.Unscheduled.Path path:
                TransportGraph.UnscheduledTransport.Paths)
        {
            for (Station station: TransportGraph.UnscheduledTransport.Stations)
                if (path.FromNodeId == station.Id)
                {
                    path.FromNode = station;
                    break;
                }
            for (Station station: TransportGraph.UnscheduledTransport.Stations)
                if (path.ToNodeId == station.Id)
                {
                    path.ToNode = station;
                    break;
                }
        }
        for (com.rincew1nd.publictransportmap.Models.Unscheduled.Route route:
                TransportGraph.UnscheduledTransport.Routes)
        {
            for (Station station: TransportGraph.UnscheduledTransport.Stations)
                if (station.RouteId == route.Id)
                    route.Stations.put(station.Id, station);
        }
    }

    public void ProcessGraph() {
        for (Station station : TransportGraph.UnscheduledTransport.Stations)
            Nodes.put(station.Id, new GraphNode(station));
        for (Node node: TransportGraph.WalkingPaths.Nodes)
            Nodes.put(node.Id, new GraphNode(node));
        for (Stop stop: TransportGraph.ScheduledTransport.Stops)
            Nodes.put(stop.Id, new GraphNode(stop));

        for (com.rincew1nd.publictransportmap.Models.Unscheduled.Path path:
                TransportGraph.UnscheduledTransport.Paths) {
            AddPath(new GraphPath(path));
            AddPath(new GraphPath(path).Reverse());
        }
        for (com.rincew1nd.publictransportmap.Models.WalkingPaths.Path path:
                TransportGraph.WalkingPaths.Paths) {
            AddPath(new GraphPath(path));
            AddPath(new GraphPath(path).Reverse());
        }
        for (Transfer transfer: TransportGraph.Transfers) {
            AddPath(new GraphPath(transfer));
            AddPath(new GraphPath(transfer).Reverse());
        }

        for (Trip trip: TransportGraph.ScheduledTransport.Trips)
            for (int i = 1; i < trip.StopTimes.size(); i++)
                Paths.add(new GraphPath(trip.StopTimes.get(i-1), trip.StopTimes.get(i)));

        for (GraphPath path: Paths)
            path.FromNode.Paths.add(path);
    }

    private void AddPath(GraphPath path) {
        if (IsUnique(path))
            Paths.add(path);
        else
            Log.d("NOT UNIQUE", String.format("%s(%d) - %s(%d)",
                path.FromNode.Name, path.FromNode.Id, path.ToNode.Name, path.ToNode.Id)
            );
    }

    private boolean IsUnique(GraphPath path) {
        for (GraphPath gPath: Paths)
            if (path == gPath)
                return false;
        return true;
    }

    public GraphNode GetNodeById(int nodeId) {
        return Nodes.get(nodeId);
    }
}
