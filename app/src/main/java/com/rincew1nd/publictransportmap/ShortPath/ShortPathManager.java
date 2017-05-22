package com.rincew1nd.publictransportmap.ShortPath;

import android.util.Log;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Settings;

import java.util.ArrayList;
import java.util.Collections;

public class ShortPathManager {

    private static ShortPathManager _instance;

    private GraphNode _fromNode;
    private GraphNode _toNode;
    private ArrayList<ShortestPathObj> _algorithmResult;

    public static ShortPathManager GetInstance() {
        if (_instance == null)
            _instance = new ShortPathManager();
        return _instance;
    }
    private ShortPathManager() {
        //OptimizeGraph(maxId);
    }

    // Поиск через Дейкстру
    //public Hashtable<ArrayList<Integer>, Integer> FindShortPath(int fromNodeId, int toNodeId) {
    //    ArrayList<GraphNode> _visitedNodes = new ArrayList<>();
    //    ArrayList<GraphNode> _notVisitedNodes = new ArrayList<>(_graphNodes.values());
    //    HashMap<GraphNode, GraphNode> _shortestPath = new HashMap<>();
    //    for(Map.Entry nodeEntry: _graphNodes.entrySet()) {
    //        GraphNode gNode = (GraphNode) nodeEntry.getValue();
    //        gNode.TotalTime = Integer.MAX_VALUE;
    //    }
    //    GraphNode fromNode = _graphNodes.get(fromNodeId);
    //    fromNode.TotalTime = 0;
    //    Collections.sort(_notVisitedNodes);
    //    while (_notVisitedNodes.size() != 0) {
    //        GraphNode iterNode = _notVisitedNodes.get(0);
    //        _notVisitedNodes.remove(0);
    //        _visitedNodes.add(iterNode);
    //        for (GraphPath path: iterNode.Paths) {
    //            int nextTime = iterNode.TotalTime + iterNode.Delay + (int)path.Path.Time;
    //            if (path.ToNode.TotalTime > nextTime) {
    //                path.ToNode.TotalTime = nextTime;
    //                _shortestPath.put(path.ToNode, iterNode);
    //            }
    //        }
    //        Collections.sort(_notVisitedNodes);
    //    }
    //    ArrayList<Integer> shortestPath = new ArrayList<>();
    //    GraphNode shortestPathNode = _graphNodes.get(toNodeId);
    //    int totalTime = shortestPathNode.TotalTime;
    //    shortestPath.add(shortestPathNode.Id);
    //    while (true) {
    //        if (!_shortestPath.containsKey(shortestPathNode))
    //            break;
    //        shortestPathNode = _shortestPath.get(shortestPathNode);
    //        shortestPath.add(shortestPathNode.Id);
    //    }
    //    Hashtable<ArrayList<Integer>, Integer> resultPath = new Hashtable<>();
    //    resultPath.put(shortestPath, totalTime);
    //    return resultPath;
    //}

    public ArrayList<ShortestPathObj> FindShortestPaths() {
        _algorithmResult = new ArrayList<>();

        //RecoverOptimizedNodes(algorithmReadyGraph, fromNodeId, toNodeId);

        _fromNode = GraphManager.GetInstance().Nodes.get(Settings.FromStationId);
        _toNode = GraphManager.GetInstance().Nodes.get(Settings.ToStationId);
        ArrayList<Integer> path = new ArrayList<>();

        path.add(_fromNode.Id);
        // Создаём вес (время, пересадки, стоимость)
        // TODO начальная стоимость поездки
        int[] weight = new int[] {0, 0, 0};

        Log.d("SEARCH_START", " ");
        DepthSearch(path, weight, _fromNode, true);
        Collections.sort(_algorithmResult);
        Log.d("SEARCH_END", " ");
        return _algorithmResult;
    }

    private void DepthSearch(ArrayList<Integer> path, int[] weight,
                             GraphNode lastNode, boolean addDelay) {
        boolean addDelayCopy = false;
        if (lastNode.Id == _toNode.Id)
        {
            ShortestPathObj result = new ShortestPathObj(path, weight);
            _algorithmResult.add(result);
            return;
        }

        for(GraphPath gPath: lastNode.Paths) {
            if (path.contains(gPath.ToNode.Id) || weight[1] > Settings.SearchDepth)
                continue;
            if (weight[1] == Settings.SearchDepth &&
                (gPath.ToNode.Type != _toNode.Type ||
                 gPath.ToNode.RouteId != _toNode.RouteId))
                    continue;
            if (ContainsTransferToVisitedNode(lastNode, path))
                continue;
            if (gPath.Delay < 0)
                continue;
            //TODO Пересадка на уже ушедшие электрички

            ArrayList newPath = new ArrayList<>(path);
            if (gPath.ToNode.OptimizedNodes.size() > 0)
                newPath.addAll(gPath.ToNode.OptimizedNodes);
            else
                newPath.add(gPath.ToNode.Id);

            int[] newWeight = new int[]{weight[0], weight[1], weight[2]};
            newWeight[0] += gPath.Time + ((addDelay) ? gPath.Delay : 0);
            if (gPath.IsTransfer) {
                addDelayCopy = true;
                newWeight[1]++;
                newWeight[2] += gPath.Cost;
            }
            DepthSearch(newPath, newWeight, gPath.ToNode, addDelayCopy);
        }
    }

    public boolean ContainsTransferToVisitedNode(GraphNode gNode, ArrayList<Integer> path) {
        if (path.size() > 2)
            for (GraphPath gPath: gNode.Paths) {
                if (gPath.ToNode.Id != path.get(path.size()-2))
                    if(path.contains(gPath.ToNode.Id))
                        return true;
            }
        return false;
    }
}

