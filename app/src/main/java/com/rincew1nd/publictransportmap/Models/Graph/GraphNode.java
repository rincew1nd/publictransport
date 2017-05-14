package com.rincew1nd.publictransportmap.Models.Graph;

import com.rincew1nd.publictransportmap.Models.Scheduled.Stop;
import com.rincew1nd.publictransportmap.Models.Unscheduled.Station;
import com.rincew1nd.publictransportmap.Models.WalkingPaths.Node;

import java.util.HashSet;

public class GraphNode {

    public GraphNodeType Type;
    public int Id;
    public String Name;
    public int RouteId;

    // Связи с другими нодами
    public HashSet<GraphPath> Paths;

    // Ноды вошедшие в оптимизированную ноду
    public HashSet<GraphNode> OptimizedNodes;

    public GraphNode (GraphNodeType type, int id, String name, int routeId) {
        Type = type;
        Id = id;
        Name = name;
        RouteId = routeId;

        Paths = new HashSet<>();
        OptimizedNodes = new HashSet<>();
    }

    public GraphNode (Station station) {
        this(GraphNodeType.Unscheduled, station.Id, station.Name, station.RouteId);
    }

    public GraphNode (Node node) {
        this(GraphNodeType.Walking, node.Id, node.Name, node.RouteId);
    }

    public GraphNode (Stop node, int routeId) {
        this(GraphNodeType.Scheduled, node.Id, node.Name, routeId);
    }
}
