/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tree;

import macierzRzadka.Tree;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author adam
 */
public class TreeTest {
    
    @Test    
    public void testTest(){
        Tree treeInstance = new Tree();
        
        treeInstance.insert(1, 2);
        treeInstance.insert(2, 5);
        treeInstance.insert(3, -1);
        
        System.out.println("Test znajdź 2 pod 1");
        System.out.println(treeInstance.get(1));
        assertEquals(2, treeInstance.get(1), 0);
        System.out.println("Test znajdź 5 pod 2");
        System.out.println(treeInstance.get(2));
        assertEquals(5, treeInstance.get(2), 0);
        System.out.println("Test znajdź -1 pod 3");
        System.out.println(treeInstance.get(3));
        assertEquals(-1, treeInstance.get(3), 0);
        System.out.println("Test znajdź 0 pod każdym innym");
        System.out.println(treeInstance.get(100));
        assertEquals(0, treeInstance.get(100), 0);      
        
    }

    /**
     * Test of delete method, of class Tree.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        int index = 0;
        Tree instance = new Tree();
        instance.insert(4,2);
        instance.insert(8,5);
        instance.insert(2,-1);
        instance.insert(6,23);
        instance.insert(3, 7);
        instance.insert(9,-10);
        
        instance.dump();
        instance.delete(8);
        instance.dump();
        
        assertEquals(instance.get(8), 0, 0);
    }
 
}
