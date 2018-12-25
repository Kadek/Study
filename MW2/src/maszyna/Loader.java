/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maszyna;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 *
 * @author adam
 */
public class Loader {
       
    JFrame frame;
    private JLabel title, introduction, step1, step2, step3, fileIcon, radioInfo;
    private JTextField selectedFile;
    Checkbox przod;
    Checkbox tyl;
    
    final private String goodIconPath = System.getProperty("user.dir") + "/goodIcon.png";
    final private String badIconPath = System.getProperty("user.dir") + "/badIcon.png";
    private String filePath;
    
    private boolean flag = false;
    private boolean finished = false;
    
    private void addElement(Object o, Container pane, int left, int top, int width, int height){
        Component comp = (Component) o;
        pane.add(comp);
        
        Insets insets = pane.getInsets();
        Dimension size = comp.getPreferredSize();
        comp.setBounds(left + insets.left, top + insets.top,
                        width + size.width, height + size.height);
    }
    
    private void addComponents(Container pane){
        pane.setLayout(null);

        title = new JLabel("MASZYNA WNIOSKUJACA");
        introduction = new JLabel("By przeanalizować dane:");
        step1 = new JLabel("1. Wybierz plik tekstowy z danymi.");
        step2 = new JLabel("2. Wybierz metode wnioskowania.");
        step3 = new JLabel("3. Kliknij przycisk start.");
        
        addElement(title, pane, 120, 10, 200 , 50);
        addElement(introduction, pane, 200, 70, 0 , 0);
        addElement(step1, pane, 160, 100, 0 , 0);
        addElement(step2, pane, 160, 120, 0 , 0);
        addElement(step3, pane, 160, 140, 0 , 0);
        
        Font titleFont = new Font("Serif", Font.BOLD, 24);
        title.setFont(titleFont);
    }
    
    private void addFileDialog(Container pane){
        
        final FileDialog fileDialog = new FileDialog(frame, "Wybierz plik");
        JButton showFileDialogButton = new JButton("Kliknij by wybrac plik");        
        addElement(showFileDialogButton, pane, 60, 300, 0, 0);
        
        selectedFile = new JTextField("C:/");
        addElement(selectedFile, pane, 66, 330, 150, 10);
        
        fileIcon = new JLabel();
        
        showFileDialogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                fileDialog.setVisible(true);
                filePath = fileDialog.getDirectory() + fileDialog.getFile();
                File f = new File(filePath);
                if(f.exists() && f.getName().endsWith(".txt")){
                    fileIcon.setIcon(new ImageIcon(goodIconPath));
                    flag = true;
                }else{
                    fileIcon.setIcon(new ImageIcon(badIconPath));
                    flag = false;
                }

                addElement(fileIcon, pane, 250, 330, 0, 0);
                
                selectedFile.setText(filePath);
            }
        });
        
    }
    
    private void addRadio(Container pane){
        CheckboxGroup boxGroup = new CheckboxGroup();
        
        przod = new Checkbox("Wnioskowanie Przód", boxGroup, true);
        tyl = new Checkbox("Wnioskowanie Tył", boxGroup, false);
        
        radioInfo = new JLabel("Wybierz kierunek wnioskowania.");
        
        addElement(radioInfo, pane, 290, 290, 0, 0);
        addElement(przod, pane, 330, 315 , 200, 20);
        addElement(tyl, pane, 330, 340 , 200, 20);
    }
    
    private void addStart(Container pane){
        JButton startButton = new JButton("START");
        
        addElement(startButton, pane, 220, 400, 50, 20);
        
        startButton.addActionListener(new ActionListener (){
            @Override
            public void actionPerformed(ActionEvent a){
                if(flag){
                    finished = true;
                    frame.dispose();
                    frame.setEnabled(false);
                }
            }
        });
    }
    
    public Loader(){

        frame = new JFrame("Maszyna Wnioskująca");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Container pane = frame.getContentPane();
        
        addComponents(pane);
        addFileDialog(pane);
        addRadio(pane);
        addStart(pane);
        
        Insets insets = frame.getInsets();
        frame.setSize(600 + insets.left + insets.right,
                        500 + insets.top + insets.bottom);
	frame.setVisible(true);
        
    }
    
    public boolean isFinished(){
        return finished;
    }
    
    public String getFilePath(){
        return filePath;
    }
    
    public boolean getSelection(){
        if(przod.getState()){
            return true;
        }else{
            return false;
        }
    }
}
