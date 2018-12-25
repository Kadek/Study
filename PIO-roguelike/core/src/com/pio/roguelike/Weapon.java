package com.pio.roguelike;

/**
 *
 * @author rafal
 */
public class Weapon extends Item{
    float v1;
    float v2;
    float speed;

    public Weapon(String name, float price, float dmg_min, float dmg_max, float speed) {
        super(name, price);
        this.v1 = dmg_min;
        this.v2 = dmg_max;
        this.speed = speed;
    }
}
