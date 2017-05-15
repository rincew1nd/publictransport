package com.rincew1nd.publictransportmap.Models.Scheduled;

import android.support.annotation.NonNull;

public class StopTime implements Comparable<StopTime>{
    public int StopId;
    public int TripId;
    public int StopSequence;
    public int DepartureTime;
    public int ArrivalTime;

    public Stop Stop;

    @Override
    public int compareTo(@NonNull StopTime o) {
        return StopSequence - o.StopSequence;
    }
}
