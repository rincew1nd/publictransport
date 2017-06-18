package com.rincew1nd.publictransportmap.Models.Unscheduled;

import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.TransportNode;

public class Station extends TransportNode {
    public int RouteId;

    public Station() {}
    public Station(GraphNode node) {
        super(node);
        this.RouteId = node.RouteId;
    }
}
