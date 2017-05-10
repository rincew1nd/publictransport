package com.rincew1nd.publictransportmap.ShortPath;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class ShortestPathObj implements Comparable<ShortestPathObj>{
    public ArrayList<Integer> Path;
    public int[] Criteria;

    public ShortestPathObj(ArrayList<Integer> path, int[] criteria)
    {
        Path = path;
        Criteria = criteria;
    }

    @Override
    public int compareTo(@NonNull ShortestPathObj o) {
        return Criteria[0] - o.Criteria[0];
    }
}
