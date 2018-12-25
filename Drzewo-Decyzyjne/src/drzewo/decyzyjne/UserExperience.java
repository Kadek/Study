/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drzewo.decyzyjne;

import java.util.Scanner;

/**
 *
 * @author adas
 */
public class UserExperience {
    public static void main(String[] args){
        
        System.out.println("------------------------------------------");
        System.out.println("DRZEWO DECYZYJNE");
        System.out.println("------------------------------------------");
        
        System.out.println("Witaj w szczytowym osiągnięciu technologii XXI wieku.");
        System.out.println("Poniżej przedstawione zostały opcje wykorzystania programu.");
        System.out.println("Wybierz jedną...");
        
        
        Scanner sc = new Scanner(System.in);
        do{
            System.out.println("1. Trening");
            System.out.println("2. Predykcja");
            System.out.println("3. Koniec programu");
            
            int opt = sc.nextInt();
            if(opt == 1){
                DrzewoDecyzyjne drzewo = new DrzewoDecyzyjne();
                drzewo.training();
            }else if(opt == 2){
                DrzewoDecyzyjne drzewo = new DrzewoDecyzyjne();
                drzewo.predict();
            }else if(opt == 3){
                break;
            }else{
                System.out.println("Zostałą wpisana zła wartość");
            }
        }while(1==1);
        
        System.out.println("Żegnaj wędrowcze.");
    }
}
