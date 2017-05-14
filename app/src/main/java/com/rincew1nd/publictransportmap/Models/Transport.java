package com.rincew1nd.publictransportmap.Models;

import com.rincew1nd.publictransportmap.Models.Transfers.Transfer;
import com.rincew1nd.publictransportmap.Models.Unscheduled.UnscheduledTransport;
import com.rincew1nd.publictransportmap.Models.Scheduled.ScheduledTransport;
import com.rincew1nd.publictransportmap.Models.WalkingPaths.WalkingPaths;

import java.util.HashSet;

public class Transport {
    public UnscheduledTransport UnscheduledTransport;
    public ScheduledTransport ScheduledTransport;
    public WalkingPaths WalkingPaths;
    public HashSet<Transfer> Transfers;
}
