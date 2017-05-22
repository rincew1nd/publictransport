package com.rincew1nd.publictransportmap.Models.Unscheduled;

public class Path
{
    // TODO расчёт динамически
    // TODO выпилить
    public int RouteId;
    public int FromNodeId;
    public int ToNodeId;

    public int Time;
    public int Cost;
    
    public Station FromNode;
    public Station ToNode;
}
