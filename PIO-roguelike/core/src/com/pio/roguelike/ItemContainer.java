package com.pio.roguelike;

import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.pio.roguelike.Item;

public class ItemContainer {
    ArrayList<Item> itemList;
    BitmapFont font;
    final Color bg_color = new Color(0x343d46ff);
    final Color text_color = new Color(0xeff1f5ff);
    final Color text_color_sp = new Color(0xebcb8bff);

   public ItemContainer(BitmapFont font) {
        itemList = new ArrayList();
        this.font = font;
    }
    
    public void addItem(Item item){
        itemList.add(item);
    }
    
    public void removeItem(Item item) { itemList.remove(item); }
    
    public void showItems(){
        float width = 300;
        float height = (font.getLineHeight() + 5) * itemList.size() + 20f;
        
        float start_w = 250f;
        float start_h = 300f - height / 2f;

        ShapeRenderer shape = new ShapeRenderer();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(bg_color);
        shape.rect(start_w, start_h, width, height);
        shape.end();

        float yoffset = 10f;
        for (Item item : itemList) {
            Batch batch = new SpriteBatch();
            batch.begin();
            String item_type = new String();
            if (item instanceof Weapon) {
                item_type = "W";
            }
            else if (item instanceof Armor) {
                item_type = "A";
            }
            else {
                item_type = "?";
            }
            font.setColor(text_color_sp);
            font.draw(batch, item_type, start_w + 10f, start_h + height - yoffset);
            font.setColor(text_color);
            font.draw(batch, item.name, start_w + 40f, start_h + height - yoffset);
            yoffset += 5f + font.getLineHeight();
            batch.end();
        }
    }
}
