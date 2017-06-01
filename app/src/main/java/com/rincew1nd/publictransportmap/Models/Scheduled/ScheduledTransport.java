package com.rincew1nd.publictransportmap.Models.Scheduled;

import com.rincew1nd.publictransportmap.Models.TransportNode;

import java.util.List;

public class ScheduledTransport {
    public List<Route> Routes = null;
    public List<TransportNode> Stops = null;
    public List<StopTime> StopTimes = null;
    public List<Calendar> Calendars = null;
    public List<Trip> Trips = null;
}
