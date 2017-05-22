package com.rincew1nd.publictransportmap.ShortPath;

import android.util.Log;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GraphOptimization {

    public HashMap<Integer, GraphNode> OptimizedNodes;
    public HashSet<GraphPath> OptimizedPaths;

    public void OptimizeGraph(int maxId) {
        Copy();
        while (OptimizeCycle(maxId))
            maxId++;
    }

    // Цикл оптимизации
    private boolean OptimizeCycle(int maxId) {
        for (GraphNode node: OptimizedNodes.values())
        {
            if (node.Paths.size() > 2 || !node.IsOptimizable)
                continue;

            if (node.Name.equals("Optimized"))
                Log.d("", "");

            for (GraphPath path: node.Paths) {
                if (CheckCriteria(path))
                {
                    maxId++;

                    if (OptimizedPaths.contains(path))  //TRUE
                        Log.d("","");
                    if (OptimizedPaths.remove(path))    //FALSE
                        Log.d("","");
                    OptimizedPaths.remove(GetPathToNode(path.ToNode, path.FromNode));

                    GraphNode optimizedNode = new GraphNode(node.Type, maxId, "Optimized", node.Lat, node.Lon);
                    optimizedNode.RouteId = path.ToNode.RouteId;
                    optimizedNode.SetColor("000000");
                    optimizedNode.OptimizedTime = path.Time + node.OptimizedTime;

                    GraphNode neighbourFromNode = GetNeighbourNodeExcept(path.FromNode, path.ToNode);
                    GraphNode neighbourToNode = GetNeighbourNodeExcept(path.ToNode, path.FromNode);

                    if (neighbourFromNode != null) {
                        GraphPath pathFromNode = GetPathToNode(path.FromNode, neighbourFromNode);
                        GraphPath pathToNode = GetPathToNode(neighbourFromNode, path.FromNode);
                        if (pathFromNode != null) {
                            pathFromNode.FromNode = optimizedNode;
                            optimizedNode.Paths.add(pathFromNode);
                            pathFromNode.SetColor("000000");
                            pathFromNode.Width = 20;
                        }
                        if (pathToNode != null) {
                            pathToNode.ToNode = optimizedNode;
                            pathToNode.SetColor("000000");
                            pathToNode.Width = 20;
                        }
                    }
                    if (neighbourToNode != null) {
                        GraphPath pathFromNode = GetPathToNode(path.ToNode, neighbourToNode);
                        GraphPath pathToNode = GetPathToNode(neighbourToNode, path.ToNode);
                        if (pathFromNode != null) {
                            pathFromNode.FromNode = optimizedNode;
                            optimizedNode.Paths.add(pathFromNode);
                            pathFromNode.SetColor("000000");
                            pathFromNode.Width = 20;
                        }
                        if (pathToNode != null) {
                            pathToNode.ToNode = optimizedNode;
                            pathToNode.SetColor("000000");
                            pathToNode.Width = 20;
                        }
                    }

                    optimizedNode.OptimizedNodes.addAll(path.FromNode.OptimizedNodes);
                    optimizedNode.OptimizedNodes.addAll(path.ToNode.OptimizedNodes);
                    optimizedNode.OptimizedNodes.add(path.FromNode);
                    optimizedNode.OptimizedNodes.add(path.ToNode);

                    OptimizedNodes.remove(path.FromNode.Id);
                    OptimizedNodes.remove(path.ToNode.Id);
                    OptimizedNodes.put(maxId, optimizedNode);

                    return true;
                }
            }
        }

        return false;
    }

    private GraphPath GetPathToNode(GraphNode neighbourNode, GraphNode toNode) {
        for(GraphPath path: neighbourNode.Paths) {
            if (path.ToNode.Id == toNode.Id)
                return path;
        }
        return null;
    }

    private GraphNode GetNeighbourNodeExcept(GraphNode node, GraphNode exceptNode) {
        for(GraphPath path: node.Paths) {
            if (path.ToNode.Id != exceptNode.Id)
                return path.ToNode;
        }
        return null;
    }

    // Принадлежат ли вершины и их соседи одному маршруту
    private boolean CheckCriteria(GraphPath path) {
        if (path.FromNode.RouteId == path.ToNode.RouteId)
        {
            if (path.FromNode.Paths.size() > 2 || path.ToNode.Paths.size() > 2)
                return false;

            int routeId = path.ToNode.RouteId;
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

    public void Copy() {
        HashMap<Integer, GraphNode> copiedNodes = new HashMap<>();
        HashSet<GraphPath> copiedPaths = new HashSet<>();

        for(Map.Entry<Integer, GraphNode> node: GraphManager.GetInstance().Nodes.entrySet()) {
            GraphNode copiedNode = new GraphNode(
                    node.getValue().Type, node.getValue().Id, node.getValue().Name,
                    node.getValue().Lat, node.getValue().Lon);
            copiedNode.RouteId = node.getValue().RouteId;
            copiedNode.NodeColor = node.getValue().NodeColor;
            copiedNodes.put(node.getKey(), copiedNode);
        }

        for(GraphPath path: GraphManager.GetInstance().Paths) {
            GraphPath copiedPath = new GraphPath(path.Type, path.FromNode.Id, path.ToNode.Id,
                    path.Time, path.Cost, copiedNodes);
            copiedPath.PathColor = path.PathColor;
            copiedPath.Width = path.Width;
            copiedPaths.add(copiedPath);
        }

        for (GraphPath path: copiedPaths)
            path.FromNode.Paths.add(path);

        OptimizedNodes = copiedNodes;
        OptimizedPaths = copiedPaths;
    }

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
//        for (GraphNode neededNodeId: optimizedNode.OptimizedNodes)
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
}
