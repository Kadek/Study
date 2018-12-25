/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package labirynt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 *
 * @author adas
 */
public class Labirynt {

    VMenu menu;
    VGridFrame gridFrame;
    VSearchSpeed searchSpeed;
    MGenerator generator;
    MSolver solver;
    
    public Labirynt(){
        generator = new MGenerator();
    }
    
    public static void main(String args[]){
        Labirynt labirynt = new Labirynt();
        labirynt.run();
    }
    
    public void run() {
        java.awt.EventQueue.invokeLater(new MenuRunnable(this));
    }
    
    public void onClickGenerate(String x, String y, String nazwa){
        
        if(generator.validateData(x, y, nazwa)){
            menu.showMessage("Dane poprawne");   
            generator.generate();
            menu.showMessage("Labirynt wygenerowany");
        }else{
            menu.showMessage("Dane niepoprawne");
        }
    }
    
    public void onClickLoadLabirynt(String nazwa){
        
        solver = new MSolver(this);
        if(solver.loadData(nazwa)){
            menu.showMessage("Labirynt poprawnie załadowany");
        }else{
            solver.dumpData();
            menu.showMessage("Nie udało się załadować labiryntu.");
        }
    }
    
    public void onClickSolveLabirynt(){
        if((solver == null) || (!solver.hasGrid())){
            menu.showMessage("Musisz najpierw załadować labirynt.");
            return;
        }
        
        //wizualizacja labiryntu
        java.awt.EventQueue.invokeLater(new GridRunnable(solver.getGrid(), this));
        
        //kontrola prędkości
        java.awt.EventQueue.invokeLater(new SearchSpeedRunnable(this));
    }
    
    public boolean onClickChangeSpeed(int diff){
        if(!gridFrame.hasSelectedStartEnd()){
            menu.showMessage("Musisz wybrać 2 punkty na mapie zanim zaczniemy poszukiwanie.");
            return false;
        }
        
        gridFrame.blockSelectionOfStartEnd();
        
        solver.changeSpeed(diff);
        if(!solver.isSearching()){
            solver.startSearch(gridFrame.getStartEnd());
        }
        
        return true;
    }
    
    public void onClickStopSearching(){
        solver.stopSearching();
        solver = null;
        gridFrame.close();
        gridFrame = null;
        searchSpeed.close();
        searchSpeed = null;
    }
    
    public void searchEnded(){
        searchSpeed.close();
        menu.showMessage("Poszukiwanie zakończone !");
    }

    private class MenuRunnable implements Runnable {

        Labirynt labirynt;
        
        public MenuRunnable(Labirynt labirynt) {
            this.labirynt = labirynt;
        }

        @Override
        public void run() {
            menu = new VMenu(labirynt);
            menu.showMenu();
        }
    } 
    
    private class GridRunnable implements Runnable {
        
        ArrayList<ArrayList<Node>> grid;
        Labirynt labirynt;
        
        public GridRunnable(ArrayList<ArrayList<Node>> grid, Labirynt labirynt){
            this.grid = grid;
            this.labirynt = labirynt;
        }
        
        @Override
        public void run() {
            gridFrame = new VGridFrame(grid, labirynt);
            gridFrame.showGrid();
        }
    } 
    
    private class SearchSpeedRunnable implements Runnable {
        
        Labirynt labirynt;
        
        public SearchSpeedRunnable(Labirynt labirynt){
            this.labirynt = labirynt;
        }
        
        @Override
        public void run() {
            searchSpeed = new VSearchSpeed(labirynt);
            searchSpeed.showSearchSpeed();
        }
    } 
    
    public void updateGraphics(PriorityQueue<Node> openSet, HashSet<Node> closedSet){
        gridFrame.updateGraphics(openSet, closedSet);
    }
}
