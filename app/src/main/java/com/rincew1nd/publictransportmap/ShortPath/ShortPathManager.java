package com.rincew1nd.publictransportmap.ShortPath;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Graph.GraphNode;
import com.rincew1nd.publictransportmap.Models.Graph.GraphPath;
import com.rincew1nd.publictransportmap.Models.Settings;

import java.util.ArrayList;
import java.util.Collections;

public class ShortPathManager {

    private static ShortPathManager _instance;

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

    public ArrayList<ShortestPathObj> FindShortestPaths() {
        _algorithmResult = new ArrayList<>();

        //RecoverOptimizedNodes(algorithmReadyGraph, fromNodeId, toNodeId);

        GraphNode _fromNode = GraphManager.GetInstance().Nodes.get(Settings.FromStationId);
        _toNode = GraphManager.GetInstance().Nodes.get(Settings.ToStationId);
        ArrayList<Integer> path = new ArrayList<>();

        path.add(_fromNode.Id);
        // Создаём вес (время, пересадки, стоимость)
        int[] weight = new int[] {0, 0, 0};

        DepthSearch(path, weight, _fromNode, null, true);
        Collections.sort(_algorithmResult);
        return _algorithmResult;
    }

    private void DepthSearch(ArrayList<Integer> path, int[] weight, GraphNode lastNode,
                             GraphPath lastPath, boolean addDelay) {
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
            //TODO ограничить пересадки электричек:
            // добавить инкремент пересадки
            // не пересаживаться на ушедшие поезда
            //if (lastPath != null && lastPath.Delay != 0 && gPath.Delay != 0 && lastPath.Delay > gPath.Delay)
            //    continue;

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
            DepthSearch(newPath, newWeight, gPath.ToNode, gPath, addDelayCopy);
        }
    }

    private boolean ContainsTransferToVisitedNode(GraphNode gNode, ArrayList<Integer> path) {
        if (path.size() > 2)
            for (GraphPath gPath: gNode.Paths) {
                if (gPath.ToNode.Id != path.get(path.size()-2))
                    if(path.contains(gPath.ToNode.Id))
                        return true;
            }
        return false;
    }
}

