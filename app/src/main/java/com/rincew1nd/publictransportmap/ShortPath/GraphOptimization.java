package com.rincew1nd.publictransportmap.ShortPath;

import android.util.Log;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GraphOptimization {

    private static GraphOptimization _instance;
    public static GraphOptimization GetInstance() {
        if (_instance == null)
            _instance = new GraphOptimization();
        return _instance;
    }

    public HashMap<Integer, GraphNode> Nodes;
    public HashSet<GraphPath> Paths;

    public void OptimizeGraph(int maxId) {
        Copy();
        while (OptimizeCycle(maxId))
            maxId++;
    }

    // Цикл оптимизации
    private boolean OptimizeCycle(int maxId) {
        for (GraphNode node: Nodes.values())
        {
            if (node.Paths.size() > 2 || !node.IsOptimizable)
                continue;

            for (GraphPath path: node.Paths) {
                if (CheckCriteria(path))
                {
                    maxId++;

                    if (Paths.contains(path))  //TRUE
                        Log.d("","");
                    if (Paths.remove(path))    //FALSE
                        Log.d("","");
                    Paths.remove(GetPathToNode(path.ToNode, path.FromNode));

                    GraphNode optimizedNode = new GraphNode(node.Type, maxId, "Optimized", node.Lat, node.Lon);
                    optimizedNode.RouteId = path.ToNode.RouteId;
                    optimizedNode.SetColor(optimizedNode.Type, "000000");
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

                    Nodes.remove(path.FromNode.Id);
                    Nodes.remove(path.ToNode.Id);
                    Nodes.put(maxId, optimizedNode);

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

            if (!path.ToNode.IsOptimizable)
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

    private void Copy() {
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

        Nodes = copiedNodes;
        Paths = copiedPaths;
    }

    public void RecoverOptimizedNodes(int fromNode, int toNode) {
        Nodes.clear();
        Paths.clear();

        Copy();
        for (GraphNode nodes: Nodes.values())
            nodes.IsOptimizable = true;
        Nodes.get(fromNode).IsOptimizable = false;
        Nodes.get(toNode).IsOptimizable = false;

        OptimizeCycle(GraphManager.GetInstance().NextNodeId(GraphManager.GetInstance().Nodes));
    }

    // Востановить оптимизированную ноду
    //public void RecoverOptimizedNodes(int fromNode, int toNode) {
    //    boolean fromNodeExist = false;
    //    boolean toNodeExist = false;
    //    for (GraphNode node : Nodes.values()) {
    //        if (node.Id == fromNode)
    //            fromNodeExist = true;
    //        if (node.Id == toNode)
    //            toNodeExist = true;
    //    }
    //    try {
    //        if (!fromNodeExist)
    //            SplitOptimizedNodes(fromNode);
    //        if (!toNodeExist)
    //            SplitOptimizedNodes(toNode);
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
//
    //    for (GraphNode nodes: Nodes.values())
    //        nodes.IsOptimizable = true;
    //    Nodes.get(fromNode).IsOptimizable = false;
    //    Nodes.get(toNode).IsOptimizable = false;
//
    //    OptimizeCycle(GraphManager.GetInstance().NextNodeId(Nodes));
    //}
//
    //// Разбить оптимизированную ноду
    //private void SplitOptimizedNodes(int nodeId) throws Exception {
//
    //    // Получить оптимизированную ноду в которой находится искомая нода
    //    GraphNode optimizedNode = null;
    //    for (GraphNode node: Nodes.values())
    //        for (GraphNode node1 : node.OptimizedNodes)
    //            if (node1.Id == nodeId)
    //                optimizedNode = node;
//
    //    // Если нода не была найдена - упасть
    //    if (optimizedNode == null)
    //        throw new Exception("Station not found");
//
    //    // Удалить оптимизированную ноду из графа
    //    Nodes.remove(optimizedNode.Id);
    //    // Добавить все ноды оптимизированной ноды в граф
    //    for (GraphNode origNode: optimizedNode.OptimizedNodes)
    //        if (origNode.OptimizedNodes.size() == 0)
    //            Nodes.put(origNode.Id, origNode);
//
    //    // Востановить оригинальные пути
    //    for (GraphPath path: optimizedNode.Paths) {
    //        RestoreOriginalNodePaths(path.ToNode);
    //    }
    //    for (GraphNode node : optimizedNode.OptimizedNodes) {
    //        RestoreOriginalNodePaths(node);
    //    }
    //}
//
    //private void RestoreOriginalNodePaths(GraphNode node) {
    //    GraphManager gm = GraphManager.GetInstance();
//
    //    node.OptimizedNodes.clear();
    //    node.Paths.clear();
//
    //    for (GraphPath path : gm.Nodes.get(node.Id).Paths) {
    //        GraphPath copiedPath = new GraphPath(path.Type, path.FromNode.Id, path.ToNode.Id,
    //                path.Time, path.Cost, Nodes);
    //        copiedPath.PathColor = path.PathColor;
    //        copiedPath.Width = path.Width;
    //        node.Paths.add(copiedPath);
    //    }
    //}
}
