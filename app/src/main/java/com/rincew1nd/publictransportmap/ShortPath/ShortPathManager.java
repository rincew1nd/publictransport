package com.rincew1nd.publictransportmap.ShortPath;

import android.util.Log;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;

import java.util.ArrayList;
import java.util.Collections;

public class ShortPathManager {

    private static ShortPathManager _instance;
    //private static HashMap<Integer, GraphNode> _optimizedGraph;

    private int _fromNode;
    private int _toNode;
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

    public ArrayList<ShortestPathObj> FindShortestPaths(int fromNodeId, int toNodeId, int depth) {
        _fromNode = fromNodeId;
        _toNode = toNodeId;
        _algorithmResult = new ArrayList<>();

        //RecoverOptimizedNodes(algorithmReadyGraph, fromNodeId, toNodeId);

        GraphNode firstNode = GraphManager.GetInstance().Nodes.get(_fromNode);
        ArrayList<Integer> path = new ArrayList<>();

        path.add(firstNode.Id);
        // Создаём вес (время, пересадки, стоимость)
        // TODO начальная стоимость поездки
        int[] weight = new int[] {0, 0, 10};

        DepthSearch(path, weight, firstNode, depth);
        Collections.sort(_algorithmResult);
        return _algorithmResult;
    }

    // Depth-First Search
    private void DepthSearch(ArrayList<Integer> path, int[] weight, GraphNode lastNode, int depth) {
        if (lastNode.Id == _toNode)
        {
            ShortestPathObj result = new ShortestPathObj(path, weight);
            _algorithmResult.add(result);
            return;
        }

        for(GraphPath gPath: lastNode.Paths) {
            if (path.contains(gPath.ToNode.Id) || weight[1] > depth)
                continue;
            if (gPath.Delay < 0)
                continue;

            ArrayList newPath = new ArrayList<>(path);
            if (gPath.ToNode.OptimizedNodes.size() > 0)
                newPath.addAll(gPath.ToNode.OptimizedNodes);
            else
                newPath.add(gPath.ToNode.Id);

            int[] newWeight = new int[]{weight[0], weight[1], weight[2]};
            newWeight[0] += gPath.Time;
            if (gPath.IsTransfer) {
                newWeight[0] += gPath.Delay;
                newWeight[1]++;
                // TODO добавить цену
                newWeight[2] += 10;
            }
            DepthSearch(newPath, newWeight, gPath.ToNode, depth);
        }
    }

//    // Инициация оптимизации графа
//    private void OptimizeGraph(int maxId) {
//        _optimizedGraph = new HashMap<>(_graphNodes);
//        while (OptimizeCycle(_optimizedGraph, maxId))
//            maxId++;
//    }
//
//    // Цикл оптимизации
//    private boolean OptimizeCycle(HashMap<Integer, GraphNode> optimizedGraph, int maxId) {
//        for (GraphNode node: optimizedGraph.values())
//        {
//            if (node.Paths.size() > 2)
//                continue;
//
//            for (GraphPath path: node.Paths) {
//                if (FromOneRouteId(path))
//                {
//                    maxId++;
//
//                    Station newStation = new Station();
//                    newStation.Id = maxId;
//                    newStation.RouteId = node.RouteId;
//                    GraphNode newGNode = new GraphNode(newStation);
//                    newGNode.TotalTime = (int)path.Path.Time + path.ToNode.Delay;
//
//                    if (path.ToNode.Paths.size() > 1)
//                    {
//                        GraphNode neigbourNode = GetOtherPath(path.ToNode.Paths, path.FromNode.Id)
//                                .ToNode;
//                        GetPath(neigbourNode.Paths, path.ToNode.Id).ToNode = newGNode;
//
//                        Path newPath = new Path();
//                        newPath.FromNode = maxId;
//                        newPath.ToNode = neigbourNode.Id;
//                        newPath.Color = path.Path.Color;
//                        newPath.RouteId = path.Path.RouteId;
//                        newPath.Cost = path.Path.Cost;
//                        newPath.Time = path.Path.Time;
//
//                        GraphPath newGPath = new GraphPath(newPath,newGNode,neigbourNode);
//                        newGNode.Paths.add(newGPath);
//                    }
//                    if (node.Paths.size() > 1)
//                    {
//                        GraphNode neigbourNode = GetOtherPath(node.Paths, path.ToNode.Id).ToNode;
//                        GetPath(neigbourNode.Paths, node.Id).ToNode = newGNode;
//
//                        Path newPath = new Path();
//                        newPath.FromNode = maxId;
//                        newPath.ToNode = neigbourNode.Id;
//                        newPath.Color = path.Path.Color;
//                        newPath.RouteId = path.Path.RouteId;
//                        newPath.Cost = path.Path.Cost;
//                        newPath.Time = 0; //
//
//                        GraphPath newGPath = new GraphPath(newPath,newGNode,neigbourNode);
//                        newGNode.Paths.add(newGPath);
//                    }
//
//                    newGNode.OptimizedNodes = new HashSet<>();
//                    newGNode.OptimizedNodes.addAll(path.FromNode.OptimizedNodes);
//                    newGNode.OptimizedNodes.addAll(path.ToNode.OptimizedNodes);
//                    newGNode.OptimizedNodes.add(path.FromNode.Id);
//                    newGNode.OptimizedNodes.add(path.ToNode.Id);
//
//                    optimizedGraph.remove(path.FromNode.Id);
//                    optimizedGraph.remove(path.ToNode.Id);
//                    optimizedGraph.put(maxId, newGNode);
//
//                    if (newGNode.OptimizedNodes.size() < 1)
//                        Log.d("FAIL", "From " + path.FromNode.Id + " To " + path.ToNode.Id);
//
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    // Востановить оптимизированную ноду
//    private void RecoverOptimizedNodes(HashMap<Integer, GraphNode> graph, int fromNode, int toNode) {
//        boolean fromNodeExist = false;
//        boolean toNodeExist = false;
//        for (GraphNode node : graph.values()) {
//            if (node.Id == fromNode)
//                fromNodeExist = true;
//            if (node.Id == toNode)
//                toNodeExist = true;
//        }
//        try {
//            if (!fromNodeExist)
//                SplitOptimizedNodes(graph, fromNode);
//            if (!toNodeExist)
//                SplitOptimizedNodes(graph, toNode);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Разбить оптимизированную ноду
//    private void SplitOptimizedNodes(HashMap<Integer, GraphNode> graph, int nodeId) throws Exception {
//        GraphNode optimizedNode = null;
//        for (GraphNode node: graph.values())
//            if (node.OptimizedNodes.contains(nodeId))
//                optimizedNode = node;
//
//        if (optimizedNode == null)
//            throw new Exception("Station not finded");
//
//        for (int neededNodeId: optimizedNode.OptimizedNodes)
//            if (_graphNodes.containsKey(neededNodeId))
//                graph.put(neededNodeId, _graphNodes.get(neededNodeId));
//
//        // По всем путям оптимизированной ноды
//        for (GraphPath path: optimizedNode.Paths) {
//            // По всем путям ноды смежной с оптимизированной нодой
//            for (GraphPath optimizedPath: path.ToNode.Paths) {
//                // Если путь до оптимизированной ноды
//                if (optimizedPath.ToNode.Id == optimizedNode.Id) {
//                    // Получить старый путь
//                    GraphPath oldGraphPath = null;
//                    GraphNode oldGraphNode = _graphNodes.get(path.ToNode.Id);
//                    for (GraphPath oldPath: oldGraphNode.Paths)
//                        if (optimizedNode.OptimizedNodes.contains(oldPath.ToNode.Id))
//                            oldGraphPath = oldPath;
//                    path.ToNode.Paths.remove(optimizedPath);
//                    path.ToNode.Paths.add(oldGraphPath);
//                }
//            }
//        }
//        graph.remove(optimizedNode.Id);
//    }
//
//    // Принадлежат ли вершины и их соседи одному маршруту
//    private boolean FromOneRouteId(GraphPath path) {
//        if (path.FromNode.RouteId == path.ToNode.RouteId)
//        {
//            int routeId = path.Path.RouteId;
//            for (GraphPath nPath: path.ToNode.Paths)
//                if (nPath.ToNode.RouteId != routeId)
//                    return false;
//            for (GraphPath nPath: path.FromNode.Paths)
//                if (nPath.ToNode.RouteId != routeId)
//                    return false;
//            return true;
//        }
//        else
//            return false;
//    }
//
//    // Получить путь отличный от nodeId
//    private GraphPath GetOtherPath(HashSet<GraphPath> paths, int nodeId) {
//        for (GraphPath resultPath: paths)
//            if (resultPath.ToNode.Id != nodeId)
//                return resultPath;
//        return null;
//    }
//
//    // Получить путь в nodeId
//    private GraphPath GetPath(HashSet<GraphPath> paths, int nodeId) {
//        for (GraphPath resultPath: paths)
//            if (resultPath.ToNode.Id == nodeId)
//                return resultPath;
//        return null;
//    }
}

