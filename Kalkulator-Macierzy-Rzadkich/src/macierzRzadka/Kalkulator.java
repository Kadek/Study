/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package macierzRzadka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author adam
 */
public class Kalkulator {
    
    public Kalkulator(){
        
    }
    
    public void operate(){
        Scanner sc = new Scanner(System.in);
        File f;
        
        String matrixApath = null;
        do{
            System.out.println("Podaj ścieżkę do pierwszego pliku");
            matrixApath = sc.nextLine();

            f = new File(matrixApath);
        }while(!(f.exists() && !f.isDirectory()));
        
        String matrixBpath;
        do{
            System.out.println("Podaj ścieżkę do drugiego pliku lub napisz brak jeśli nie potrzebny");
            matrixBpath = sc.nextLine();

            if("brak".equals(matrixBpath)){ break;}
            f = new File(matrixBpath);
        }while(!(f.exists() && !f.isDirectory()));
        
        String matrixCpath = null;
        System.out.println("Podaj ścieżkę w której ma być zapisany plik");
        matrixCpath = sc.nextLine();
        
        Set<String> operations;
        String operation = null;
        operations = new HashSet<>();
        operations.add("+");
        operations.add("*");
        operations.add("T");
        do{
            System.out.println("Podaj operację do wykonania (+*T)");
            operation = sc.nextLine();
        }while(!operations.contains(operation));
        
        System.out.println("Wykonujemy operację " + matrixApath + operation  + matrixBpath);
        
        MacierzRzadka matrixA = null;
        MacierzRzadka matrixB = null;
        MacierzRzadka matrixC;
        try {
            matrixA = loadData(matrixA, matrixApath);
        } catch (IOException ex) {
            System.out.println("Problem z plikem.");
        }
        
        if(!"brak".equals(matrixBpath)){
            try {
                matrixB = loadData(matrixB, matrixBpath);
            } catch (IOException ex) {
                System.out.println("Problem z plikem.");
            }
        }
        
        if(matrixA == null){
            System.out.println("Nie udało się wczytać macierzy A. Przerywam program");
            return;
        }
                
        if("+".equals(operation)){
            if(matrixB == null){
                System.out.println("Operacja wymaga dwóch macierzy. Przerywam program");
                return;
            }
            matrixC = matrixA.add(matrixB);
        }else if("*".equals(operation)){
            if(matrixB == null){
                System.out.println("Operacja wymaga dwóch macierzy. Przerywam program");
                return;
            }
            matrixC = matrixA.multiply(matrixB);            
        }else{
            matrixC = matrixA.transpose();            
        }
        
        saveData(matrixC, matrixCpath);
    }
    
    private MacierzRzadka loadData(MacierzRzadka matrix, String matrixPath) throws FileNotFoundException, IOException{
        BufferedReader in = new BufferedReader(new FileReader(matrixPath));
        
        String line;
        line = in.readLine();
        int w = Integer.parseInt(line.split(" ")[0]);
        int k = Integer.parseInt(line.split(" ")[1]);
        if(w <= 0 || k <= 0){
            System.out.println("Plik posiada niepoprawne dane");
            return null;
        }
        
        matrix = new MacierzRzadka(w,k);
        
        while((line = in.readLine()) != null)
        {
            String[] lineSplit = line.split(" ");
            int x = Integer.parseInt(lineSplit[0]);
            int y = Integer.parseInt(lineSplit[1]);
            int z = Integer.parseInt(lineSplit[2]);
            matrix.insert(x,y,z);
        }
        in.close();
        
        return matrix;
    }
    
    private void saveData(MacierzRzadka matrix, String matrixPath){
        ArrayList<String> lines = new ArrayList<>();
        String line = matrix.getIloscWierszy() +" "+ matrix.getIloscKolumn();
        lines.add(line);
        
        for(int i = 0; i < matrix.getIloscWierszy(); i++){
            Set<Integer> indices = matrix.getWiersz(i).getIndices();
            if(indices == null){continue ;}
            Iterator it = indices.iterator();
            while(it.hasNext()){
                int x = (int)it.next();
                line = i + " " + x + " " + matrix.get(i,x);
                lines.add(line);
            }
        }
        
        Path file = Paths.get(matrixPath);
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            System.out.println("Zapisywanie się nie powiodło.");
        }
    }
}
