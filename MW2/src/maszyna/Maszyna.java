/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maszyna;

import java.io.IOException;

/**
 *
 * @author adam
 */
public class Maszyna {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        Loader loader = new Loader();
        boolean flag = false;
        while(!flag){
            flag = loader.isFinished();
            try {
                Thread.sleep(100);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Ścieżka do danych : " + loader.getFilePath());
        System.out.println("Wybrany tryb : " + loader.getSelection());
        
        Progress progress = new Progress(loader.getFilePath(), loader.getSelection());
        progress.run();
    }
    
}
