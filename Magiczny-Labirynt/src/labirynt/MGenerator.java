/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package labirynt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author adas
 */
public class MGenerator {
    int width;
    int height;
    String nazwa;
    final int nSides = 4;
    
    public boolean validateData(String x, String y, String nazwa){
        try{
            int xNumber = Integer.parseInt(x);
            int yNumber = Integer.parseInt(y);
            if((xNumber <= 0) || (yNumber <= 0) || (nazwa.equals(""))){
                return false;
            }
            
            this.height = xNumber;
            this.width = yNumber;
            this.nazwa = nazwa;
        }catch(Exception e){
            return false;
        }
                
        return true;
    }
    
    public boolean generate(){
        ArrayList<ArrayList<HashSet<Node>>> gridSets = new ArrayList<>();
        ArrayList<ArrayList<Node>> gridNodes = new ArrayList<>();
        setGridNodes(gridNodes);
        setGridSets(gridSets, gridNodes);
                
        Random rand = new Random();
        int i = 0;
        while(gridSets.get(0).get(0).size() != width*height){
            int x = rand.nextInt(height);
            int y = rand.nextInt(width);
            
            HashSet<Node> nodesA = gridSets.get(x).get(y);
            Node nodeA = gridNodes.get(x).get(y);
            Node nodeB = getSideNode(nodeA, gridNodes, nodesA);
            if(nodeB == null){
                continue;
            }
            
            joinNodes(nodeA, nodeB, gridSets);
        }
        saveMaze(gridNodes);
        
        return true;
    }
    
    private void joinNodes(Node nodeA, Node nodeB, ArrayList<ArrayList<HashSet<Node>>> gridSet){
        
        //join two nodes
        if(nodeA.x - 1 == nodeB.x){
            nodeA.sides.put("Up", nodeB);
            nodeB.sides.put("Down",nodeA);
        }else if(nodeA.x + 1 == nodeB.x){
            nodeA.sides.put("Down", nodeB);
            nodeB.sides.put("Up",nodeA);
        }else if(nodeA.y + 1  == nodeB.y){
            nodeA.sides.put("Right", nodeB);
            nodeB.sides.put("Left",nodeA);
        }else if(nodeA.y - 1 == nodeB.y){
            nodeA.sides.put("Left", nodeB);
            nodeB.sides.put("Right",nodeA);
        }
        
        
        //merge two sets
        HashSet<Node> nodesA = gridSet.get(nodeA.x).get(nodeA.y);
        HashSet<Node> nodesB = gridSet.get(nodeB.x).get(nodeB.y);
        
        Iterator it = nodesB.iterator();
        while(it.hasNext()){
            Node tmpNode = (Node)it.next();
            nodesA.add(tmpNode);
            gridSet.get(tmpNode.x).set(tmpNode.y, nodesA);
        }
        
    }
    
    private Node getSideNode(Node node, ArrayList<ArrayList<Node>> grid, HashSet<Node> nodes){
        Random rand = new Random();
        int side = rand.nextInt(4);
        int i = 0;
        
        do{
            Node nodeB = null;
            
            if((side == 0) && (node.x != 0)){
                nodeB = grid.get(node.x - 1).get(node.y);
            }else if((side == 1) && (node.y != width-1)){
                nodeB = grid.get(node.x).get(node.y + 1);
            }else if((side == 2) && (node.x != height - 1)){
                nodeB = grid.get(node.x + 1).get(node.y);
            }else if((side == 3) && (node.y != 0)){
                nodeB = grid.get(node.x).get(node.y - 1);
            }
            
            if(nodeB != null){
                if(!nodes.contains(nodeB)){return nodeB;}               
            }
            
            side = ++side % 4;
            i++;
        }while(i < 4);
        
        return null;
    }
    
    private void setGridSets(ArrayList<ArrayList<HashSet<Node>>> grid, ArrayList<ArrayList<Node>> gridNodes){
        for(int i = 0; i < height; i++){
            grid.add(new ArrayList<>());
            for(int j = 0; j < width; j++){
                grid.get(i).add(new HashSet<>());
                grid.get(i).get(j).add(gridNodes.get(i).get(j));
            }
        }
    }    
    
    private void setGridNodes(ArrayList<ArrayList<Node>> grid){
        for(int i = 0; i < height; i++){
            grid.add(new ArrayList<>());
            for(int j = 0; j < width; j++){
                grid.get(i).add(new Node(i, j));
            }
        }
    }
    
    private void showMaze(ArrayList<ArrayList<Node>> grid){
        for(int i = 0; i < grid.size(); i++){
            for(int j = 0; j < grid.get(i).size(); j++){
                System.out.print(grid.get(i).get(j));
            }
            System.out.print("\n");
        }
        System.out.println("--------------------------------------------");
    }
    
    private void showSets(ArrayList<ArrayList<HashSet<Node>>> grid){
        HashMap<HashSet<Node>, Integer> sets = new HashMap<>();
        
        int count = 1;
        for(int i = 0; i < grid.size(); i++){
            for(int j = 0; j < grid.get(i).size(); j++){
                if(!sets.containsKey(grid.get(i).get(j))){
                    sets.put(grid.get(i).get(j), count++);
                }
                System.out.print(sets.get(grid.get(i).get(j)));
            }
            System.out.print("\n");
        }
    }
    
    private void saveMaze(ArrayList<ArrayList<Node>> grid){
        try{
            PrintWriter writer = new PrintWriter(nazwa+".txt", "UTF-8");
            writer.println(Integer.toString(height) + " " + Integer.toString(width));
            for(int i = 0; i < grid.size(); i++){
                for(int j = 0; j < grid.get(i).size(); j++){
                    writer.print(grid.get(i).get(j).toString());
                }
                writer.print("\n");
            }
            writer.close();
        } catch (IOException e) {
           // do something
        }   
    }
}
