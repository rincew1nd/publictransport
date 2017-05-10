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
import java.util.Map;

public class ShortPathManager {

    private static ShortPathManager _instance;
    private static HashMap<Integer, GraphNode> _graphNodes;
    private int _fromNode;
    private int _toNode;
    private ArrayList<ShortestPathObj> _algorhitmResult;

    public static ShortPathManager GetInstance() {
        if (_instance == null)
            _instance = new ShortPathManager();
        return _instance;
    }

    private ShortPathManager() {
        PublicTransportMap transportMap = MapMarkerManager.GetInstance()._transportMap;

        Calendar c = Calendar.getInstance();
        if (_graphNodes == null)
            _graphNodes = new HashMap<>();
        if (_graphNodes.size() == 0)
        {
            for (Node node: transportMap.Nodes)
                _graphNodes.put(node.Id, new GraphNode(node));

            for (Path path: transportMap.Paths) {
                GraphNode fromNode = _graphNodes.get(path.FromNode);
                GraphNode toNode = _graphNodes.get(path.ToNode);

                fromNode.Paths.add(new GraphPath(path, fromNode, toNode));
                toNode.Paths.add(new GraphPath(path, toNode, fromNode));
            }
        }
        for(Map.Entry nodeEntry: _graphNodes.entrySet()) {
            GraphNode gNode = (GraphNode) nodeEntry.getValue();
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
        ArrayList<GraphNode> _notVisitedNodes = new ArrayList<>(_graphNodes.values());
        HashMap<GraphNode, GraphNode> _shortestPath = new HashMap<>();

        for(Map.Entry nodeEntry: _graphNodes.entrySet()) {
            GraphNode gNode = (GraphNode) nodeEntry.getValue();
            gNode.TotalTime = Integer.MAX_VALUE;
        }

        GraphNode fromNode = _graphNodes.get(fromNodeId);
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
        GraphNode shortestPathNode = _graphNodes.get(toNodeId);
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

    public ArrayList<ShortestPathObj> FindShortestPaths(int fromNodeId, int toNodeId, int depth)
    {
        // 43437
        _fromNode = fromNodeId;
        _toNode = toNodeId;
        _algorhitmResult = new ArrayList<>();

        // Берем начальную точку поиска
        GraphNode firstNode = _graphNodes.get(_fromNode);
        // Создаём путь
        ArrayList<Integer> path = new ArrayList<>();
        path.add(firstNode.Id);
        // Создаём вес (время, пересадки, стоимость)
        int[] weight = new int[] {0, 0, 10};

        DepthSearch(path, weight, firstNode, depth);
        Collections.sort(_algorhitmResult);
        return _algorhitmResult;
    }

    private void DepthSearch(ArrayList<Integer> path, int[] weight, GraphNode lastNode, int depth)
    {
        if (lastNode.Id == _toNode)
        {
            ShortestPathObj result = new ShortestPathObj(path, weight);
            _algorhitmResult.add(result);
            return;
        }

        for(GraphPath gPath: lastNode.Paths) {
            // Предотвращаем петли
            if (path.contains(gPath.ToNode.Id) || weight[1] > depth)
                continue;
            // Создаём новый путь с учётом последней дуги
            ArrayList newPath = new ArrayList<>(path);
            newPath.add(gPath.ToNode.Id);
            // Определяем новый вес с учётом последней дуги
            int[] newWeight = new int[]{weight[0], weight[1], weight[2]};
            newWeight[0] += gPath.Path.Time + gPath.ToNode.Delay;
            if (gPath.Path.RouteId < 0) {
                newWeight[1]++;
                newWeight[2] += 10;
            }
            DepthSearch(newPath, newWeight, gPath.ToNode, depth);
        }
    }
}
