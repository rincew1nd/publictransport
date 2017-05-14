package com.rincew1nd.publictransportmap.Models.Graph;

import android.graphics.Color;
import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;

public class GraphPath {

    public GraphNode FromNode;
    public GraphNode ToNode;

    public boolean IsTransfer;
    public int Time;
    public int Cost;

    // TODO Хранить как-то по другому
    public int PathColor = 0;

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
        SetColor("FF00FF");
    }

    public GraphPath (Transfer transfer) {
        this(transfer.FromNodeId, transfer.ToNodeId, transfer.Time, transfer.Cost);
        IsTransfer = true;
    }

    public void SetColor(String color) {
        PathColor = Color.parseColor("#"+color);
    }
}
