package com.rincew1nd.publictransportmap.Models.Unscheduled;

import java.util.HashMap;
import java.util.List;

public class Route {
    public int Id;
    public String Name;
    public List<Delay> Delay;
    public String Color;

    public transient HashMap<Integer, Station> Stations = new HashMap<>();
}

