package com.rincew1nd.publictransportmap.Models.Graph;

public class GraphPath {

    public GraphNode FromNode;
    public GraphNode ToNode;

    public boolean IsTransfer;
    public int RouteId;
    public int Time;
    public int Cost;

    // TODO Хранить как-то по другому
    public String Color;

    public GraphPath (GraphNode from, GraphNode to, int routeId, int time, int cost, String color) {
        FromNode = from;
        ToNode = to;

        RouteId = routeId;
        Time = time;
        Cost = cost;
        Color = color;

        IsTransfer = from.RouteId != to.RouteId;
    }

    public GraphPath (GraphNode from, GraphNode to, com.rincew1nd.publictransportmap.Models.Unscheduled.Path path) {
        FromNode = from;
        ToNode = to;

        RouteId = path.RouteId;
        Time = path.Time;
        Cost = 0;
        Color = path.Color;

        IsTransfer = from.RouteId != to.RouteId;
    }

    public GraphPath (GraphNode from, GraphNode to, com.rincew1nd.publictransportmap.Models.WalkingPaths.Path path) {
        FromNode = from;
        ToNode = to;

        RouteId = path.RouteId;
        Time = path.Time;
        Color = "FF00FF";
        Cost = 0;

        IsTransfer = from.RouteId != to.RouteId;
    }
}
