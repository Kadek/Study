/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.com.pio.roguelike.map;

import com.pio.roguelike.map.ASCIITextureInfo;
import com.pio.roguelike.map.CharMetric;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author adam
 */
public class ASCIITextureInfoTest {
    
    
    public ASCIITextureInfoTest() {
    }

    /**
     * Test of get method, of class ASCIITextureInfo.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        char c = 'A';
        ASCIITextureInfo instance = new ASCIITextureInfo("/home/adam/PIO/core/assets/ascii/fira_mono_medium_24.sfl");
        CharMetric result = instance.get(c);
        System.out.println(result);
        assertEquals(instance.char_metrics.get(c), result);
    }

    /**
     * Test of char_width method, of class ASCIITextureInfo.
     */
    @Test
    public void testChar_width() {
        int char_width = 19;
        System.out.println("char_width");
        ASCIITextureInfo instance = new ASCIITextureInfo("/home/adam/PIO/core/assets/ascii/fira_mono_medium_24.sfl");
        int result = instance.char_width();
        assertEquals(char_width, result);
    }

    /**
     * Test of char_height method, of class ASCIITextureInfo.
     */
    @Test
    public void testChar_height() {
        int char_height = 38;
        System.out.println("char_height");
        ASCIITextureInfo instance = new ASCIITextureInfo("/home/adam/PIO/core/assets/ascii/fira_mono_medium_24.sfl");
        int result = instance.char_height();
        assertEquals(char_height, result);
    }
    
}
