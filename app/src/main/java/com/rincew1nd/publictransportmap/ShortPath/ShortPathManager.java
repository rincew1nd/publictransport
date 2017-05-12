package com.rincew1nd.publictransportmap.ShortPath;

import android.util.Log;

import com.rincew1nd.publictransportmap.MarkersNodes.MapMarkerManager;
import com.rincew1nd.publictransportmap.Models.Metro.MetroMap;
import com.rincew1nd.publictransportmap.Models.Metro.Node;
import com.rincew1nd.publictransportmap.Models.Metro.Path;
import com.rincew1nd.publictransportmap.Models.Metro.Route;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

public class ShortPathManager {

    private static ShortPathManager _instance;
    private static HashMap<Integer, GraphNode> _graphNodes;
    private static HashMap<Integer, GraphNode> _optimizedGraph;
    private HashMap<Integer, GraphNode> algorithmReadyGraph;

    private int _fromNode;
    private int _toNode;
    private ArrayList<ShortestPathObj> _algorhitmResult;

    // Получить инстанс объекта
    public static ShortPathManager GetInstance() {
        if (_instance == null)
            _instance = new ShortPathManager();
        return _instance;
    }

    // Собрать граф из моделей
    private ShortPathManager() {
        MetroMap transportMap = MapMarkerManager.GetInstance()._transportMap;
        int maxId = 0;

        Calendar c = Calendar.getInstance();
        if (_graphNodes == null)
            _graphNodes = new HashMap<>();
        if (_graphNodes.size() == 0)
        {
            for (Node node: transportMap.Nodes)
            {
                _graphNodes.put(node.Id, new GraphNode(node));
                if (node.Id > maxId)
                    maxId = node.Id;
            }

            for (Path path: transportMap.Paths) {
                GraphNode fromNode = _graphNodes.get(path.FromNode);
                GraphNode toNode = _graphNodes.get(path.ToNode);

                fromNode.Paths.add(new GraphPath(path, fromNode, toNode));
                toNode.Paths.add(new GraphPath(path, toNode, fromNode));
            }
        }
        for(GraphNode gNode: _graphNodes.values()) {
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

        //OptimizeGraph(maxId);
    }

    // Поиск через Дейкстру
    public Hashtable<ArrayList<Integer>, Integer> FindShortPath(int fromNodeId, int toNodeId) {
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

    // Поиск пути с помощью DFS
    public ArrayList<ShortestPathObj> FindShortestPaths(int fromNodeId, int toNodeId, int depth) {
        // 43437
        _fromNode = fromNodeId;
        _toNode = toNodeId;
        _algorhitmResult = new ArrayList<>();

        //algorithmReadyGraph = new HashMap<>(_optimizedGraph);
        //RecoverOptimizedNodes(algorithmReadyGraph, fromNodeId, toNodeId);

        // Берем начальную точку поиска
        GraphNode firstNode = _graphNodes.get(_fromNode);
        // Создаём путь
        ArrayList<Integer> path = new ArrayList<>();
        path.add(firstNode.Id);
        // Создаём вес (время, пересадки, стоимость)
        int[] weight = new int[] {firstNode.TotalTime, 0, 10};

        DepthSearch(path, weight, firstNode, depth);
        Collections.sort(_algorhitmResult);
        return _algorhitmResult;
    }

    // Depth-First Search
    private void DepthSearch(ArrayList<Integer> path, int[] weight, GraphNode lastNode, int depth) {
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
            if (gPath.ToNode.OptimizedNodes.size() > 0)
                newPath.addAll(gPath.ToNode.OptimizedNodes);
            else
                newPath.add(gPath.ToNode.Id);
            // Определяем новый вес с учётом последней дуги
            int[] newWeight = new int[]{weight[0], weight[1], weight[2]};
            newWeight[0] += gPath.Path.Time + gPath.ToNode.Delay + gPath.ToNode.TotalTime;
            if (gPath.Path.RouteId < 0) {
                newWeight[1]++;
                newWeight[2] += 10;
            }
            DepthSearch(newPath, newWeight, gPath.ToNode, depth);
        }
    }

    // Оптимизация графа
    // TODO Вынести в новый класс

    // Инициация оптимизации графа
    private void OptimizeGraph(int maxId) {
        _optimizedGraph = new HashMap<>(_graphNodes);
        while (OptimizeCycle(_optimizedGraph, maxId))
            maxId++;
    }

    // Цикл оптимизации
    private boolean OptimizeCycle(HashMap<Integer, GraphNode> optimizedGraph, int maxId) {
        for (GraphNode node: optimizedGraph.values())
        {
            if (node.Paths.size() > 2)
                continue;

            for (GraphPath path: node.Paths) {
                if (FromOneRouteId(path))
                {
                    maxId++;

                    Node newNode = new Node();
                    newNode.Id = maxId;
                    newNode.RouteId = node.RouteId;
                    GraphNode newGNode = new GraphNode(newNode);
                    newGNode.TotalTime = (int)path.Path.Time + path.ToNode.Delay;

                    if (path.ToNode.Paths.size() > 1)
                    {
                        GraphNode neigbourNode = GetOtherPath(path.ToNode.Paths, path.FromNode.Id)
                                .ToNode;
                        GetPath(neigbourNode.Paths, path.ToNode.Id).ToNode = newGNode;

                        Path newPath = new Path();
                        newPath.FromNode = maxId;
                        newPath.ToNode = neigbourNode.Id;
                        newPath.Color = path.Path.Color;
                        newPath.RouteId = path.Path.RouteId;
                        newPath.Cost = path.Path.Cost;
                        newPath.Time = path.Path.Time;

                        GraphPath newGPath = new GraphPath(newPath,newGNode,neigbourNode);
                        newGNode.Paths.add(newGPath);
                    }
                    if (node.Paths.size() > 1)
                    {
                        GraphNode neigbourNode = GetOtherPath(node.Paths, path.ToNode.Id).ToNode;
                        GetPath(neigbourNode.Paths, node.Id).ToNode = newGNode;

                        Path newPath = new Path();
                        newPath.FromNode = maxId;
                        newPath.ToNode = neigbourNode.Id;
                        newPath.Color = path.Path.Color;
                        newPath.RouteId = path.Path.RouteId;
                        newPath.Cost = path.Path.Cost;
                        newPath.Time = 0; //

                        GraphPath newGPath = new GraphPath(newPath,newGNode,neigbourNode);
                        newGNode.Paths.add(newGPath);
                    }

                    newGNode.OptimizedNodes = new HashSet<>();
                    newGNode.OptimizedNodes.addAll(path.FromNode.OptimizedNodes);
                    newGNode.OptimizedNodes.addAll(path.ToNode.OptimizedNodes);
                    newGNode.OptimizedNodes.add(path.FromNode.Id);
                    newGNode.OptimizedNodes.add(path.ToNode.Id);

                    optimizedGraph.remove(path.FromNode.Id);
                    optimizedGraph.remove(path.ToNode.Id);
                    optimizedGraph.put(maxId, newGNode);

                    if (newGNode.OptimizedNodes.size() < 1)
                        Log.d("FAIL", "From " + path.FromNode.Id + " To " + path.ToNode.Id);

                    return true;
                }
            }
        }

        return false;
    }

    // Востановить оптимизированную ноду
    private void RecoverOptimizedNodes(HashMap<Integer, GraphNode> graph, int fromNode, int toNode) {
        boolean fromNodeExist = false;
        boolean toNodeExist = false;
        for (GraphNode node : graph.values()) {
            if (node.Id == fromNode)
                fromNodeExist = true;
            if (node.Id == toNode)
                toNodeExist = true;
        }
        try {
            if (!fromNodeExist)
                SplitOptimizedNodes(graph, fromNode);
            if (!toNodeExist)
                SplitOptimizedNodes(graph, toNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Разбить оптимизированную ноду
    private void SplitOptimizedNodes(HashMap<Integer, GraphNode> graph, int nodeId) throws Exception {
        GraphNode optimizedNode = null;
        for (GraphNode node: graph.values())
            if (node.OptimizedNodes.contains(nodeId))
                optimizedNode = node;

        if (optimizedNode == null)
            throw new Exception("Node not finded");

        for (int neededNodeId: optimizedNode.OptimizedNodes)
            if (_graphNodes.containsKey(neededNodeId))
                graph.put(neededNodeId, _graphNodes.get(neededNodeId));

        // По всем путям оптимизированной ноды
        for (GraphPath path: optimizedNode.Paths) {
            // По всем путям ноды смежной с оптимизированной нодой
            for (GraphPath optimizedPath: path.ToNode.Paths) {
                // Если путь до оптимизированной ноды
                if (optimizedPath.ToNode.Id == optimizedNode.Id) {
                    // Получить старый путь
                    GraphPath oldGraphPath = null;
                    GraphNode oldGraphNode = _graphNodes.get(path.ToNode.Id);
                    for (GraphPath oldPath: oldGraphNode.Paths)
                        if (optimizedNode.OptimizedNodes.contains(oldPath.ToNode.Id))
                            oldGraphPath = oldPath;
                    path.ToNode.Paths.remove(optimizedPath);
                    path.ToNode.Paths.add(oldGraphPath);
                }
            }
        }
        graph.remove(optimizedNode.Id);
    }

    // Принадлежат ли вершины и их соседи одному маршруту
    private boolean FromOneRouteId(GraphPath path) {
        if (path.FromNode.RouteId == path.ToNode.RouteId)
        {
            int routeId = path.Path.RouteId;
            for (GraphPath nPath: path.ToNode.Paths)
                if (nPath.ToNode.RouteId != routeId)
                    return false;
            for (GraphPath nPath: path.FromNode.Paths)
                if (nPath.ToNode.RouteId != routeId)
                    return false;
            return true;
        }
        else
            return false;
    }

    // Получить путь отличный от nodeId
    private GraphPath GetOtherPath(HashSet<GraphPath> paths, int nodeId) {
        for (GraphPath resultPath: paths)
            if (resultPath.ToNode.Id != nodeId)
                return resultPath;
        return null;
    }

    // Получить путь в nodeId
    private GraphPath GetPath(HashSet<GraphPath> paths, int nodeId) {
        for (GraphPath resultPath: paths)
            if (resultPath.ToNode.Id == nodeId)
                return resultPath;
        return null;
    }
}

