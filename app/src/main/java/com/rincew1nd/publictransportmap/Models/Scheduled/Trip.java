package com.rincew1nd.publictransportmap.Models.Scheduled;

import java.util.ArrayList;

public class Trip{
    public int Id;
    public int RouteId;
    public int CalendarId;

    public transient Route Route;
    public transient Calendar Calendar;
    public transient ArrayList<StopTime> StopTimes = new ArrayList<>();
}