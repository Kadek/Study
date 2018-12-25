/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package macierzRzadka;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author kasperoa
 */
public class Tree {
    
    private Node root;
    
    public void insert(int index, double x){
        Node newNode = new Node(x, index);
        
        if(root == null){
            root = newNode;
        }else{
            insertRec(root, newNode);
        }
    }
    
    private void insertRec(Node currentNode, Node newNode){
        if(currentNode.getIndex() == newNode.getIndex()){
            currentNode.setX(newNode.getX());
        }else if(currentNode.getIndex() > newNode.getIndex()){
            if(currentNode.getLeft() != null){
                insertRec(currentNode.getLeft(), newNode);
            }else{
                currentNode.setLeft(newNode);
            }                
        }else{
            if(currentNode.getRight() != null){
                insertRec(currentNode.getRight(), newNode);
            }else{
                currentNode.setRight(newNode);
            }                
        }
    }
    
    public double get(int index){
        return getRec(root, index);        
    }
    
    private double getRec(Node currentNode, int index){
        if(currentNode == null){
            return 0.0;
        }else if(currentNode.getIndex() == index){
            return currentNode.getX();
        }else if(currentNode.getIndex() > index){
            return getRec(currentNode.getLeft(), index);
        }else{
            return getRec(currentNode.getRight(), index);
        }
    }
    
    //wypisywanie drzewa breadth-first
    public void dump(){
        if(root == null){
            System.out.println("Brak danych");
            return;
        }
        ArrayList<Node> nodesRow1 = new ArrayList<>();
        ArrayList<Node> nodesRow2 = new ArrayList<>();
        nodesRow1.add(root);
        int depth = 1;
        
        while(!nodesRow1.isEmpty()){
            
            Iterator it = nodesRow1.iterator();
            while(it.hasNext()){
                Node nextNode = (Node) it.next();
                System.out.println("Głębokość: " + depth + " indeks: " + nextNode.getIndex() + " wartość: " + nextNode.getX());
                if(nextNode.hasLeft()){ nodesRow2.add(nextNode.getLeft());}
                if(nextNode.hasRight()){nodesRow2.add(nextNode.getRight());}
            }
            nodesRow1.clear();
            
            depth++;

            it = nodesRow2.iterator();
            while(it.hasNext()){
                Node nextNode = (Node) it.next();
                System.out.println("Głębokość: " + depth + " indeks: " + nextNode.getIndex() + " wartość: " + nextNode.getX());
                if(nextNode.hasLeft()){nodesRow1.add(nextNode.getLeft());}
                if(nextNode.hasRight()){nodesRow1.add(nextNode.getRight());}
            }     
            nodesRow2.clear();
            
            depth++;
        }
    }
    
    //wypisywanie drzewa inorder
    public void dumpOrdered(){
        if(root == null){
            System.out.println("Brak danych");
            return;
        }
        dumpRec(root);
    }
    
    private void dumpRec(Node currentNode){
        if(currentNode.hasLeft()){dumpRec(currentNode.getLeft());}
        System.out.println("Indeks: " + currentNode.getIndex() + " wartość: " + currentNode.getX());
        if(currentNode.hasRight()){dumpRec(currentNode.getRight());}
    }
    
    public void delete(int index){
        
        //Znajdź węzeł do usunięcia
        Node target = findNode(index);
        if(target == null){
            return;
        }
        
        //Włóż poddrzewa na stos
        Stack nodesStack = new Stack();
        putChildrenOnStack(nodesStack, target);
        //pop usuwa ostatni element z poddrzewa który jest targetem
        nodesStack.pop();
        
        //Usuń element celowy z drzewa 
        if(target.equals(root)){
            root = null;
        }else{
            Node parent = findParent(target);
            if(parent.hasRight() && parent.getRight().equals(target)){
                parent.setRight(null);
            }else{
                parent.setLeft(null);
            }
        }
        
        insertChildrenIntoTree(nodesStack);
    }
    
    private void insertChildrenIntoTree(Stack nodesStack){
        while(!nodesStack.isEmpty()){
            Node currentNode = (Node)nodesStack.pop();
            insert(currentNode.getIndex(), currentNode.getX()); 
        }
    }
    
    private void putChildrenOnStack(Stack nodesStack, Node root){
        if(root.hasLeft()){putChildrenOnStack(nodesStack, root.getLeft());}
        if(root.hasRight()){putChildrenOnStack(nodesStack, root.getRight());}
        root.setLeft(null);
        root.setRight(null);
        nodesStack.push(root);
    }
    
    private Node findNode(int index){
        return findNodeRec(root, index); 
    }
     
    private Node findNodeRec(Node currentNode, int index){
        if(currentNode == null){
            return null;
        }else if(currentNode.getIndex() == index){
            return currentNode;
        }else if(currentNode.getIndex() > index){
            return findNodeRec(currentNode.getLeft(), index);
        }else{
            return findNodeRec(currentNode.getRight(), index);
        }
    }    
    
    private Node findParent(Node child){
        return findParentRec(root, child); 
    }
     
    private Node findParentRec(Node currentNode, Node child){
        if(currentNode == null){
            return null;
        }else if((currentNode.hasRight() && currentNode.getRight().equals(child)) || 
                 (currentNode.hasLeft() && currentNode.getLeft().equals(child))){
            return currentNode;
        }else if(currentNode.getIndex() > child.getIndex()){
            return findParentRec(currentNode.getLeft(), child);
        }else{
            return findParentRec(currentNode.getRight(), child);
        }
    }
    
    public Set<Integer> getIndices(){
        Set<Integer> indices = new HashSet<>();
        
        if(root == null){
            return null;
        }
        getIndicesRec(root, indices);
        
        return indices;
    }
    
    private void getIndicesRec(Node root, Set<Integer> indices){
        indices.add(root.getIndex());
        if(root.hasLeft()){getIndicesRec(root.getLeft(), indices);}
        if(root.hasRight()){getIndicesRec(root.getRight(), indices);}
    }
       
    public void test(){
        insert(2, 2.0);
        insert(1, 10.0);
        insert(4, 12.0);
        insert(5, 59.0);
        insert(3, 3.0);
        
        dump();
    }
}
