/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maszyna;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author adam
 */
public class WnioskowanieTyl extends Wnioskowanie{

    @Override
    public void run() {
        for(int i = 0; i < celeModel.getRowCount(); i++){
            LinkedHashSet<String> unk = new LinkedHashSet();
            unk.add((String)celeModel.getValueAt(i, 0));
            wnioskowanie(unk);
        }
        
        drawGraph();
    }

    @Override //Lewa jest celem, tutaj
    protected String wnioskowanie(String lewa, String prawa){
        return "";
    }
    
    protected boolean wnioskowanie(LinkedHashSet unk) {
        LinkedHashSet<Integer> lista = new LinkedHashSet<>();
        LinkedHashSet<String> unknowns = unk;
        LinkedHashSet<Integer> tmpLista = new LinkedHashSet<>();
        
        Iterator it = unk.iterator();
        String cel = (String)it.next();
        
        while(!unknowns.isEmpty()){
            if(!addRegulyToSet(unknowns, tmpLista)){return false;}
            unknowns.clear();
            for(Integer x: tmpLista){
                przemielRegule(x);
            }
            unknowns.addAll(addUnknowns(tmpLista));
            lista = przemielLista(lista);

            lista.addAll(tmpLista);
            tmpLista.clear();
        
            if(celFound(cel)){
                addLog("Wartość celu " + cel + " wynosi " + getDaneValueWithKey(cel));
                return true;
            }
        }
        
        addLog("Cel " + cel + " niemożliwy do wywnioskowania");
        return false;
    }
    
    private boolean addRegulyToSet(LinkedHashSet setCel, LinkedHashSet lista){
        
        setCel.stream().forEach((cel) -> {
            for(int j = 0; j < regulyModel.getRowCount(); j++){
                String regulaCel = (String)regulyModel.getValueAt(j, 1);
                if(regulaCel.startsWith("!")){regulaCel = regulaCel.substring(1);}
                if(regulaCel.equals(cel)){
                    lista.add(j);
                }
            }
        });
        
        return !lista.isEmpty();
    }
    
    protected LinkedHashSet<String> addUnknowns(LinkedHashSet<Integer> lista){
            
        LinkedHashSet<String> unk = new LinkedHashSet();
        Pattern p = Pattern.compile(unknownPattern);
        boolean isChanged = false;
    
        for (int x : lista) {
            String lewa = (String)regulyModel.getValueAt(x, 0);
            Matcher m = p.matcher(lewa);
            isChanged = false;

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
                    unk.add(word);
                    isChanged = true;
                }
            }
        }

        return unk;
    }
    
    //sprawdza czy regula ma niewiadome i jesli mozliwe wylicza
    //i dodaje danę oraz wartosc reguly
    private boolean przemielRegule(int x){
        String lewa = (String)regulyModel.getValueAt(x, 0);
        String prawa = (String)regulyModel.getValueAt(x, 1);
        
        if(!hasUnknown(lewa)){
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
                addRegulaValue(x, value);
            }
            
            return true;
        }
        
        return false;
    }
    
    protected LinkedHashSet<Integer> przemielLista(LinkedHashSet lista){
        LinkedList<Integer> reverseList = new LinkedList(lista);
        Iterator it = reverseList.descendingIterator();
        while(it.hasNext()){
            int x = (Integer)it.next();
            if(przemielRegule(x)){
                lista.remove(x);
            }
        }
        
        return lista;
    }
    
}