/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pio.roguelike.actor;

/**
 *
 * @author xfix
 */
public class DamageEvent {
    private final double damage;
    private final double current_hp;
    
    public DamageEvent(double damage, double hp) {
        this.damage = damage;
        this.current_hp = hp;
    }

    /**
     * @return the damage
     */
    public double getDamage() { return damage; }
    public double getCurrentHp() { return current_hp; }
}
