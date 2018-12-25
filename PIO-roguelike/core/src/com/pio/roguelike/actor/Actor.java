package com.pio.roguelike.actor;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.pio.roguelike.InputAction;
import com.pio.roguelike.InputListener;
import com.pio.roguelike.Item;
import com.pio.roguelike.ItemContainer;
import com.pio.roguelike.map.ASCIIMap;
import java.util.ArrayList;
import java.util.Observable;

public class Actor extends Observable implements InputListener {
    private final ActorSprite sprite;
    private final String name;
    private double hp = 100;
    private double maxHp = 100;
    private int strength = 10;
    private int agility = 10;
    // Use ItemContainer class when implemented
    private final ItemContainer items;
    
    public Actor(ASCIIMap map, String name, BitmapFont font) {
        items = new ItemContainer(font);
        sprite = new ActorSprite(map);
        sprite.addObserver((actorSprite, event) -> {
            setChanged();
            notifyObservers(event);
        });
        this.name = name;
    }

    @Override
    public void execute(InputAction action) {
        getSprite().execute(action);
    }
    
    public void damage(double damage) {
        if (hp == 0) return;
        setChanged();
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            notifyObservers(new DamageEvent(damage, hp));
            setChanged();
            notifyObservers(new Death());
        } else {
            notifyObservers(new DamageEvent(damage, hp));
        }
    }
    
    public void addItem(Item item) {
        items.addItem(item);
        setChanged();
        notifyObservers(new ItemObtained(item));
    }

    public ItemContainer itemContainer() { return items; }

    /// Notify all observers that actor was created
    public void created() {
        setChanged();
        notifyObservers(new CreateEvent());
    }

    /**
     * @return the sprite
     */
    public ActorSprite getSprite() { return sprite; }
    public double getMaxHp() { return  maxHp; }
    public double getHp() { return  hp; }

}
