package com.rincew1nd.publictransportmap.Models.Unscheduled;

public class Path
{
    public int RouteId;
    public int FromNodeId;
    public int ToNodeId;

    public int Time;
    public int Cost;
    public String Color;
    
    public Station FromNode;
    public Station ToNode;
}
