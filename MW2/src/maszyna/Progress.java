/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maszyna;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class Progress {
    String filePath;
    boolean tryb;
    
    private JFrame frame;
    public JTable dane, reguly, cele, log;
    HashMap tables = new HashMap<>();
    
    DataSelector dataSelector;
    Wnioskowanie wnioskowanie;
    Container pane;
    
    private void addElement(Object o, Container pane, int left, int top, int width, int height){
        Component comp = (Component) o;
        pane.add(comp);
        
        Insets insets = pane.getInsets();
        Dimension size = comp.getPreferredSize();
        comp.setBounds(left + insets.left, top + insets.top,
                        width + size.width, height + size.height);
    }
    
    public Progress(String filePath, boolean tryb){
        this.filePath = filePath;
        this.tryb = tryb;
        dataSelector = new DataSelector(filePath);
        
        if(tryb){
            wnioskowanie = new WnioskowaniePrzod();
        }else{
            wnioskowanie = new WnioskowanieTyl();
        }
    }
    
    private void drawDane(Container pane){
        String[] columnNames = {"Nazwa danej",
                                "Wartość"};
       
        dane = new JTable(new DefaultTableModel(columnNames, 0));
        dane.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
        TableColumn columnA = dane.getColumn("Nazwa danej");
        columnA.setMinWidth(110);
        columnA.setMaxWidth(110);
        TableColumn columnB = dane.getColumn("Wartość");
        columnB.setMinWidth(70);
        columnB.setMaxWidth(70);
        
        JScrollPane scrollPane = new JScrollPane(dane);
        addElement(scrollPane, pane, 50, 50, -270, -100);
    }
    
    private void drawReguly(Container pane){
        String[] columnNames = {"Lewa strona reguły",
                              "Prawa strona",
                              "Wartość"};
       
        reguly = new JTable(new DefaultTableModel(columnNames, 0));
        reguly.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
        TableColumn columnA = reguly.getColumn("Lewa strona reguły");
        columnA.setMinWidth(200);
        columnA.setMaxWidth(200);
        TableColumn columnB = reguly.getColumn("Prawa strona");
        columnB.setMinWidth(110);
        columnB.setMaxWidth(110);
        TableColumn columnC = reguly.getColumn("Wartość");
        columnC.setMinWidth(70);
        columnC.setMaxWidth(70);
        
        JScrollPane scrollPane = new JScrollPane(reguly);
        addElement(scrollPane, pane, 270, 50, -70, -100);
        
    }
    
    private void drawCele(Container pane){
        String[] columnNames = {"Nazwa celu",
                                "Wartość"};
       
        cele = new JTable(new DefaultTableModel(columnNames, 0));
        cele.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
        TableColumn columnA = cele.getColumn("Nazwa celu");
        columnA.setMinWidth(110);
        columnA.setMaxWidth(110);
        TableColumn columnB = cele.getColumn("Wartość");
        columnB.setMinWidth(70);
        columnB.setMaxWidth(70);
        
        JScrollPane scrollPane = new JScrollPane(cele);
        addElement(scrollPane, pane, 690, 50, -270, -100);
    }  
    
    private void drawLog(Container pane){
        String[] columnNames = {"Log"};
                
        log = new JTable(new DefaultTableModel(columnNames, 0));
        
        JScrollPane scrollPane = new JScrollPane(log);
        addElement(scrollPane, pane, 50, 400, 370, -250);
    }
    
    private void drawExecuteButton(Container pane){
        JButton executeButton = new JButton("Wnioskuj");
        
        addElement(executeButton, pane, 880, 460, 15, 20);
        executeButton.addActionListener(new ActionListener(){
           @Override
           public void actionPerformed(ActionEvent e){
                addElementToLog("Rozpoczynam wnioskowanie");
                wnioskowanie.run();
           }
        });
    }
    
    private void drawFrame(){
        
        frame = new JFrame("Maszyna Wnioskująca");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
                
        Insets insets = frame.getInsets();
        frame.setSize(1000 + insets.left + insets.right,
                        650 + insets.top + insets.bottom);
	frame.setVisible(true);
    }
    
    public void run() throws IOException{
        drawFrame();
        pane = frame.getContentPane();
        
        drawDane(pane);
        drawReguly(pane);
        drawCele(pane);
        drawLog(pane);   
        drawExecuteButton(pane);
        
        try {
            dataSelector.addDane(dane, log);
            dataSelector.addReguly(reguly, log);
            dataSelector.addCele(cele, log);
        } catch (IOException ex) {
            Logger.getLogger(Progress.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        tables.put("dane", dane);
        tables.put("reguly", reguly);
        tables.put("cele", cele);
        tables.put("log", log);
        wnioskowanie.setTables(tables);
    }
    
    public void addElementToLog(String element){
        DefaultTableModel model = (DefaultTableModel) log.getModel();
        model.addRow(new Object[] {element});
    }
}
