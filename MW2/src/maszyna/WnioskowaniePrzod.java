/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maszyna;

/**
 *
 * @author adam
 */
public class WnioskowaniePrzod extends Wnioskowanie{

    @Override
    public void run(){
        int rowCount = regulyModel.getRowCount();
        
        boolean flag;
        
        for(int i = 0; i < rowCount; i++){
            flag = false;
            if(regulyModel.getValueAt(i, 2).equals("?")){
                String regulyValue = wnioskowanie((String)regulyModel.getValueAt(i, 0), 
                                                  (String)regulyModel.getValueAt(i, 1));
                if(!regulyValue.isEmpty()){
                    regulyModel.setValueAt((Object) regulyValue, i, 2);
                }
            }
            if(flag){i = 0;}
        }
        
        drawGraph();
    }

    @Override
    protected String wnioskowanie(String lewa, String prawa) {
        if(!hasUnknown(lewa)){
            addLog("Obliczanie wniosku : " + lewa + " => " + prawa);
            String value = compute(lewa);
            if(value.equals("F")){
                value = "?";
                addDana(prawa,value);
            }else{
                if(prawa.startsWith("!")){
                    if(value.equals("F")){value = "T";}else{value = "F";}
                    addDana(prawa.substring(1),value);
                }else{
                    addDana(prawa,value);
                }
            }
            
            addLog("Dodano danę " + prawa + " o wartości " + value);
            return value;
        }
        return "";
    }
}
