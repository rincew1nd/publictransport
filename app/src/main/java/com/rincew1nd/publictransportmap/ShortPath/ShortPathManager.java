package com.rincew1nd.publictransportmap.ShortPath;

import com.rincew1nd.publictransportmap.MarkersNodes.MapMarkerManager;
import com.rincew1nd.publictransportmap.Models.Node;
import com.rincew1nd.publictransportmap.Models.Path;
import com.rincew1nd.publictransportmap.Models.PublicTransportMap;
import com.rincew1nd.publictransportmap.Models.Route;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

public class ShortPathManager {

    private static ShortPathManager _instance;
    private static HashSet<GraphNode> _graphNodes;

    public static ShortPathManager GetInstance() {
        if (_instance == null)
            _instance = new ShortPathManager();
        return _instance;
    }

    private ShortPathManager() {
        PublicTransportMap transportMap = MapMarkerManager.GetInstance()._transportMap;

        Calendar c = Calendar.getInstance();
        if (_graphNodes == null)
            _graphNodes = new HashSet<>();
        if (_graphNodes.size() == 0)
        {
            for (Node node: transportMap.Nodes)
                _graphNodes.add(new GraphNode(node));

            for (Path path: transportMap.Paths) {
                GraphNode fromNode = GetNodeById(path.FromNode);
                GraphNode toNode = GetNodeById(path.ToNode);

                fromNode.Paths.add(new GraphPath(path, fromNode, toNode));
                toNode.Paths.add(new GraphPath(path, toNode, fromNode));
            }
        }
        for(GraphNode gNode: _graphNodes) {
            for (Route route: transportMap.Routes) {
                if (route.Id == gNode.RouteId) {
                    int timeInMinutes = c.get(Calendar.SECOND) / 60;
                    boolean isDelaySetted = false;
                    if (route.Delay != null)
                        for (int i = 0; i < route.Delay.size(); i++)
                            if (route.Delay.get(i).Time < timeInMinutes) {
                                gNode.Delay = route.Delay.get(i).Seconds;
                                isDelaySetted = true;
                                break;
                            }
                    if (!isDelaySetted)
                        gNode.Delay = 60;
                    break;
                }
            }
        }
    }

    public Hashtable<ArrayList<Integer>, Integer> FindShortPath(int fromNodeId, int toNodeId)
    {
        ArrayList<GraphNode> _visitedNodes = new ArrayList<>();
        ArrayList<GraphNode> _notVisitedNodes = new ArrayList<>(_graphNodes);
        HashMap<GraphNode, GraphNode> _shortestPath = new HashMap<>();

        for(GraphNode node: _graphNodes)
            node.TotalTime = Integer.MAX_VALUE;

        GraphNode fromNode = GetNodeById(fromNodeId);
        fromNode.TotalTime = 0;

        Collections.sort(_notVisitedNodes);

        while (_notVisitedNodes.size() != 0)
        {
            GraphNode iterNode = _notVisitedNodes.get(0);
            _notVisitedNodes.remove(0);
            _visitedNodes.add(iterNode);

            for (GraphPath path: iterNode.Paths) {
                int nextTime = iterNode.TotalTime + iterNode.Delay + (int)path.Path.Time;
                if (path.ToNode.TotalTime > nextTime)
                {
                    path.ToNode.TotalTime = nextTime;
                    _shortestPath.put(path.ToNode, iterNode);
                }
            }

            Collections.sort(_notVisitedNodes);
        }

        ArrayList<Integer> shortestPath = new ArrayList<>();
        GraphNode shortestPathNode = GetNodeById(toNodeId);
        int totalTime = shortestPathNode.TotalTime;
        shortestPath.add(shortestPathNode.Id);

        while (true)
        {
            if (!_shortestPath.containsKey(shortestPathNode))
                break;
            shortestPathNode = _shortestPath.get(shortestPathNode);
            shortestPath.add(shortestPathNode.Id);
        }

        Hashtable<ArrayList<Integer>, Integer> resultPath = new Hashtable<>();
        resultPath.put(shortestPath, totalTime);
        return resultPath;
    }

    private GraphNode GetNodeById(int id)
    {
        for(GraphNode gNode: _graphNodes)
            if (gNode.Id == id)
                return gNode;
        return null;
    }
}
