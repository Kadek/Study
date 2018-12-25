package test.java.com.pio.roguelike.map;

import static org.junit.Assert.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.pio.roguelike.map.ASCIIMap;
import com.pio.roguelike.map.ASCIITextureInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by arvamer on 08.06.16.
 */
public class ASCIIMapTest {
    @Test
    public void create() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                ASCIITextureInfo texture_info = new ASCIITextureInfo("ascii/fira_mono_medium_24.sfl");
                ASCIIMap map = new ASCIIMap("test", texture_info);
                byte[] logic_good = {
                        1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                        1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                        1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1,
                        1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                };
                byte[] logic_map = map.get_logic_array();
                assertArrayEquals(logic_good, logic_map);
            }
        }, config);
    }

    @Test
    public void is_valid() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                ASCIITextureInfo texture_info = new ASCIITextureInfo("ascii/fira_mono_medium_24.sfl");
                ASCIIMap map = new ASCIIMap("test", texture_info);
                assertEquals(map.is_valid(1, 0), true);
                assertEquals(map.is_valid(-100, 0), false);
                assertEquals(map.is_valid(100, 0), false);
                assertEquals(map.is_valid(0, 3), false);
            }
        }, config);
    }
}