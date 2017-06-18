package com.rincew1nd.publictransportmap.Models.Transfers;

import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;

public class Transfer {
    public int FromNodeId;
    public int ToNodeId;

    public int Time;
    public int Cost;

    public Transfer() {}
    public Transfer(GraphPath path) {
        this.FromNodeId = path.FromNode.Id;
        this.ToNodeId = path.ToNode.Id;
        this.Time = path.Time;
        this.Cost = path.Cost;
    }
}
