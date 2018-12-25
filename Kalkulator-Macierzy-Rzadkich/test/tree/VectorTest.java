/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tree;

import macierzRzadka.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author adam
 */
public class VectorTest {
    
    /**
     * Test of add method, of class Vector.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        Vector instance1 = new Vector();
        Vector instance2 = new Vector();
        Vector instance3 = new Vector();
        
        System.out.println("Sprawdzanie daneTest1.txt");
        readAndTest(instance1, "daneTest1.txt");
        instance1.dumpTree();
        System.out.println("Dane działają");
        System.out.println("Sprawdzanie daneTest2.txt");
        readAndTest(instance2, "daneTest2.txt");
        instance2.dumpTree();
        System.out.println("Dane działają");
        System.out.println("Sprawdzanie daneTest3.txt");
        readAndTest(instance3, "daneTest3.txt");
        instance3.dumpTree();
        System.out.println("Dane działają");
        
    }   
    
    private void readAndTest(Vector instance, String fileName){
        
        HashMap<Integer, Double> verificationData = new HashMap<>();
        
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
            String line;
            while((line = br.readLine()) != null){
                String[] lineSplit = line.split(" ");
                verificationData.put(Integer.parseInt(lineSplit[0]), Double.parseDouble(lineSplit[1]));
                instance.add(Integer.parseInt(lineSplit[0]), Integer.parseInt(lineSplit[1]));
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
        instance.dumpVector();
        
        for(Integer index: verificationData.keySet()){
            assertEquals(instance.get(index),verificationData.get(index), 0);
        }
        
    }

}
