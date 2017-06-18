package com.rincew1nd.publictransportmap.Models;

import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNodeType;

public class TransportNode {
    public GraphNodeType Type;
    public Integer Id;
    public String Name;
    public Float Lat;
    public Float Lon;

    public TransportNode() {}

    public TransportNode(GraphNode node)
    {
        this.Type = node.Type;
        this.Id = node.Id;
        this.Lat = node.Lat;
        this.Lon = node.Lon;
        this.Name = node.Name;
    }
}
