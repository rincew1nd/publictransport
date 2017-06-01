package com.rincew1nd.publictransportmap.Models.WalkingPaths;

import com.rincew1nd.publictransportmap.Models.TransportNode;

public class Path {
    public int FromNodeId;
    public int ToNodeId;
    public String Name;
    public int Time;

    public TransportNode FromNode;
    public TransportNode ToNode;
}
