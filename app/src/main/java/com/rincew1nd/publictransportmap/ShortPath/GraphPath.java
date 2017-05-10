package com.rincew1nd.publictransportmap.ShortPath;

import com.google.maps.android.geojson.GeoJsonFeature;
import com.rincew1nd.publictransportmap.Models.Path;

import java.util.HashSet;

public class GraphPath {
    public Path Path;
    public GraphNode FromNode;
    public GraphNode ToNode;

    public GraphPath (Path path, GraphNode from, GraphNode to) {
        Path = path;
        FromNode = from;
        ToNode = to;
    }
}
