/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drzewo.decyzyjne;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import org.arvamer.Vadr;
import org.arvamer.Vadr.Query;

/**
 *
 * @author adas
 */
public class AdvancedInput implements InputInterface{
    public String title = "System wczytywania danych z zaawansowanego systemu wczytywania danych";
    Vadr advancedInput;
    
    public AdvancedInput(){
        this.advancedInput = new Vadr();
    }
    
    @Override
    public String getTitle(){
        return title;
    }

    @Override
    public boolean load(String fileName) {
        try {
            advancedInput.open(fileName);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @Override
    public ArrayList<Integer> getTargetColumnData(ArrayList<ArrayList<String>> rules) {
                
        HashMap<String, Integer> count = createHashMap();
        fillHashMap(rules, count);
        ArrayList<Integer> res = transformHashMapToArrayList(count);
        
        return res;
    }
    
    @Override
    public ArrayList<ArrayList<Integer>> getSpecificColumnData(String column, ArrayList<ArrayList<String>> rules) {        
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
    public ArrayList<String> getUniqueValues(String column) {
        ArrayList<String> res = new ArrayList<>();
        try {
            Iterator<String> uniqueValuesQuery = advancedInput.query().unique().only(column);
            while(uniqueValuesQuery.hasNext()){
                res.add(uniqueValuesQuery.next());
            }
        } catch (Exception e){
            return res;
        }
            
        return res;
    }

    @Override
    public ArrayList<String> getColumns() {
        return advancedInput.header();
    }

    @Override
    public ArrayList<String> getTargetUniqueValues() {
        return getUniqueValues(advancedInput.header().get(advancedInput.header().size() - 1));
    }
    
    private ArrayList<String> transformArrayArraytoArray(ArrayList<ArrayList<String>> in){
        ArrayList<String> out = new ArrayList<>();
        for(int i = 0; i < in.size(); i++){
            out.add(in.get(i).get(0));
            out.add(in.get(i).get(1));
        }
        
        return out;
    }
    
    private HashMap<String, Integer> createHashMap(){
        HashMap<String, Integer> hash = new HashMap<>();
        
        ArrayList<String> uniqueValues = getTargetUniqueValues();
        for(int i = 0 ; i < uniqueValues.size(); i++){
            hash.put(uniqueValues.get(i), 0);
        }
        
        return hash;
    }
    
    private void fillHashMap(ArrayList<ArrayList<String>> rules, HashMap<String, Integer> count){
        ArrayList<String> uniqueValues = getTargetUniqueValues();
        String column = advancedInput.header().get(advancedInput.header().size()-1);
        
        count.put("Total", 0);
        if(rules == null){
            rules = new ArrayList<>();
        }
        for(int i = 0; i<uniqueValues.size(); i++){
            
            ArrayList<String> tmpRule = new ArrayList<>();
            tmpRule.add(column);
            tmpRule.add(uniqueValues.get(i));
            rules.add(tmpRule);
            
            Query tmpData;
            try {
                tmpData = advancedInput.query().eq(transformArrayArraytoArray(rules));
            } catch (Exception e){
                tmpData = null;
            }
            
            count.put(uniqueValues.get(i), tmpData.count());
            count.put("Total", count.get("Total")+tmpData.count());
            
            rules.remove(rules.size()-1);
        }
    }
    
    private ArrayList<Integer> transformHashMapToArrayList(HashMap<String,Integer> count){
        int total = count.get("Total");
        count.remove("Total");
        ArrayList<Integer> res = new ArrayList<>(count.values());
        res.add(0, total);
        
        return res;
    } 
}
