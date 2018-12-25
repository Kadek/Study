/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package labirynt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author adas
 */
public class MSolver {
    
    ArrayList<ArrayList<Node>> grid = null;
    int width;
    int height;
    
    Thread searchThread;
    boolean searching = false;
    Labirynt labirynt;
    
    int speed = 0;

    public MSolver (Labirynt labirynt){
        this.labirynt = labirynt;
    }
    
    public boolean hasGrid(){
        return grid != null;
    }
    
    public boolean isSearching(){
        return searching;
    }
    
    public void stopSearching(){
        searching = false;
    }
    
    public ArrayList<ArrayList<Node>> getGrid(){
        return grid;
    }
   
    public void startSearch(int[] startEnd){
        searching = true;
        searchThread = new Thread(new SearchRunnable(grid, startEnd, this));
        searchThread.start();
    }
    
    public void searchEnded(Node currentNode){
        searching = false;
        while(currentNode != null){
            System.out.println(currentNode.position());
            currentNode = currentNode.predecessor;
        }
        
        labirynt.searchEnded();
    }
    
    public void updateGraphics(PriorityQueue<Node> openSet, HashSet<Node> closedSet){
        labirynt.updateGraphics(openSet, closedSet);
    }
    
    public void changeSpeed(int diff){
        System.out.println("diff: " + diff);
        speed += diff;
        if((diff > 0) && (speed - diff == 0)){
            // jeśli speed - diff == 0 to znaczy, że wątek był spausowany funkcją wait() i trzeba go odetkać
            if(searchThread != null){searchThread.interrupt();}
        }else{
            if(speed < 0){ speed = 0;}  
        }
    }
    
    public int getSpeed(){
        return speed;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Kod odpowiadajacy za ładowanie labiryntu z pliku.">
    public boolean loadData(String nazwa){
        
        File file = new File(nazwa);
        FileReader fr;
        
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException ex) {
            return false;
        }
        
        try (BufferedReader br = new BufferedReader(fr)) {
            String line;
            int lineCount = 0;
            while((line = br.readLine()) != null){
                
                // usuwa ze Stringów przerwy i znak |
                line = line.replace('|', ' ');
                String[] lineSplit = line.split(" ");
                List<String> list = new ArrayList<>(Arrays.asList(lineSplit));
                list.removeAll(Arrays.asList("", null));
                
                if(list.size() == 2){
                    generateGrid(Integer.parseInt(list.get(0)), Integer.parseInt(list.get(1)));
                }else{
                    connectNodes(list, lineCount++);
                }
            }
        } catch (IOException ex) {
            return false;
        }
        
        try {
            fr.close();
        } catch (IOException ex) {
            return false;
        }
        
        return true;
    }
    
    public void dumpData(){
        this.width = 0;
        this.height = 0;
        this.grid = null;
        this.searchThread = null;
    }
    
    private void connectNodes(List<String> lineSplit, int lineCount){
        for(int i = 0; i < width; i++){
            Node tmpNode = grid.get(lineCount).get(i);
            String cell = lineSplit.get(i);
            for(int j = 0; j < lineSplit.get(i).length(); j++){
                if(cell.charAt(cell.length() - 1 - j) == '1'){
                    if(j == 0){
                        addConnection(tmpNode, "Up");
                    }
                    if(j == 1){
                        addConnection(tmpNode, "Right");
                    }
                    if(j == 2){
                        addConnection(tmpNode, "Down");
                    }
                    if(j == 3){
                        addConnection(tmpNode, "Left");
                    }                    
                }
            }
        }
    }
    
    private void addConnection(Node node, String direction){
        if("Up".equals(direction)){
            if(node.x != 0){node.sides.put(direction, grid.get(node.x - 1).get(node.y));}
        }
        if("Right".equals(direction)){
            if(node.y != width - 1){node.sides.put(direction, grid.get(node.x).get(node.y + 1));}
        }
        if("Down".equals(direction)){
            if(node.x != height - 1){node.sides.put(direction, grid.get(node.x + 1).get(node.y));}
        }
        if("Left".equals(direction)){
            if(node.y != 0){node.sides.put(direction, grid.get(node.x).get(node.y - 1));}
        }
    }
    
    private void generateGrid(int height, int width){
        this.height = height;
        this.width = width;
        this.grid = new ArrayList<>();
        for(int i = 0; i < height; i++){
            grid.add(new ArrayList<>());
            for(int j = 0; j < width; j++){
                grid.get(i).add(new Node(i,j));
            }
        }
    }
    // </editor-fold>   
    
    private void showMaze(ArrayList<ArrayList<Node>> grid){
        for(int i = 0; i < grid.size(); i++){
            for(int j = 0; j < grid.get(i).size(); j++){
                System.out.print(grid.get(i).get(j));
            }
            System.out.print("\n");
        }
        System.out.println("--------------------------------------------");
    }
    
    // <editor-fold defaultstate="collapsed" desc="Kod wątku algorytmu A*">
    private class SearchRunnable implements Runnable {
                
        ArrayList<ArrayList<Node>> grid;
        int[] startEnd; // 0 = startX, 1 = startY, 2 = endX, 3 = endY
        MSolver solver;
        
        int speed;
        final double second = 1000.0;
        
        public SearchRunnable(ArrayList<ArrayList<Node>> grid, int[] startEnd, MSolver solver){
            this.grid = grid;
            this.startEnd = startEnd;
            this.solver = solver;
        }
        
        @Override
        public void run() {     
            
            PriorityQueue<Node> openSet = new PriorityQueue<>((Object t, Object t1) -> {
                Node node1 = (Node)t;
                Node node2 = (Node)t1;
                if ((node1.sourceCost + node1.heuristicCost) > (node2.sourceCost + node2.heuristicCost)){
                    return 1;
                }else if((node1.sourceCost + node1.heuristicCost) == (node2.sourceCost + node2.heuristicCost)){
                    return 0;
                }else{
                    return -1;
                }
            });
            HashSet<Node> closedSet = new HashSet<>();
            Node endNode = grid.get(startEnd[0]).get(startEnd[1]);
            
            openSet.add(grid.get(startEnd[2]).get(startEnd[3]));
            
            while(solver.isSearching()){
                delay();       
                updateScreen(openSet, closedSet);
                doStep(openSet, closedSet, endNode);
                
            }
        }
        
        private void doStep(PriorityQueue<Node> openSet, HashSet<Node> closedSet, Node endNode){
            Node currentNode = openSet.poll();
            //System.out.println("Opracowuję pozycję"+currentNode.position());
            if(currentNode.equals(endNode)){
                //System.out.println("Poszukiwanie zakończone w"+ currentNode.position());
                solver.searchEnded(currentNode);
                return;
            }
            closedSet.add(currentNode);
            
            expandNode(openSet, closedSet, currentNode, endNode);
        }
        
        private void expandNode(PriorityQueue<Node> openSet, HashSet<Node> closedSet, Node currentNode, Node endNode){
            Iterator it = currentNode.sides.keySet().iterator();
            while(it.hasNext()){
                String key = (String)it.next();
                Node side = currentNode.sides.get(key);
                if(side == null){ continue;}
                if(closedSet.contains(side)){ continue;}
                
                int sourceCost = currentNode.sourceCost + 1;
                
                if((openSet.contains(side)) && (side.sourceCost < sourceCost)){continue;}
                
                side.predecessor = currentNode;
                side.sourceCost = sourceCost;
                
                side.heuristicCost = getHeuristicCost(side.x, side.y, endNode.x, endNode.y);
                
                if(openSet.contains(side)){
                    openSet.remove(side);
                }
                
                //System.out.println("Dodaję do openSet " + side.position() + " o koszcie " + (side.sourceCost+side.heuristicCost));
                openSet.add(side);
            }
        }
        
        // koszt heurystyczny jest odległością euklidesową między dwoma punktami w przestrzenii labiryntu
        private double getHeuristicCost(int x1, int y1, int x2, int y2){
            return euclidianCost(x1, y1, x2, y2);
        }
        
        private double euclidianCost(int x1, int y1, int x2, int y2){
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }
        
        private double manhattanCost(int x1, int y1, int x2, int y2){
            return (x2 - x1) + (y2 - y1);
        }
        
        private void delay(){
            speed = solver.getSpeed();
            System.out.println(solver.getSpeed());
            if(speed == 0){
                try {
                    sleep(Long.MAX_VALUE);
                } catch (InterruptedException ex) {
                    System.out.println("Wątek kończy pauze.");
                }
            }else{
                try {
                    long time = (long) ((1.0/speed)*second);
                    sleep(time);
                } catch (InterruptedException ex) {
                    System.out.println("Wątek szukający ścieżki został zatrzymany.");
                }
            }
        }
        
        private void updateScreen(PriorityQueue<Node> openSet, HashSet<Node> closedSet){
            if(solver.isSearching()){
                solver.updateGraphics(openSet, closedSet);
            }
        }
        
    } 
    // </editor-fold>   
}
