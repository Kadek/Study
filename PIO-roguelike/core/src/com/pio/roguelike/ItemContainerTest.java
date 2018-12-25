/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pio.roguelike;

/**
 *
 * @author Mateusz
 */
public class ItemContainerTest {
        
    Item miecz = new Item("miecz", 5000 );
    Item tarcza = new Item("tarcza", 2000 );
    Item skrzynia = new Item("skrzynia", 500 );
    
    ItemContainer kontener = new ItemContainer(null);
    
    
    public void addItemTest() {
        kontener.addItem(miecz);
        kontener.addItem(tarcza);
        kontener.addItem(skrzynia);
        assert kontener.itemList.size() == 3;
    }
    
}
