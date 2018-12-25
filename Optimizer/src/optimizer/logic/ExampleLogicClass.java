/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizer.logic;

import static java.lang.Math.toIntExact;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import optimizer.datastructure.Connection;
import optimizer.datastructure.Node;
import optimizer.datastructure.Pair;

/**
 *
 * @author adas
 */
public class ExampleLogicClass {

    /*
    private static ArrayList<Node> nodesOnMap;
    private ArrayList<Connection> connectionsOnMap;
    private ArrayList<String> tasks;
    
    public ExampleLogicClass(ArrayList<Node> nodes, ArrayList<Connection> connections) {
        nodesOnMap = nodes;
        connectionsOnMap = connections;
    }
     */
    double shortestTotalDist;
    Node startNode;
    public List<ArrayList<Node>> permutations = new ArrayList<>();
    public void check(ArrayList<Node> nodes, ArrayList<Connection> connections) {
        System.out.println(nodes);
        for (Connection i : connections) {
            System.out.println(i);
        }
    } 
    public void reset(ArrayList<Node> nodes) {
        for (Node node : nodes) {
            node.minDistance = Double.POSITIVE_INFINITY;
            node.previous = null;
        }
    }

    public void computePaths(Node source) {
        source.minDistance = 0; //source.getTimeRequired();
        PriorityQueue<Node> vertexQueue = new PriorityQueue<Node>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Node u = vertexQueue.poll();
            for (Connection e : u.adjacencies) {
                if (e.isOpen()) {
                    Node v = e.getNeighbour(u);
                    double weight = e.getTimeRequired(); // + v.getTimeRequired();
                    double distanceThroughU = u.minDistance + weight;
                    if (distanceThroughU < v.minDistance) {
                        vertexQueue.remove(v);
                        v.minDistance = distanceThroughU;
                        v.previous = u;
                        vertexQueue.add(v);
                    }
                }
            }
        }
    }

    public static ArrayList<Node> getShortestPathTo(Node target) {
        ArrayList<Node> path = new ArrayList<Node>();
        for (Node vertex = target; vertex != null; vertex = vertex.previous) {
            vertex.setVisited(true);
            path.add(vertex);
        }
        Collections.reverse(path);
        return path;
    }

    public ArrayList<Node> getShortestPathFromTo(Node source, Node target) {
        computePaths(source);
        ArrayList<Node> path = getShortestPathTo(target);

        return path;
    }

    public Node getNode(ArrayList<Node> nodes, Node v) {
        int index = nodes.indexOf(v);
        Node node = nodes.get(index);
        return node;
    }

    public void resetVisited(ArrayList<Node> nodes) {
        for (Node n : nodes) {
            n.setVisited(false);
        }
    }

    public void dfs_rec(ArrayList<Node> nodes, Node v, ArrayList<Node> finalPath) {
        Node n = getNode(nodes, v);
        n.setVisited(true);
        n.setColor(0);
        Node checkpoint = null;
        //System.out.print(v.getName() + " ");
        ArrayList<Connection> connectionsOfV = v.adjacencies_tasked;
        for (Connection e : connectionsOfV) {
            Node w = e.getNeighbour(v);
            if (!w.isVisited()) {
                ArrayList<Node> path_tmp = new ArrayList<Node>();
                path_tmp = getShortestPathFromTo(v, w);
                for (Node x : path_tmp) {
                    finalPath.add(x);
                }
                printPath(finalPath);
                System.out.println(v);
                System.out.println(w);
                reset(nodes);
                dfs_rec(nodes, w, finalPath);
            }
        }
        n.setColor(1);
        //finalPath.add(v);
    }

    public ArrayList<Node> dfs(ArrayList<Node> nodes, Node start) {
        resetVisited(nodes);
        ArrayList<Node> finalPath = new ArrayList<Node>();
        dfs_rec(nodes, start, finalPath);
        return finalPath;
    }

    public void printPath(List<Node> path) {
        for (Node x : path) {
            System.out.print(x.getName() + " - ");
        }
        System.out.println();
    }

    public Pair<ArrayList<Node>, ArrayList<Connection>> getPath(ArrayList<Node> nodesOnMap, ArrayList<Connection> connectionsOnMap, ArrayList<String> tasks, Node activeNode, int mode) {
        this.startNode = nodesOnMap.get(0);
        ArrayList<Node> taskNodes = new ArrayList<Node>();
        ArrayList<Connection> taskConnections = new ArrayList<Connection>();
        //System.out.println(tasks);
        for (Node node : nodesOnMap) {
            for (String searched : tasks) {
                if (searched.equals(node.getName())) {
                    taskNodes.add(node);
                }
            }
        }
        //System.out.println("Ilosc taskow: " + taskNodes.size());

        // Liczona najkrótszej drogi pomiędzy kązdym taskiem
        for (int i = 0; i < taskNodes.size() - 1; i++) {
            for (int j = i + 1; j < taskNodes.size(); j++) {
                Node source = taskNodes.get(i);
                Node target = taskNodes.get(j);
                ArrayList<Node> path = getShortestPathFromTo(source, target);
                //printPath(path);
                //System.out.println("Distance: " + target.minDistance);

                Connection connectionBetweenTasks = new Connection(
                        source.getName() + " " + target.getName(),
                        (long) target.minDistance,
                        (long) 0,
                        (long) 0,
                        true,
                        source,
                        target
                );
                taskConnections.add(connectionBetweenTasks);
                reset(nodesOnMap);
            }
        }

        //Tworzenie nowego grafu na podstawie najkrotszych drog do taskow i DFS po nich
        //System.out.println(taskNodes);
        //System.out.println(taskConnections);
        for (Connection e : taskConnections) {
            e.getAttach1().adjacencies_tasked.add(e);
            e.getAttach2().adjacencies_tasked.add(e);
        }
        //ArrayList<Node> finalpath = dfs(taskNodes, taskNodes.get(0));
        //printPath(finalpath);
        ArrayList<Node> permutation = new ArrayList<Node>();
        permutation.add(activeNode);
        _permute(permutation, taskNodes);
        for (ArrayList<Node> l : permutations) {
            l.add(startNode);
            //printPath(l);
        }

        //System.out.println(permutations);
        ArrayList<Node> finalList = findShortest(nodesOnMap);
        Pair<ArrayList<Node>, ArrayList<Connection>> pair = new Pair<>();
        pair.setFirst(finalList);
        pair.setSecond(connectionsOnMap);
        return pair;
    }

    public ArrayList<Node> findShortest(ArrayList<Node> nodesOnMap) {
        double shortestDist = Double.POSITIVE_INFINITY;
        ArrayList<Node> finalList = new ArrayList<Node>();
        ArrayList<Node> bestTasks = new ArrayList<Node>();
        ArrayList<Node> tmp = new ArrayList<Node>();
        for (ArrayList<Node> list : permutations) {
            //System.out.println(list);
            double sum = 0;
            for (int i = 1; i < list.size(); i++) {
                Node source = list.get(i - 1);
                Node target = list.get(i);
                source = getNode(nodesOnMap, source);
                target = getNode(nodesOnMap, target);
                tmp = getShortestPathFromTo(source, target);
                sum += target.minDistance;
                reset(nodesOnMap);
            }
            //System.out.println(sum);
            if (sum < shortestDist) {
                shortestDist = sum;
                bestTasks = list;
            }
        }
        for (int i = 1; i < bestTasks.size(); i++) {
            Node source = bestTasks.get(i - 1);
            Node target = bestTasks.get(i);
            tmp = getShortestPathFromTo(source, target);
            for (int j = 1; j < tmp.size(); j++) {
                finalList.add(tmp.get(j));
            }
            //printPath(tmp);
            reset(nodesOnMap);
        }
        //System.out.println(shortestDist);
        shortestTotalDist = shortestDist;
        return finalList;
    }
    public double shortestDistane(){
        return shortestTotalDist;
    }
    private void _permute(ArrayList<Node> permutation, List<Node> nodes) {
        if (nodes.size() <= 0) {
            permutations.add(permutation);
            return;
        }

        for (Node datum : nodes) {
            List<Node> remnants = new ArrayList<Node>(nodes);
            remnants.remove(datum);
            ArrayList<Node> elements = new ArrayList<Node>(permutation);
            elements.add(datum);
            _permute(elements, remnants);
        }
    }

    public Pair<ArrayList<Node>, ArrayList<Connection>> randomizeMap(ArrayList<Node> nodesOnMap, ArrayList<Connection> connectionsOnMap) {
        Random r = new Random();
        for (Connection e : connectionsOnMap) {
            int randOpen = r.nextInt(100);
            if (randOpen < 40) {
                e.setOpen(true);
            }
            int randOpenChange = r.nextInt(100);
            if (randOpenChange < 10) {
                e.setOpen(!e.isOpen());
            }
            
            int randChange = r.nextInt(100);
            if (randChange < 10) {             
                if( randChange % 2 == 0){
                    if(e.getTimeRequired() - toIntExact(randChange) > 3){                      
                        e.setTimeRequired(e.getTimeRequired() - toIntExact(randChange));
                    }
                    else{
                         e.setTimeRequired(e.getTimeRequired() + toIntExact(randChange));
                    }
                }
                else{
                     e.setTimeRequired(e.getTimeRequired() + toIntExact(randChange));
                }
            }          
        }
        Pair<ArrayList<Node>, ArrayList<Connection>> pair = new Pair<>();
        pair.setFirst(nodesOnMap);
        pair.setSecond(connectionsOnMap);
        return pair;
    }

}
