package com.rincew1nd.publictransportmap.Models.Scheduled;

import java.util.HashMap;

public class Route {
    public int Id;
    public String Name;

    // TODO добавить поддержку цвета
    public String Color;

    public HashMap<Integer, Trip> Trips = new HashMap<>();
}