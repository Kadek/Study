/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pio.roguelike;

/**
 *
 * @author rafal
 */
public class Item {
    String name;
    float price;
    int id;

    private static int items_count = 0;

    public Item(String name, float price) {
        this.name = name;
        this.price = price;
        this.id = Item.items_count++;
    }
}
