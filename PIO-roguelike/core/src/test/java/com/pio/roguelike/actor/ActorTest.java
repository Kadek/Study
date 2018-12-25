/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.com.pio.roguelike.actor;

import com.pio.roguelike.InputAction;
import com.pio.roguelike.Item;
import com.pio.roguelike.ItemContainer;
import com.pio.roguelike.Weapon;
import com.pio.roguelike.actor.Actor;
import com.pio.roguelike.actor.ActorSprite;
import com.pio.roguelike.actor.CreateEvent;
import java.util.Observable;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

/**
 *
 * @author Konrad Borowski <xfix at protonmail.com>
 */
public class ActorTest {
    Actor instance;
    
    @Before
    public void setUp() {
        instance = new Actor(null, "Test actor", null);
    }

    /**
     * Test of execute method, of class Actor.
     */
    @org.junit.Test(expected = NullPointerException.class)
    public void testExecute() {
        System.out.println("execute");
        InputAction action = InputAction.MOVE_UP;
        instance.execute(action);
    }

    /**
     * Test of damage method, of class Actor.
     */
    @org.junit.Test
    public void testDamage() {
        System.out.println("damage");
        instance.damage(50);
        assertEquals("HP was reduced", instance.getHp(), 50.0, 0);
        instance.damage(30);
        assertEquals("HP was reduced once again", instance.getHp(), 20.0, 0);
    }

    /**
     * Test of addItem method, of class Actor.
     */
    @org.junit.Test(expected = NullPointerException.class)
    public void testAddItem() {
        System.out.println("addItem");
        // Nie kupisz go ;)
        Item item = new Weapon("Excalibur", Float.NaN, 19f, 91f, 1.2f);
        instance.addItem(item);
        instance.itemContainer().showItems();
    }

    /**
     * Test of itemContainer method, of class Actor.
     */
    @org.junit.Test
    public void testItemContainer() {
        System.out.println("itemContainer");
        ItemContainer result = instance.itemContainer();
        assertThat("Got ItemContainer", result, instanceOf(ItemContainer.class));
    }

    /**
     * Test of created method, of class Actor.
     */
    @org.junit.Test
    public void testCreated() {
        System.out.println("created");
        instance.addObserver((Observable observable, Object object) -> {
            assertThat("Got CreateEvent", object, instanceOf(CreateEvent.class));
        });
        instance.created();
    }

    /**
     * Test of getSprite method, of class Actor.
     */
    @org.junit.Test
    public void testGetSprite() {
        System.out.println("getSprite");
        ActorSprite result = instance.getSprite();
        assertThat("Got a sprite", result, instanceOf(ActorSprite.class));
    }

    /**
     * Test of getMaxHp method, of class Actor.
     */
    @org.junit.Test
    public void testGetMaxHp() {
        System.out.println("getMaxHp");
        double result = instance.getMaxHp();
        assertEquals("Got max HP", result, 100.0, 0);
    }

    /**
     * Test of getHp method, of class Actor.
     */
    @org.junit.Test
    public void testGetHp() {
        System.out.println("getHp");
        double result = instance.getHp();
        assertEquals("Got HP", result, 100.0, 0);
    }
    
}
