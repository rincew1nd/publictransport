package com.rincew1nd.publictransportmap.Models.Metro;

import com.google.android.gms.maps.model.Polyline;

public class Path
{
    public int RouteId;
    public int FromNode;
    public int ToNode;

    public float Time;
    public int Cost;
    public String Color;

    public Polyline Line;
}
