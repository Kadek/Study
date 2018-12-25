/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drzewo.decyzyjne;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/**
 *
 * @author adas
 */
public class ConsoleInput implements InputInterface{
    private final String title = "Prosty system wczytywania danych z pliku";
    ArrayList<ArrayList<String>> data;
    ArrayList<String> columns;
    
    @Override
    public String getTitle(){
        return title;
    }
    
    @Override
    public boolean load(String fileName){

        BufferedReader br = null;
        FileReader fr = null;

        try {
            String sCurrentLine;
            String sHeader;
            
            br = new BufferedReader(new FileReader(fileName));

            sHeader = br.readLine();
            String[] splitLine = sHeader.split(" ");
            columns = new ArrayList<>();
            for(int i =0 ; i < splitLine.length; i++){
                columns.add(splitLine[i]);
            }
            
            data = new ArrayList<>();
            while ((sCurrentLine = br.readLine()) != null) {
                splitLine = sCurrentLine.split(" ");
                data.add(new ArrayList<>());
                for(int i = 0 ; i < splitLine.length; i++){
                    data.get(data.size()-1).add(splitLine[i]);
                }
            }
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (IOException ex) {
                return false;
            }
        }
        
        return true;
    }    
    
    
    // <editor-fold defaultstate="collapsed" desc="implementacja getTargetColumnData">
    @Override
    public ArrayList<Integer> getTargetColumnData(ArrayList<ArrayList<String>> rules){
        ArrayList<ArrayList<String>> specificData = cleanData(rules);
        
        HashMap<String,Integer> count = createHashMap();   
        
        fillHashMap(specificData, count);
        
        ArrayList<Integer> list = transformHashMapToArrayList(count);
        return list;
    }
    
    private ArrayList<ArrayList<String>> cleanData(ArrayList<ArrayList<String>> rules){
        ArrayList<ArrayList<String>> specificData = new ArrayList<>(data);
        if(rules != null){
            for(int i = 0 ; i < rules.size(); i++){
                int index = columns.indexOf(rules.get(i).get(0));
                if(index == -1){continue ;}
                for(int j = 0; j < specificData.size(); j++){
                    if(!specificData.get(j).get(index).equals(rules.get(i).get(1))){
                        specificData.remove(j);
                        j--;
                    }
                }
            }
        }

        return specificData;
    }
    
    private HashMap<String, Integer> createHashMap(){
        HashMap<String, Integer> hash = new HashMap<>();
        
        ArrayList<String> uniqueValue = getTargetUniqueValues();
        for(int i = 0 ; i < uniqueValue.size(); i++){
            hash.put(uniqueValue.get(i), 0);
        }
        
        return hash;
    }
    
    private void fillHashMap(ArrayList<ArrayList<String>> specificData, HashMap<String,Integer> count){
        count.put("Total", 0);
        for(int i = 0; i < specificData.size(); i++){
            String hash = specificData.get(i).get(specificData.get(i).size()-1);
            if(!count.containsKey(hash)){
                count.put(hash, 0);
            }
            count.put(hash, count.get(hash)+1);
            count.put("Total", count.get("Total")+1);
        }        
    }
    
    private ArrayList<Integer> transformHashMapToArrayList(HashMap<String,Integer> count){
        int total = count.get("Total");
        count.remove("Total");
        ArrayList<Integer> res = new ArrayList<>(count.values());
        res.add(0, total);
        
        return res;
    }
    
    // </editor-fold>
    
    @Override
    public ArrayList<ArrayList<Integer>> getSpecificColumnData(String column, ArrayList<ArrayList<String>> rules){
        
        ArrayList<ArrayList<Integer>> res = new ArrayList<>();
        ArrayList<String> uniqueValues = getUniqueValues(column);
        
        if(rules == null){
            rules = new ArrayList<>();
        }
        
        for(int i = 0; i < uniqueValues.size(); i++){
            ArrayList<String> specificValue = new ArrayList<>();
            specificValue.add(column);
            specificValue.add(uniqueValues.get(i));
            
            rules.add(specificValue);
            res.add(getTargetColumnData(rules));
            rules.remove(rules.size()-1);
        }
        return res;
    }
    
    @Override
    public ArrayList<String> getTargetUniqueValues(){
        int index = columns.size()-1;
        TreeSet<String> uniqueValues = new TreeSet<>();
        for(int i = 0 ; i < data.size(); i++){
            uniqueValues.add(data.get(i).get(index));
        }
        
        return new ArrayList<>(uniqueValues);
    }
    
    @Override
    public ArrayList<String> getUniqueValues(String column){
        int index = columns.indexOf(column);
        TreeSet<String> uniqueValues = new TreeSet<>();
        for(int i = 0 ; i < data.size(); i++){
            uniqueValues.add(data.get(i).get(index));
        }
        
        return new ArrayList<>(uniqueValues);
    }
    
    @Override
    public ArrayList<String> getColumns(){
        return columns;
    }
    
    public void showData(){
        for(int i = 0; i < data.size(); i++){
            System.out.println(data.get(i).toString());
        }
    }
}
