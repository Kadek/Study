/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maszyna;

import java.util.*;
import java.io.*;
import java.util.regex.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DataSelector {
    
    private String currentLine;
    String filePath;
    
    private int logCounter = 1;
    
    BufferedReader br;
    HashMap<String, Pattern> patterns = new HashMap<>();
    
    public DataSelector(String filePath){
        try{
            this.filePath = filePath;
            
            patterns.put("dane", Pattern.compile("(?i)dane"));
            patterns.put("dana", Pattern.compile("^([A-Za-z\\[\\]]+) = ([TF])$")); 
            patterns.put("reguly", Pattern.compile("(?i)wzory"));
            patterns.put("regula", Pattern.compile("^([^=]+)=> ?([A-Za-z!()]+)$"));
            patterns.put("szukane", Pattern.compile("(?i)szukane"));
            patterns.put("szukana", Pattern.compile("^([A-Za-z]+) = ([TF?])$")); 
            
        }catch(Exception e){
            System.out.println("Nie udało się załadować pliku.");
            System.exit(0);
        }
    }
    
    public void addDane(JTable dane, JTable log) throws IOException{
        
        Matcher mDana;
        br = new BufferedReader(new FileReader(filePath));
        
        while((currentLine = br.readLine()) != null){
            if(currentLine.equals("Szukane:")){
                return;
            }
            if((mDana = checkMatch(currentLine, patterns.get("dana"))) != null){
                if("".equals(currentLine)) continue;
                DefaultTableModel model = (DefaultTableModel) dane.getModel();
                model.addRow(new Object[]{mDana.group(1), mDana.group(2)});
                
                model = (DefaultTableModel) log.getModel();
                model.addRow(new Object[]{logCounter + ": Dodano dane: " + currentLine});
                logCounter++;
            }
        }
    }
    
    public void addReguly(JTable reguly, JTable log) throws IOException{
        
        Matcher mReguly;
        br = new BufferedReader(new FileReader(filePath));
        
        while((currentLine = br.readLine()) != null){
            if((mReguly = checkMatch(currentLine, patterns.get("regula"))) != null){
                if("".equals(currentLine)) continue;
                DefaultTableModel model = (DefaultTableModel) reguly.getModel();
                model.addRow(new Object[]{mReguly.group(1), mReguly.group(2), "?"});
                
                model = (DefaultTableModel) log.getModel();
                model.addRow(new Object[]{logCounter + ": Dodano regułe: " + currentLine});
                logCounter++;
            }
        }
    }
    
    public void addCele(JTable cele, JTable log) throws IOException{
        
        Matcher mDana;
        br = new BufferedReader(new FileReader(filePath));
        boolean flag = false;
        
        while((currentLine = br.readLine()) != null){
            if(currentLine.equals("Szukane:")){
                flag = true;
            }else if(((mDana = checkMatch(currentLine, patterns.get("szukana"))) != null) && flag) {
                
                if("".equals(currentLine)) continue;
                DefaultTableModel model = (DefaultTableModel) cele.getModel();
                model.addRow(new Object[]{mDana.group(1), mDana.group(2)});
                
                model = (DefaultTableModel) log.getModel();
                model.addRow(new Object[]{logCounter + ": Dodano cel: " + currentLine});
                logCounter++;
            }
        }
    }
    
    private Matcher checkMatch(String currentLine, Pattern pattern){
        Pattern currentPattern = (Pattern) pattern;
        Matcher m = currentPattern.matcher(currentLine);
        if(m.matches()){
            return m;
        }
        
        return null;
    }
    
}
