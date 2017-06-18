package com.rincew1nd.publictransportmap.Models.Unscheduled;

import android.widget.FrameLayout;

import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;

public class Path extends Transfer
{
    public int RouteId;
    
    public Station FromNode;
    public Station ToNode;

    public Path() {}
    public Path(GraphPath path) {
        super(path);
        this.RouteId = (path.FromNode.RouteId == path.ToNode.RouteId) ?
                path.ToNode.RouteId : -1;
        this.FromNode = (Station)path.FromNode.original;
        this.ToNode = (Station)path.ToNode.original;
    }
}
