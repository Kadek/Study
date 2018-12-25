/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package labirynt;

import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author adas
 */
public class VGridFrame {
    
    JFrame jf = null;
    VGrid zoomPanel;
    
    public VGridFrame(ArrayList<ArrayList<Node>> grid, Labirynt labirynt){
        this.jf = new JFrame("Magiczny Labirynt");
        jf.setSize(800, 800);

        JPanel containerPanel = new JPanel();     // extra JPanel 
        containerPanel.setLayout(new GridBagLayout());
        containerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(1, 0, 1)));
        
        zoomPanel = new VGrid(grid);
        containerPanel.add(zoomPanel);
        zoomPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jf.add(new JScrollPane(containerPanel));
        
        jf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                labirynt.onClickStopSearching();
            }
        });
    }
    
    public void showGrid(){
        jf.setVisible(true);         
    }
    
    public boolean hasSelectedStartEnd(){
        return zoomPanel.hasSelectedStartEnd();
    }
    
    public void blockSelectionOfStartEnd(){
        zoomPanel.blockSelectionOfStartEnd();
    }
    
    public int[] getStartEnd(){
        return zoomPanel.getStartEnd();
    }
    
    public void updateGraphics(PriorityQueue<Node> openSet, HashSet<Node> closedSet){
        zoomPanel.updateSets(openSet, closedSet);
        zoomPanel.update(zoomPanel.getGraphics());
    }
    
    public void close(){
        jf.setVisible(false);
        jf.dispose();
    }
}
