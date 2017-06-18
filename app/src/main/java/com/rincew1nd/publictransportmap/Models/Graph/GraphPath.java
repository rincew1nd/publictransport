package com.rincew1nd.publictransportmap.Models.Graph;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Scheduled.StopTime;
import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class GraphPath {

    public GraphNode FromNode;
    public GraphNode ToNode;

    public GraphNodeType Type;
    public boolean IsTransfer;
    public int Delay = 0;
    public int Time;
    public int Cost;

    public Object original;

    public int Width = 30;
    public int PathColor = -16777216;

    public ArrayList<LatLng> MidPoints;

    public GraphPath (GraphNodeType type, int from, int to, int time, int cost,
                      HashMap<Integer, GraphNode> graph) {
        Type = type;
        FromNode = (from == -1) ? null : graph.get(from);
        ToNode = (to == -1) ? null : graph.get(to);

        Time = time;
        Cost = cost;
    }

    public GraphPath (com.rincew1nd.publictransportmap.Models.Unscheduled.Path path) {
        this(GraphNodeType.Unscheduled, path.FromNodeId, path.ToNodeId, path.Time, 0,
                GraphManager.GetInstance().Nodes);
        IsTransfer = (path.FromNode.RouteId != path.ToNode.RouteId || path.RouteId == -1);
        if (!(IsTransfer || FromNode.RouteId != ToNode.RouteId)) {
            String color = null;
            for (com.rincew1nd.publictransportmap.Models.Unscheduled.Route route:
                    GraphManager.GetInstance().TransportGraph.UnscheduledTransport.Routes)
                if (route.Id == path.FromNode.RouteId)
                    color = route.Color;
            if (color != null)
                SetColor(color);
        }
        original = path;
    }

    public GraphPath (com.rincew1nd.publictransportmap.Models.WalkingPaths.Path path) {
        this(GraphNodeType.Walking, path.FromNodeId, path.ToNodeId, path.Time, 0,
                GraphManager.GetInstance().Nodes);
        SetColor("FF0000");
        original = path;
    }

    public GraphPath (Transfer transfer) {
        this(GraphNodeType.None, transfer.FromNodeId, transfer.ToNodeId, transfer.Time,
                transfer.Cost, GraphManager.GetInstance().Nodes);
        IsTransfer = true;
        original = transfer;
    }

    public GraphPath (StopTime from, StopTime to) {
        this(GraphNodeType.Scheduled, from.StopId, to.StopId,
                (to.ArrivalTime - from.DepartureTime)*60, 32, GraphManager.GetInstance().Nodes);
        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60+
                   Calendar.getInstance().get(Calendar.MINUTE);
        Delay = (from.DepartureTime - time)*60;
        SetColor("FF00FF");
    }

    public GraphPath Reverse()
    {
        GraphNode toNode = ToNode;
        ToNode = FromNode;
        FromNode = toNode;
        return this;
    }

    public void SetColor(String color) {
        PathColor = Color.parseColor("#"+color);
        Width = 10;
    }

    public void AddMidPoint(LatLng midPoint, int order) {
        if (MidPoints.get(order) == null)
            MidPoints.set(order, midPoint);
        else
        {
            LatLng midPointBackup = MidPoints.get(order);
            MidPoints.set(order, midPoint);
            MidPoints.set(order+1, midPointBackup);
        }
    }
}
