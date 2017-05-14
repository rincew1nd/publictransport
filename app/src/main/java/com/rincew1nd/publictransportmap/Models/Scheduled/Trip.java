package com.rincew1nd.publictransportmap.Models.Scheduled;

import java.util.HashSet;

public class Trip {
    public int Id;
    public int RouteId;
    public int CalendarId;

    public Route Route;
    public Calendar Calendar;

    public HashSet<StopTime> StopTimes = new HashSet<>();
}