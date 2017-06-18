package com.rincew1nd.publictransportmap.Models.WalkingPaths;

import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;
import com.rincew1nd.publictransportmap.Models.TransportNode;

public class Path extends Transfer {
    public TransportNode FromNode;
    public TransportNode ToNode;

    public Path() {}
    public Path(GraphPath path) {
        super(path);
        this.FromNode = (TransportNode)path.FromNode.original;
        this.ToNode = (TransportNode)path.ToNode.original;
    }
}
