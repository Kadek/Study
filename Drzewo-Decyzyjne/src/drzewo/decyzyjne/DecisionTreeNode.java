/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drzewo.decyzyjne;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author adas
 */
public class DecisionTreeNode {
    private final double entropy;
    private final String column;
    private final String value;
    private String columnWithHighestEntropy;
    private DecisionTreeNode predecessor = null;
    private final ArrayList<DecisionTreeNode> children;
    private final HashMap<String, Double> options;
    
    //wykorzystywane przy wczytywanie drzewa z pliku
    public int nTabs = 0;
    
    public DecisionTreeNode(double entropy, String column, String value){
        this.entropy = entropy;
        this.column = column;
        this.value = value;
        this.children = new ArrayList<>();
        this.options = new HashMap<>();
    }
    
    public void addOption(String key, Double value){
        options.put(key, value);
    }
    
    public void addOptionsFromData(ArrayList<Integer> data, ArrayList<String> uniqueValues){
        for(int i = 0 ; i < uniqueValues.size(); i++){
            options.put(uniqueValues.get(i), ((double)data.get(i+1))/data.get(0));
        }
    }
    
    public HashMap<String, Double> getOptions(){
        return options;
    }
    
    public void showOptions(){
        System.out.println("--------------------------------");
        options.keySet().forEach((key) -> {
            System.out.println(key + " : " + options.get(key));
        });
        System.out.println("--------------------------------");
    }
    
    public void setPredecessor(DecisionTreeNode predecessor){
        this.predecessor = predecessor;
    }
    public DecisionTreeNode getPredecessor(){
        return predecessor;
    }
    
     public void setColumnWithHighestEntropy(String column){
        this.columnWithHighestEntropy = column;
    }
    public String getColumnWithHighestEntropy(){
        return columnWithHighestEntropy;
    }  
    
    public void addChild(DecisionTreeNode child){
        children.add(child);
    }
    public ArrayList<DecisionTreeNode> getChildren(){
        return children;
    }
    
    public String getValue(){
        return value;
    }
    public String getColumn(){
        return column;
    }
    
    @Override
    public String toString(){
        return column+":"+value+" = "+Double.toString(entropy)+" decision="+options.toString();
    }
}
