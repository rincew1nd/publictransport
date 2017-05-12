package com.rincew1nd.publictransportmap.ShortPath;

import android.support.annotation.NonNull;

import com.rincew1nd.publictransportmap.Models.Metro.Node;

import java.util.HashSet;

public class GraphNode implements Comparable<GraphNode> {

    public int Id;
    public int RouteId;
    public String Name;
    public int Delay;
    public int TotalTime;
    public HashSet<GraphPath> Paths;
    public HashSet<Integer> OptimizedNodes;

    public GraphNode (Node node) {
        this.Id = node.Id;
        this.RouteId = node.RouteId;
        this.Name = node.Name;
        Paths = new HashSet<>();
        OptimizedNodes = new HashSet<>();
    }

    @Override
    public int compareTo(@NonNull GraphNode o) {
        return TotalTime - o.TotalTime;
    }
}
