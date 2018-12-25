/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import optimizer.datastructure.Connection;
import optimizer.datastructure.Node;
import optimizer.datastructure.Pair;
import optimizer.gui.View;
import optimizer.logic.ExampleLogicClass;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author adas
 */
public class Optimizer extends Application {

    private View gui = null;
    private ExampleLogicClass logic;
    private ArrayList<Node> nodesOnMap = new ArrayList<>();
    private ArrayList<Connection> connectionsOnMap = new ArrayList<>();
    private ArrayList<String> tasks = new ArrayList<>();
    private Node activeNode;
    
    public Optimizer() {
        
        //logic = new ExampleLogicClass();
        
    }
    
    public static void main(String[] args) {
                
        launch();
      
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Systemy");
        primaryStage.setScene(new Scene(root, 1023, 828));
        primaryStage.show();
    }

    public void initialize(View gui){
        
        this.gui = gui;
        gui.showPossibleNodes(readPossibleNodes());
        gui.showPossibleConnections(readPossibleConnections());
        gui.showPossibleTasks(getPossibleTasks(readPossibleNodes()));
        
    }
    
    public ArrayList<Node> addNewNode(Node node){
        nodesOnMap.add(node);
        //System.out.println(nodesOnMap);
        return nodesOnMap;
    }
    public ArrayList<Node> removeNode(Node node){
        nodesOnMap.remove(node);
        return nodesOnMap;
    }
    
    public ArrayList<Connection> addNewConnection(Connection connection){
        connectionsOnMap.add(connection);
        return connectionsOnMap;
    }
    
    public ArrayList<Connection> removeConnection(Connection connection){
        connectionsOnMap.remove(connection);
        return connectionsOnMap;
    }
    
    public Pair<ArrayList<Node>, ArrayList<Connection>> loadMap(String filename){
        Iterator<JSONObject> nodesIterator = getJSONIterator(filename, "nodes");
        Iterator<JSONObject> connectionsIterator = getJSONIterator(filename, "connections");
        
        ArrayList<Node> nodes = new ArrayList<>();
        Node node;
        while(nodesIterator.hasNext()){
            JSONObject object = nodesIterator.next();
            node = new Node(
                (String)object.get("name"),
                (Long)object.get("time_required"),
                (Long)object.get("time_open"),
                (Long)object.get("time_close"),
                (Long)object.get("x"),
                (Long)object.get("y"),
                (Boolean)object.get("open")
            );
            nodes.add(node);
        }
        
        ArrayList<Connection> connections = new ArrayList<>();
        Connection connection;
        
        while(connectionsIterator.hasNext()){
            JSONObject object = connectionsIterator.next();
            
            Node attach1 = findNodeInNodes(nodes, (Long)object.get("attach1"));
            Node attach2 = findNodeInNodes(nodes, (Long)object.get("attach2"));            
            
            connection = new Connection(
                (String)object.get("name"),
                (Long)object.get("time_required"),
                (Long)object.get("time_open"),
                (Long)object.get("time_close"),
                (Boolean)object.get("open"),
                attach1,
                attach2
            );
            
            connections.add(connection);
            attach1.adjacencies.add(connection);
            attach2.adjacencies.add(connection);
        }
                
        this.nodesOnMap = nodes;
        this.connectionsOnMap = connections;
        
        Pair<ArrayList<Node>, ArrayList<Connection>> readyPair = new Pair<>();
        readyPair.setFirst(nodes);
        readyPair.setSecond(connections);
        return readyPair;
    }
        
    private Node findNodeInNodes(ArrayList<Node> nodes, Long hash){
        for(Node node : nodes){
            if(node.hashCode() == hash){
                return node;
            }
        }
        System.out.println("Couldn't find node when loading");
        return null;
    }
    
    public boolean saveMap(){
        
        String filename = "map.json";
        
        JSONArray JSONNodes = new JSONArray();
        this.nodesOnMap.forEach((node) -> {
            JSONObject JSONNode = new JSONObject();
            JSONNode.put("name", node.getName());
            JSONNode.put("time_required", node.getTimeRequired());
            JSONNode.put("time_open", node.getTimeOpen());
            JSONNode.put("time_close", node.getTimeClose());
            JSONNode.put("x", node.getX());
            JSONNode.put("y", node.getY());
            JSONNode.put("open", node.isOpen());
            JSONNodes.add(JSONNode);
        });
  
        JSONArray JSONConnections = new JSONArray();
        this.connectionsOnMap.forEach((connection) -> {
            JSONObject JSONConnection = new JSONObject();
            JSONConnection.put("name", connection.getName());
            JSONConnection.put("time_required", connection.getTimeRequired());
            JSONConnection.put("time_open", connection.getTimeOpen());
            JSONConnection.put("time_close", connection.getTimeClose());
            JSONConnection.put("open", connection.isOpen());
            JSONConnection.put("attach1", connection.getAttach1().hashCode());
            JSONConnection.put("attach2", connection.getAttach2().hashCode());
            JSONConnections.add(JSONConnection);
        });
        JSONObject finalObject = new JSONObject();
        finalObject.put("nodes", JSONNodes);
        finalObject.put("connections", JSONConnections);

        String path = Paths.get(".")
                .toAbsolutePath()
                .normalize()
                .toString() + "\\data\\" + filename + ".txt";
        try (FileWriter file = new FileWriter(path)) {

            file.write(finalObject.toJSONString());
            file.flush();

        } catch (IOException e) {
            return false;
        }
        
        return true;
    }
    
    public ArrayList<String> addTask(String task){
        tasks.add(task);
        
        if(activeNode != null){
            updatePath();
        }
        
        return tasks;
    }
    
    public ArrayList<String> removeTask(String task){
        tasks.remove(task);
        return tasks;
    }
    
    public boolean setActivePoint(Node node){
        activeNode = null;
        int index = nodesOnMap.indexOf(node);
        activeNode = nodesOnMap.get(index);
        
        if(activeNode == null)
                return false;
        
        gui.dehighlightAllNodes();
        gui.highlightNode(activeNode);
        
        updatePath();        
        
        return true;
    }
    
    public boolean doStep(){
        if(activeNode == null || tasks.isEmpty())
            return false;
        
        gui.removeTask(tasks.get(0));
        
        Pair<ArrayList<Node>, ArrayList<Connection>> map;
        map = logic.randomizeMap(nodesOnMap, connectionsOnMap);
        nodesOnMap = map.getFirst();
        connectionsOnMap = map.getSecond();
        
        return setActivePoint(nodesOnMap.get(0));
    }

    public ArrayList<Node> readPossibleNodes() {

        ArrayList<Node> nodes = new ArrayList<>();
        Iterator<JSONObject> iterator = getJSONIterator("elements", "nodes");
        Node node;
        while(iterator.hasNext()){
            JSONObject object = iterator.next();
            node = new Node(
                (String)object.get("name"),
                (Long)object.get("time_required"),
                (Long)object.get("time_open"),
                (Long)object.get("time_close"),
                (long)-1,
                (long)-1,
                false
            );
            nodes.add(node);
        }
        return nodes;
    }

    private ArrayList<Connection> readPossibleConnections() {

        ArrayList<Connection> connections = new ArrayList<>();
        Iterator<JSONObject> iterator = getJSONIterator("elements", "connections");
        Connection connection;
        while(iterator.hasNext()){
            JSONObject object = iterator.next();
            connection = new Connection(
                (String)object.get("name"),
                (Long)object.get("time_required"),
                (Long)object.get("time_open"),
                (Long)object.get("time_close"),
                false,
                null,
                null
            );
            connections.add(connection);
        }
        return connections;
    }

    private static Iterator<JSONObject> getJSONIterator(String filename, String element){
               
        JSONParser parser = new JSONParser();
        Iterator<JSONObject> iterator = null;
        try {

            String path = Paths.get(".")
                    .toAbsolutePath()
                    .normalize()
                    .toString() + "\\data\\" + filename + ".txt";
            
            Object obj = parser.parse(new FileReader(path));

            JSONObject jsonObject = (JSONObject) obj;

            // loop array
            JSONArray msg = (JSONArray) jsonObject.get(element);
            iterator = msg.iterator();

        } catch (FileNotFoundException e) {
        } catch (IOException | ParseException e) {
        }
        
        return iterator;
    }
    
    private ArrayList<String> getPossibleTasks(ArrayList<Node> possibleNodes) {
        
        HashSet<String> tasksSet = new HashSet<>();
        
        for(Node node : possibleNodes){
            if(!tasksSet.contains(node.getName()))
                tasksSet.add(node.getName());
        }
        
        return new ArrayList<>(tasksSet);
        
    }
    
    private void updatePath(){
        Pair<ArrayList<Node>, ArrayList<Connection>> path;
        path = logic.getPath(
                nodesOnMap, 
                connectionsOnMap, 
                tasks, 
                activeNode,
                0);//Te zero tu siedzi bo jeszcze tryby szukania ścieżek trzeba wymyślić
        
        //gui.showPath(path.getFirst(), path.getSecond());
    }
    
    public ArrayList<Node> getNodesOnMap(){
        return nodesOnMap;
    }
    public ArrayList<Connection> getConnectionsOnMap(){
        return connectionsOnMap;
    }
    public ArrayList<String> getTasks(){
        return tasks;
    }
    public void reset(){
        nodesOnMap = new ArrayList<>();
        connectionsOnMap = new ArrayList<>();
    }
}
