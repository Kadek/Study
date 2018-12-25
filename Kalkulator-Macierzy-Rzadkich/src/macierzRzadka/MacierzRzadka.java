/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package macierzRzadka;

import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author adam
 */
public class MacierzRzadka {
    
    private final int iloscWierszy;
    private final int iloscKolumn;
    private final Vector[] wektorWierszy;
    
    public MacierzRzadka(int iloscWierszy, int iloscKolumn) {
        this.iloscWierszy = iloscWierszy;
        this.iloscKolumn = iloscKolumn;
        this.wektorWierszy = new Vector[iloscWierszy];
    }
    
    public void insert(int wiersz, int kolumna, double wartosc){
        if(wiersz > iloscWierszy){return ;}
        if(kolumna > iloscKolumn){return ;}
        
        if(wektorWierszy[wiersz] == null){
            wektorWierszy[wiersz] = new Vector();
        }
        
        wektorWierszy[wiersz].add(kolumna, wartosc);
    }
    
    public double get(int wiersz, int kolumna){
        return wektorWierszy[wiersz].get(kolumna);
    }
    
    public Vector getWiersz(int wiersz){
        if(wektorWierszy == null){return null;}
        return wektorWierszy[wiersz];
    }
    
    public int getIloscWierszy(){
        return iloscWierszy;
    }   
    
    public int getIloscKolumn(){
        return iloscKolumn;
    }   
    
    public void show(){
        if(wektorWierszy == null){return ;}
        
        for(int i = 0; i < iloscWierszy; i++){
            for(int j = 0; j < iloscKolumn; j++){
                if(wektorWierszy[i] == null){
                    System.out.print("-");  
                    continue;
                }
                double x = wektorWierszy[i].get(j);
                if(x == 0.0){
                    System.out.print("-");                    
                }else{
                    System.out.print(x);
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }
    
    public MacierzRzadka add(MacierzRzadka B){
        
        int newIloscWierszy = this.getIloscWierszy() > B.getIloscWierszy() ? this.getIloscWierszy() : B.getIloscWierszy();
        int newIloscKolumn = this.getIloscKolumn() > B.getIloscKolumn() ? this.getIloscKolumn() : B.getIloscKolumn();
        MacierzRzadka C = new MacierzRzadka(newIloscWierszy, newIloscKolumn);
        
        for(int i = 0; i < newIloscWierszy; i++){
            if((this.getIloscWierszy() < i+1 || this.wektorWierszy[i] == null)
             && (B.getIloscWierszy() < i+1 || B.wektorWierszy[i] == null)){                
                C.insert(i, 0, 0);
                continue;                    
            }else if(this.getIloscWierszy() == newIloscWierszy && this.wektorWierszy[i] == null){
                C.wektorWierszy[i] = B.wektorWierszy[i];
                continue;
            }else if(B.getIloscWierszy() == newIloscWierszy && B.wektorWierszy[i] == null){
                C.wektorWierszy[i] = this.wektorWierszy[i];
                continue;
            }
            
            Set<Integer> indicesA = this.getIloscWierszy() > i ? this.wektorWierszy[i].getIndices() : null;
            Set<Integer> indicesB = B.getIloscWierszy() > i ? B.wektorWierszy[i].getIndices() : null;
            
            if(indicesA == null){
                if(indicesB == null){
                    C.insert(i, 0, 0);
                    continue;
                }else{
                    indicesA = indicesB;
                }
            }else{
                if(indicesB != null){
                    indicesA.addAll(indicesB);                     
                }               
            }
            
            Iterator it = indicesA.iterator();
            while(it.hasNext()){
                int j = (int)it.next();
                double x = this.getIloscWierszy() > i ? this.wektorWierszy[i].get(j) : 0;
                double y = B.getIloscWierszy() > i ? B.wektorWierszy[i].get(j) : 0;
                C.insert(i, j, x + y);
            }
        }
        
        return C;        
    }
    
    public MacierzRzadka multiply(MacierzRzadka B){
        if(this.getIloscKolumn() != B.getIloscWierszy()){
            System.out.println("Złe wymiary macierzy");
            return null;
        }
        
        MacierzRzadka C = new MacierzRzadka(this.getIloscWierszy(), B.getIloscKolumn());
        MacierzRzadka tmp = B.transpose();
        
        for(int i = 0 ; i < this.getIloscWierszy(); i++){
            for(int j = 0; j < B.getIloscKolumn(); j++){
                int sum = 0;
                if(this.wektorWierszy[i] == null || tmp.wektorWierszy[j] == null){
                    C.insert(i, j, 0);
                    continue;                    
                }
                Set<Integer> indicesA = this.wektorWierszy[i].getIndices();
                Set<Integer> indicesB = tmp.wektorWierszy[j].getIndices();
                
                if(indicesA == null){
                    C.insert(i, j, 0);
                    continue;
                }else if(indicesB != null){
                    indicesA.retainAll(indicesB);                    
                }

                Iterator it = indicesA.iterator();
                while(it.hasNext()){
                    int q = (int)it.next();
                    sum += this.wektorWierszy[i].get(q) * tmp.wektorWierszy[j].get(q);
                }
                C.insert(i, j, sum);
            }
        }
        
        return C;
    }
    
    public MacierzRzadka transpose(){        
        MacierzRzadka C = new MacierzRzadka(this.getIloscKolumn(), this.getIloscWierszy());
                
        for(int i = 0; i < iloscWierszy; i++){
            if(wektorWierszy[i] == null){continue ;}
            Set<Integer> indices = this.wektorWierszy[i].getIndices();
            if(indices == null){
                continue;
            }
            Iterator it = indices.iterator();
            while(it.hasNext()){
                int j = (int)it.next();
                C.insert(j, i, this.wektorWierszy[i].get(j));
            }
        }
        
        for(int i = 0; i < C.iloscWierszy; i++){
            if(C.wektorWierszy[i] == null){
                C.insert(i, 0, 0);
            }
        }
        
        return C;
    }

}
