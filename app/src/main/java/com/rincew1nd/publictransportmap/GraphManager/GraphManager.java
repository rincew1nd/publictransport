package com.rincew1nd.publictransportmap.GraphManager;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rincew1nd.publictransportmap.Activities.MapsActivity;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNodeType;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Scheduled.Calendar;
import com.rincew1nd.publictransportmap.Models.Scheduled.StopTime;
import com.rincew1nd.publictransportmap.Models.Scheduled.Trip;
import com.rincew1nd.publictransportmap.Models.Settings;
import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;
import com.rincew1nd.publictransportmap.Models.Transport;
import com.rincew1nd.publictransportmap.Models.TransportNode;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
import com.rincew1nd.publictransportmap.ShortPath.GraphOptimization;
import com.rincew1nd.publictransportmap.Utils.FileOperations;
import com.rincew1nd.publictransportmap.Utils.JsonSerializer;
import com.rincew1nd.publictransportmap.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class GraphManager {
    private static GraphManager _instance;

    private GraphManager() {
        Nodes = new HashMap<>();
        Paths = new ArrayList<>();
    }
    public static GraphManager GetInstance() {
        if (_instance == null)
            _instance = new GraphManager();
        return _instance;
    }
    public void SetContext(Context context) {
        _context = context;
    }

    private Context _context;
    public Transport TransportGraph;
    private String _lastJsonFile;

    public HashMap<Integer, GraphNode> Nodes;
    public ArrayList<GraphPath> Paths;

    // Load markers from JSON file and generate icons
    public void LoadGraph() {
        if (_lastJsonFile != null)
            SaveToJson();
        Nodes.clear();
        Paths.clear();

        CheckDefaultMapFile();
        File file = new File(
                Environment.getExternalStorageDirectory() + File.separator +
                "transportmap" + File.separator + Settings.mapFilePath);
        JsonSerializer reader = new JsonSerializer(file);
        TransportGraph = reader.constructUsingGson(Transport.class);
        LinkStructures();
        ProcessGraph();
        GraphOptimization.GetInstance().OptimizeGraph(NextNodeId(GraphManager.GetInstance().Nodes));
        _lastJsonFile = Settings.mapFilePath;
    }

    private void CheckDefaultMapFile() {
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "transportmap");
        File file = new File(dir, Settings.mapFilePath);
        if (!dir.exists())
            dir.mkdir();
        if (!file.exists())
            try {
                FileOperations.CopyRAWtoSDCard(
                        R.raw.transport,
                        file.getPath(),
                        _context);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void SaveToJson() {
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "transportmap");
        File file = new File(dir, _lastJsonFile);
        Gson gson = new GsonBuilder().create();
        try {
            FileOutputStream stream = new FileOutputStream(file);
            String output = gson.toJson(TransportGraph);
            stream.write(output.getBytes());
            stream.close();
        } catch (IOException ex) {}
    }

    private void LinkStructures() {
        // WalkingPaths
        for (com.rincew1nd.publictransportmap.Models.WalkingPaths.Path path:
                TransportGraph.WalkingPaths.Paths) {
            for (TransportNode node: TransportGraph.WalkingPaths.Nodes)
                if (node.Id == path.FromNodeId) {
                    path.FromNode = node;
                    break;
                }
            for (TransportNode node: TransportGraph.WalkingPaths.Nodes)
                if (node.Id == path.ToNodeId) {
                    path.ToNode = node;
                    break;
                }
        }

        // ScheduledTransport
        for (com.rincew1nd.publictransportmap.Models.Scheduled.Route route:
                TransportGraph.ScheduledTransport.Routes) {
            for (Trip trip: TransportGraph.ScheduledTransport.Trips)
                if (trip.RouteId == route.Id) {
                    route.Trips.put(trip.Id, trip);
                    trip.Route = route;
                }
        }
        for (StopTime stopTime:
                TransportGraph.ScheduledTransport.StopTimes) {
            for (TransportNode stop: TransportGraph.ScheduledTransport.Stops)
                if (stopTime.StopId == stop.Id) {
                    stopTime.Stop = stop;
                    break;
                }
        }
        for (Trip trip: TransportGraph.ScheduledTransport.Trips) {
            for (Calendar calendar: TransportGraph.ScheduledTransport.Calendars)
                if (calendar.Id == trip.CalendarId) {
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
                TransportGraph.UnscheduledTransport.Paths) {
            for (Station station: TransportGraph.UnscheduledTransport.Stations)
                if (path.FromNodeId == station.Id) {
                    path.FromNode = station;
                    break;
                }
            for (Station station: TransportGraph.UnscheduledTransport.Stations)
                if (path.ToNodeId == station.Id) {
                    path.ToNode = station;
                    break;
                }
        }
        for (com.rincew1nd.publictransportmap.Models.Unscheduled.Route route:
                TransportGraph.UnscheduledTransport.Routes) {
            for (Station station: TransportGraph.UnscheduledTransport.Stations)
                if (station.RouteId == route.Id)
                    route.Stations.put(station.Id, station);
        }
    }

    private void ProcessGraph() {
        for (Station station : TransportGraph.UnscheduledTransport.Stations)
            Nodes.put(station.Id, new GraphNode(station));
        for (TransportNode node: TransportGraph.WalkingPaths.Nodes)
            Nodes.put(node.Id, new GraphNode(node, GraphNodeType.Walking));
        for (TransportNode stop: TransportGraph.ScheduledTransport.Stops)
            Nodes.put(stop.Id, new GraphNode(stop, GraphNodeType.Scheduled));

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

    public int NextNodeId(HashMap<Integer, GraphNode> nodes) {
        int maxId = -1;
        for (GraphNode node : nodes.values())
            if (maxId < node.Id)
                maxId = node.Id;
        maxId++;
        return maxId;
    }

    private boolean IsUnique(GraphPath path) {
        for (GraphPath gPath: Paths)
            if (path == gPath)
                return false;
        return true;
    }

    public HashSet<TransportNode> NodesFromScheduledTransportRouteId(int routeId) {
        HashSet<TransportNode> result = new HashSet<>();
        com.rincew1nd.publictransportmap.Models.Scheduled.Route route = null;

        for (com.rincew1nd.publictransportmap.Models.Scheduled.Route droute:
                TransportGraph.ScheduledTransport.Routes)
            if (droute.Id == routeId)
                route = droute;
        if (route == null) return null;
        for (Trip trip: route.Trips.values()) {
            for (StopTime stopTime: trip.StopTimes)
                if (!result.contains(stopTime.Stop))
                    result.add(stopTime.Stop);
        }

        return result;
    }
}
