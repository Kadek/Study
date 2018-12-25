/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maszyna;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author adam
 */
public abstract class Wnioskowanie {
    HashMap tables;
    protected DefaultTableModel daneModel, regulyModel, celeModel, logModel;
    
    public abstract void run();
    protected abstract String wnioskowanie(String lewa, String prawa);
    
    String unknownPattern = "([A-Za-z\\]\\[]+)";
    String compilePattern = "(!?[A-Za-z\\[\\]]+)";
    
    public void setTables(HashMap tables) {
        this.tables = tables;
        
        JTable dane = (JTable) this.tables.get("dane");
        this.daneModel = (DefaultTableModel) dane.getModel();
        
        JTable cele = (JTable) this.tables.get("cele");
        this.celeModel = (DefaultTableModel) cele.getModel();
        
        JTable reguly = (JTable) this.tables.get("reguly");
        this.regulyModel = (DefaultTableModel) reguly.getModel();
        
        JTable log = (JTable) this.tables.get("log");
        this.logModel = (DefaultTableModel) log.getModel();
    }
    
    protected boolean hasUnknown(String lewa){
        Pattern p = Pattern.compile(unknownPattern);
        Matcher m = p.matcher(lewa);
        
        while(m.find()){
            String word = m.group();
            boolean flag = false;
            for(int i = 0; i < daneModel.getRowCount(); i++){
                String rowWord = (String)daneModel.getValueAt(i, 0);
                if(rowWord.equals(word)){
                    if(!((String)daneModel.getValueAt(i, 1)).equals("?")){
                        flag = true;
                    }
                    break;
                }
            }
            if(!flag){
                addLog("Dla reguły " + lewa + " brakuje danej " + word);
                return true;
            }
        }
        
        return false;
    }
    
    protected String compute(String lewa){
        
        for(int i = 0 ; i < regulyModel.getRowCount(); i++){
            if(regulyModel.getValueAt(i, 0).equals(lewa)){
                String value = (String)regulyModel.getValueAt(i, 0);
                if(value.equals("F")){return "F";}
                if(value.equals("T")){return "T";}
            }
        }
        
        Pattern p = Pattern.compile(compilePattern);
        Matcher m = p.matcher(lewa);
        while(m.find()){
            System.out.println(m);
            String value = getDaneValueWithKey(m.group());
            System.out.println(m.group());
            lewa = lewa.replace(m.group(), value);
            System.out.println(lewa);
        }
        
        ONP onp = new ONP();
        lewa = onp.transformToONP(lewa);
        return onp.computeONP(lewa);
    }
    
    protected void addLog(String message){
        Object[] log = new Object[]{message};
        logModel.addRow(log);
    }
    
    protected String getDaneValueWithKey(String dana){
        boolean flag = false;
        if(dana.startsWith("!")){
            flag = true;
            dana = dana.substring(1);
        }
        for(int i = 0; i < daneModel.getRowCount(); i++){
            String tmp = (String)daneModel.getValueAt(i, 0);
            if(dana.equals(tmp)){
                if(flag){
                    tmp = (String)daneModel.getValueAt(i, 1);
                    if(tmp.equals("T")){tmp = "F";}else{tmp = "T";}
                    return tmp;
                }else{
                    return (String)daneModel.getValueAt(i, 1);
                }
            }
        }
        return "-1";
    }
    
    protected void addDana(String prawa, String value){
        if(getDaneValueWithKey(prawa).equals("-1")){
            daneModel.addRow(new Object[] {prawa, value});
        }else{
            for(int i = 0; i < daneModel.getRowCount(); i++){
                if(daneModel.getValueAt(i, 0).equals(prawa)){
                    daneModel.setValueAt((Object)value, i, 1);
                    return;
                }
            }
        }
    }
    
    protected void addRegulaValue(int x, String value){
        
        if(regulyModel.getValueAt(x, 2).equals("?")){
            regulyModel.setValueAt((Object)value, x, 2);
        }
    }
    
    protected boolean celFound(String cel){
        for(int i = 0; i < daneModel.getRowCount(); i++){
            if(((String)daneModel.getValueAt(i, 0)).equals(cel)){
                return !(((String)daneModel.getValueAt(i, 1)).equals("?"));
            }
        }
        return false;
    }
    
    protected void drawGraph(){
        
        WnioskowanieXML xmlGenerator = new WnioskowanieXML(regulyModel);
        xmlGenerator.generateXML();
        WnioskowanieGraph graph = new WnioskowanieGraph();
        graph.run();
    }
}
