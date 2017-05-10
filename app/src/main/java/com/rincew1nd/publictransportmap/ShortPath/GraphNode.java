package com.rincew1nd.publictransportmap.ShortPath;

import android.support.annotation.NonNull;

import com.rincew1nd.publictransportmap.Models.Node;
import com.rincew1nd.publictransportmap.Models.Path;

import java.util.HashSet;

public class GraphNode implements Comparable<GraphNode> {

    public int Id;
    public int RouteId;
    public int Delay;
    public int TotalTime;
    public HashSet<GraphPath> Paths;

    public GraphNode (Node node) {
        this.Id = node.Id;
        this.RouteId = node.RouteId;
        Paths = new HashSet<>();
    }

    @Override
    public int compareTo(@NonNull GraphNode o) {
        return TotalTime - o.TotalTime;
    }
}
