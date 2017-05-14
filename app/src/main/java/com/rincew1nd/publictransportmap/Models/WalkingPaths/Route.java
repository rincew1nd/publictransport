package com.rincew1nd.publictransportmap.Models.WalkingPaths;

import java.util.HashMap;

public class Route {
    public int Id;
    public String Name;
    public String Color;

    public HashMap<Integer, Node> Nodes = new HashMap<>();
}
