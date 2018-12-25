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
public interface InputInterface {
    public String getTitle();
    
    public boolean load(String fileName);
    
    // zwraca ilość wartości zgodnie z formatem [całkowita n wierszy, n wierszy dla 1 wartości, n wierszy dla 2 wartości, ...]
    public ArrayList<Integer> getTargetColumnData(ArrayList<ArrayList<String>> rules);
    
    // działa podobnie jak getTargetColumnData tylko, że zwraca macierz gdzie każdy wiersz to wybrany getTargetColumnData
    public ArrayList<ArrayList<Integer>> getSpecificColumnData(String column, ArrayList<ArrayList<String>> rules);
    
    public ArrayList<String> getTargetUniqueValues();
    public ArrayList<String> getUniqueValues(String column);
    public ArrayList<String> getColumns();
}
