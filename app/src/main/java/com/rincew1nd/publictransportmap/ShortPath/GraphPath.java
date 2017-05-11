package com.rincew1nd.publictransportmap.ShortPath;

import com.rincew1nd.publictransportmap.Models.Path;

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
