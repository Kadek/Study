/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package macierzRzadka;

import java.util.Set;

/**
 *
 * @author kasperoa
 */
public class Vector {
    
    Tree treeInstance;
    
    public Vector(){
        this.treeInstance = new Tree();
    }
    
    public void add(int index, double x){
        if(x == 0){
            treeInstance.delete(index);
        }else{
            treeInstance.insert(index, x);
        }
    }
    
    public double get(int index){
        return treeInstance.get(index);
    }
    
    public Set<Integer> getIndices(){
        return treeInstance.getIndices();
    }
    
    public void dumpVector(){
        System.out.println("Element posortowane według indeksów");
        treeInstance.dumpOrdered();
    }
    
    public void dumpTree(){
        System.out.println("Struktura drzewa");
        treeInstance.dump();
    }
}
