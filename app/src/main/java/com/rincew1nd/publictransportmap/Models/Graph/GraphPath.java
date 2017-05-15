package com.rincew1nd.publictransportmap.Models.Graph;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Scheduled.StopTime;
import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;

import java.util.ArrayList;
import java.util.Calendar;

public class GraphPath {

    public GraphNode FromNode;
    public GraphNode ToNode;

    public boolean IsTransfer;
    public int Delay = 0;
    public int Time;
    public int Cost;

    public int Width = 30;
    public int PathColor = -16777216;

    public ArrayList<LatLng> MidPoints;

    public GraphPath (int from, int to, int time, int cost) {
        FromNode = GraphManager.GetInstance().GetNodeById(from);
        ToNode = GraphManager.GetInstance().GetNodeById(to);

        Time = time;
        Cost = cost;

        IsTransfer = (FromNode.Type != ToNode.Type);
    }

    public GraphPath (com.rincew1nd.publictransportmap.Models.Unscheduled.Path path) {
        this(path.FromNodeId, path.ToNodeId, path.Time, 0);
        if (!(IsTransfer || FromNode.RouteId != ToNode.RouteId)) {
            String color = null;
            for (com.rincew1nd.publictransportmap.Models.Unscheduled.Route route:
                    GraphManager.GetInstance().TransportGraph.UnscheduledTransport.Routes)
                if (route.Id == path.FromNode.RouteId)
                    color = route.Color;
            if (color != null)
                SetColor(color);
        }
    }

    public GraphPath (com.rincew1nd.publictransportmap.Models.WalkingPaths.Path path) {
        this(path.FromNodeId, path.ToNodeId, path.Time, 0);
        SetColor("FF0000");
    }

    public GraphPath (Transfer transfer) {
        this(transfer.FromNodeId, transfer.ToNodeId, transfer.Time, transfer.Cost);
        IsTransfer = true;
    }

    public GraphPath (StopTime from, StopTime to) {
        this(from.StopId, to.StopId, (to.ArrivalTime - from.DepartureTime)*60, 32);
        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60+
                   Calendar.getInstance().get(Calendar.MINUTE);
        Delay = from.DepartureTime - time;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!GraphPath.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final GraphPath other = (GraphPath) obj;

        if (this.FromNode != other.FromNode || this.ToNode != other.FromNode) {
            return false;
        }
        return true;
    }
}
