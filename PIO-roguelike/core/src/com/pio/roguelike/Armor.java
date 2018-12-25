package com.pio.roguelike;

/**
 *
 * @author rafal
 */
public class Armor extends Item {
    float dmg_reduction;

    public Armor(String name, float price, float red) {
        super(name, price);
        this.dmg_reduction = red;
    }
}
