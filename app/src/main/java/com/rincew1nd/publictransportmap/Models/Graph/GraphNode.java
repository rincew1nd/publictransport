package com.rincew1nd.publictransportmap.Models.Graph;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Scheduled.Stop;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
import com.rincew1nd.publictransportmap.Models.WalkingPaths.Node;

import android.graphics.Color;
import java.util.HashSet;

public class GraphNode {

    public GraphNodeType Type;

    public int Id;
    public String Name;
    public int RouteId;
    public float Lat;
    public float Lon;
    public int Delay;
    public int NodeColor;

    // Связи с другими нодами
    public HashSet<GraphPath> Paths;
    // Ноды вошедшие в оптимизированную ноду
    public HashSet<GraphNode> OptimizedNodes;

    public GraphNode (GraphNodeType type, int id, String name, float lat, float lon) {
        Type = type;
        Id = id;
        Name = name;
        Lat = lat;
        Lon = lon;

        Paths = new HashSet<>();
        OptimizedNodes = new HashSet<>();
    }

    public GraphNode (Station station) {
        this(GraphNodeType.Unscheduled, station.Id, station.Name, station.Lat, station.Lon);
        String color = null;
        for(com.rincew1nd.publictransportmap.Models.Unscheduled.Route route:
                GraphManager.GetInstance().TransportGraph.UnscheduledTransport.Routes)
            if (route.Id == station.RouteId)
                color = route.Color;
        if (color == null)
            color = "000000";
        SetColor(color);
        RouteId = station.RouteId;
    }

    public GraphNode (Node node) {
        this(GraphNodeType.Walking, node.Id, node.Name, node.Lat, node.Lon);
        SetColor("FF0000");
    }

    public GraphNode (Stop stop) {
        this(GraphNodeType.Scheduled, stop.Id, stop.Name, stop.Lat, stop.Lon);
        SetColor("FF00FF");
    }

    private void SetColor(String color) {
        NodeColor = Color.parseColor("#"+color);
    }
}
