/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tree;

import macierzRzadka.MacierzRzadka;
import org.junit.Test;

/**
 *
 * @author adam
 */
public class MacierzRzadkaTest {
    
    public MacierzRzadkaTest() {
    }

    /**
     * Test of show method, of class MacierzRzadka.
     */
    @Test
    public void testShow() {
        System.out.println("***********************************************");
        System.out.println("show");
        MacierzRzadka instance = new MacierzRzadka(5,5);
                
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("pełna macierz");
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                instance.insert(i, j, 5);
            }
        }
        
        instance.show();
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("pusta macierz");        
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                instance.insert(i, j, 0);
            }
        }
        
        instance.show();        
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("rzadka macierz");
        instance = new MacierzRzadka(5,5);
        instance.insert(1, 1, 1);
        instance.insert(4, 3, 7);
        
        
        instance.show();
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("brak macierzy");   
        instance = new MacierzRzadka(0,0);  
        
        instance.show();
        
    }
        
    /**
     * Test of show method, of class MacierzRzadka.
     */
    @Test
    public void testTranspose() {
        System.out.println("***********************************************");
        System.out.println("transpose");
        MacierzRzadka instance = new MacierzRzadka(5,5);  
        MacierzRzadka instanceB;
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("pełna macierz");
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                instance.insert(i, j, 5);
            }
        }
        
        instance.show();
        System.out.println("TRANSPOZYCJA");
        instanceB = instance.transpose();
        instanceB.show();
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("pusta macierz");        
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                instance.insert(i, j, 0);
            }
        }
        
        instance.show();
        System.out.println("TRANSPOZYCJA");
        instanceB = instance.transpose();
        instanceB.show();    
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("rzadka macierz");
        instance = new MacierzRzadka(5,5);
        instance.insert(1, 1, 1);
        instance.insert(4, 3, 7);
        
        instance.show();
        System.out.println("TRANSPOZYCJA");
        instanceB = instance.transpose();
        instanceB.show();
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("niewymiarowa macierz");
        instance = new MacierzRzadka(1,20);
        instance.insert(0, 1, 1);
        instance.insert(0, 17, 7);
        
        
        instance.show();
        System.out.println("TRANSPOZYCJA");
        instanceB = instance.transpose();
        instanceB.show();
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("brak macierzy");   
        instance = new MacierzRzadka(0,0);  
        
        instance.show();
        System.out.println("TRANSPOZYCJA");
        instanceB = instance.transpose();
        instanceB.show();
        
    }
    
    /**
     * Test of show method, of class MacierzRzadka.
     */
    @Test
    public void testMultiply() {
        System.out.println("***********************************************");
        System.out.println("multiply");
        MacierzRzadka instanceA = new MacierzRzadka(5,3);
        MacierzRzadka instanceB = new MacierzRzadka(3,5);            
        MacierzRzadka instanceC ;
                
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("pełna macierz");
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 3; j++){
                instanceA.insert(i, j, i+(i*j));
            }
        }
        
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 5; j++){
                instanceB.insert(i, j, i+(i*j));
            }
        } 
        
        instanceC = instanceA.multiply(instanceB);
        
        System.out.println("Macierz A");
        instanceA.show();
        System.out.println("Macierz B");
        instanceB.show();
        System.out.println("Macierz C");
        instanceC.show(); 
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("pusta macierz");        
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 3; j++){
                instanceA.insert(i, j, 0);
            }
        }
        
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 5; j++){
                instanceB.insert(i, j, 0);
            }
        } 
        
        instanceC = instanceA.multiply(instanceB);
        
        System.out.println("Macierz A");
        instanceA.show();
        System.out.println("Macierz B");
        instanceB.show();
        System.out.println("Macierz C");
        instanceC.show(); 
               
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("rzadka macierz");
        instanceA = new MacierzRzadka(5,3);
        instanceB = new MacierzRzadka(3,5);
        instanceA.insert(0, 0, 1);
        instanceA.insert(2, 1, 6);
        instanceA.insert(4, 0, -1);
        
        instanceB.insert(0, 0, 3);
        instanceB.insert(0, 4, 10);
        instanceB.insert(2, 2, 5);
        
        instanceC = instanceA.multiply(instanceB);
        
        System.out.println("Macierz A");
        instanceA.show();
        System.out.println("Macierz B");
        instanceB.show();
        System.out.println("Macierz C");
        instanceC.show(); 
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("brak macierzy");   
        instanceA = new MacierzRzadka(0,0);
        instanceB = new MacierzRzadka(0,0);
        instanceC = instanceA.multiply(instanceB);
        
        System.out.println("Macierz A");
        instanceA.show();
        System.out.println("Macierz B");
        instanceB.show();
        System.out.println("Macierz C");
        instanceC.show();           
    }
            
    /**
     * Test of show method, of class MacierzRzadka.
     */
    @Test
    public void testAdd() {
        System.out.println("***********************************************");
        System.out.println("Add");
        MacierzRzadka instanceA = new MacierzRzadka(5,5);
        MacierzRzadka instanceB = new MacierzRzadka(5,5);      
                
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("pełna macierz");
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                instanceA.insert(i, j, i+(i*j));
            }
        }
        
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                instanceB.insert(i, j, i+(i*j));
            }
        }  
         
        MacierzRzadka instanceC = instanceA.add(instanceB);
        
        System.out.println("Macierz A");
        instanceA.show();
        System.out.println("Macierz B");
        instanceB.show();
        System.out.println("Macierz C");
        instanceC.show(); 
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("pusta macierz");        
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                instanceA.insert(i, j, 0);
            }
        }
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                instanceB.insert(i, j, 0);
            }
        }  
         
        instanceC = instanceA.add(instanceB);
        
        System.out.println("Macierz A");
        instanceA.show();
        System.out.println("Macierz B");
        instanceB.show();
        System.out.println("Macierz C");
        instanceC.show();       
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("rzadka macierz");    
        instanceA = new MacierzRzadka(5,5);
        instanceB = new MacierzRzadka(5,5);   
        
        instanceA.insert(0, 0, 1);
        instanceA.insert(2, 1, 6);
        instanceA.insert(4, 0, -1);
        
        instanceB.insert(0, 0, 3);
        instanceB.insert(0, 4, 10);
        instanceB.insert(2, 2, 5);
         
        instanceC = instanceA.add(instanceB);
        
        System.out.println("Macierz A");
        instanceA.show();
        System.out.println("Macierz B");
        instanceB.show();
        System.out.println("Macierz C");
        instanceC.show(); 
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("brak macierzy");   
        instanceA = new MacierzRzadka(0,0);
        instanceB = new MacierzRzadka(0,0); 
        
        instanceC = instanceA.add(instanceB);
        
        System.out.println("Macierz A");
        instanceA.show();
        System.out.println("Macierz B");
        instanceB.show();
        System.out.println("Macierz C");
        instanceC.show(); 
        
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("niewymiarowa macierz");
        instanceA = new MacierzRzadka(1,20);
        instanceB = new MacierzRzadka(20,1);
        instanceA.insert(0, 17, 1);
        instanceA.insert(0, 0, 1);
        instanceB.insert(17, 0, 7);
        instanceB.insert(10, 0, 7);
        
        
        instanceC = instanceA.add(instanceB);
        
        System.out.println("Macierz A");
        instanceA.show();
        System.out.println("Macierz B");
        instanceB.show();
        System.out.println("Macierz C");
        instanceC.show(); 
    }
}
