/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drzewo.decyzyjne;

import java.util.ArrayList;

/**
 *
 * @author adas
 */
public class AlgorytmDrzewa {
    private final InputInterface inputSystem;
    
    public AlgorytmDrzewa(InputInterface inputSystem){
        this.inputSystem = inputSystem;
    }
    
    public DecisionTreeNode train(String fileName){
        if(!inputSystem.load(fileName)){ return null; }
            
        // tworzy zbiór wszystkich kolumn
        ArrayList<String> columnsPool = new ArrayList<>(inputSystem.getColumns());
        columnsPool.remove(columnsPool.size()-1);
        // sprawdza czy jest co badać
        double entropy = getEntropy(inputSystem.getTargetColumnData(null));

        DecisionTreeNode rootDecisionTree = new DecisionTreeNode(entropy, "root", "root");
        if(entropy == 0.0){return rootDecisionTree;}
        //szuka kolumny od której zaczniemy badanie
        String column = getColumnWithHighestEntropyGain(columnsPool, null, entropy);
        rootDecisionTree.setColumnWithHighestEntropy(column);
        
        
        do{
            // szuka niestworzonego Node'a najbardziej na lewo drzewa
            ArrayList<ArrayList<String>> unprocessedChild = findUnprocessedChild(rootDecisionTree);
            System.out.println(unprocessedChild);
            if(unprocessedChild == null){ break;}

            // sprawdza entropie dla tego Node'a
            entropy = getEntropy(inputSystem.getTargetColumnData(unprocessedChild));
            DecisionTreeNode newDecisionTreeNode = new DecisionTreeNode(entropy, unprocessedChild.get(unprocessedChild.size()-1).get(0), 
                                                                                 unprocessedChild.get(unprocessedChild.size()-1).get(1));
            newDecisionTreeNode.addOptionsFromData(inputSystem.getTargetColumnData(unprocessedChild), inputSystem.getTargetUniqueValues());
            // jeśli entropia dla Node'a = 0 to mamy czysty podzbiór
            if(entropy == 0.0){
                appendTreeNode(rootDecisionTree, newDecisionTreeNode, unprocessedChild);
                continue;
            }
            
            column = getColumnWithHighestEntropyGain(currentColumnsPool(columnsPool, unprocessedChild), unprocessedChild, entropy);
            newDecisionTreeNode.setColumnWithHighestEntropy(column);
            appendTreeNode(rootDecisionTree, newDecisionTreeNode, unprocessedChild);
            
        }while(1 == 1);
        
        return rootDecisionTree;
    }
    
    private ArrayList<String> currentColumnsPool(ArrayList<String> columnsPool, ArrayList<ArrayList<String>> path){
        ArrayList<String> newPool = new ArrayList<>(columnsPool);
        
        for(int i = 0; i < path.size(); i++){
            newPool.remove(path.get(i).get(0));
        }
        
        return newPool;
    }
    
    // <editor-fold defaultstate="collapsed" desc="operacje na drzewie">
    private void appendTreeNode(DecisionTreeNode root, DecisionTreeNode newNode, ArrayList<ArrayList<String>> path){
        for(int i = 0 ; i < path.size() - 1; i++){
            for(int j = 0; j < root.getChildren().size(); j++){
                if(root.getChildren().get(j).getColumn() == path.get(i).get(0) &&
                   root.getChildren().get(j).getValue() == path.get(i).get(1)){
                    root = root.getChildren().get(j);
                    break;
                }
            }
        }
        
        root.addChild(newNode);
    }
    
    private ArrayList<ArrayList<String>>  findUnprocessedChild(DecisionTreeNode rootDecisionTreeNode){
        
        // szukaj rekurencyjnie po isniejących dzieciach
        for(int i = 0; i < rootDecisionTreeNode.getChildren().size(); i++){
            ArrayList<ArrayList<String>> unprocessedChild = findUnprocessedChild(rootDecisionTreeNode.getChildren().get(i));
            if(unprocessedChild != null){
                if("root".equals(rootDecisionTreeNode.getColumn())){
                    return unprocessedChild;
                }
                unprocessedChild.add(0, new ArrayList<>());
                unprocessedChild.get(0).add(rootDecisionTreeNode.getColumn());
                unprocessedChild.get(0).add(rootDecisionTreeNode.getValue());
                return unprocessedChild;
            }
        }
        
        // brak kolumny z najwyższą entropią oznacza że podzbiór jest czysty
        if(rootDecisionTreeNode.getColumnWithHighestEntropy() == null){ return null;}
        
        // jeśli wszystkie dzieci zostały przebadane to zajmij się pierwszą wolną wartością
        ArrayList<String> valueSpace = inputSystem.getUniqueValues(rootDecisionTreeNode.getColumnWithHighestEntropy());
        int sizeOfChildren = rootDecisionTreeNode.getChildren() != null ? rootDecisionTreeNode.getChildren().size() : 0;
        if(sizeOfChildren < valueSpace.size()){
            ArrayList<ArrayList<String>> tmp = new ArrayList<>();
            tmp.add(new ArrayList<>());
            tmp.get(0).add(rootDecisionTreeNode.getColumnWithHighestEntropy());
            tmp.get(0).add(valueSpace.get(sizeOfChildren));
            
            
            if("root".equals(rootDecisionTreeNode.getColumn())){
                return tmp;
            }
            tmp.add(0, new ArrayList<>());
            tmp.get(0).add(rootDecisionTreeNode.getColumn());
            tmp.get(0).add(rootDecisionTreeNode.getValue());
            return tmp;
        }
        
        // wszystko zostałe przebadane
        return null;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="wyliczanie entropii">
    public double getEntropy(ArrayList<Integer> data){
        double entropy = 0;
        for(int i = 1; i < data.size(); i++){
            double logp = 0.0;
            double p = ((double)data.get(i))/data.get(0);
            if(p != 0.0){
                logp = Math.log(p)/Math.log(2);
            }
            entropy += -1*p*logp;
        }
        return entropy;
    }
    
    public double getTwoVariableEntropy(ArrayList<ArrayList<Integer>> data){
        
        int sumOfAllOfRows = 0;
        for(int i = 0 ; i < data.size(); i++){
            if(data.get(i).isEmpty()){continue;}
            sumOfAllOfRows += data.get(i).get(0);
        }
        
        double entropy = 0;
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).isEmpty()){continue;}
            entropy += ((double)data.get(i).get(0)/sumOfAllOfRows)*getEntropy(data.get(i)); 
        }
        
        return entropy;
    }
    
    public String getColumnWithHighestEntropyGain(ArrayList<String> columnsPool, ArrayList<ArrayList<String>> rules, double entropy){
        double entropyGain = 0;
        int index = -1;
        for(int i = 0 ; i < columnsPool.size(); i++){
            ArrayList<ArrayList<Integer>> data = inputSystem.getSpecificColumnData(columnsPool.get(i), rules);
            double tmpEntropy = getTwoVariableEntropy(data);
            if(entropy - tmpEntropy > entropyGain){
                entropyGain = entropy - tmpEntropy;
                index = i;
            }
        }
        
        return columnsPool.get(index);
    }
    // </editor-fold>
}
