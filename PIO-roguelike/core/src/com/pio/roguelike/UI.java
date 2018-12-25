package com.pio.roguelike;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.pio.roguelike.actor.Actor;
import com.pio.roguelike.actor.CreateEvent;
import com.pio.roguelike.actor.DamageEvent;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by arvamer on 5/17/16.
 */
public class UI implements Observer {
    double hp_p;
    double hp, max_hp;
    // Rozmiar ekranu
    float width, height;
    BitmapFont font;
    Color bg_color;

    final float hp_bar_width = 130f;
    final float hp_bar_height = 25f;
    final float ui_width = 300f;
    final float ui_height = 40f;
    final float top_offset = 30f;
    final Color text_normal = new Color(0xebcb8bff);
    final Color green = new Color(0xa3be8cff);
    final Color red = new Color(0xbf616aff);

    public UI(int width, int height, BitmapFont font, Color bg_color) {
        this.hp_p = 1;
        this.width = width;
        this.height = height;
        this.font = font;
        this.bg_color = bg_color;
    }

    @Override
    public void update(Observable observable, Object event) {
        Actor actor = (Actor)observable;
        if (event instanceof DamageEvent) {
            hp = ((DamageEvent)event).getCurrentHp();
            update_hp();
        }
        else if (event instanceof CreateEvent) {
            hp = actor.getHp();
            max_hp = actor.getMaxHp();
            update_hp();
        }
    }

    private void update_hp() {
        hp_p = hp / max_hp;
    }

    public void draw() {
        float x = width - ui_width;
        float y = height - ui_height;

        ShapeRenderer shape = new ShapeRenderer();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(bg_color);
        shape.rect(x, y, ui_width, ui_height);
        shape.end();

        float hp_y_pos = height - top_offset + font.getLineHeight();
        Batch batch = new SpriteBatch();
        batch.begin();
        font.setColor(text_normal);
        font.draw(batch, "HP: " + (int)hp + "/" + (int)max_hp, x, hp_y_pos);
        font.setColor(hp_p > 0.3 ? green : red);
        font.draw(batch, progress_bar(hp_p, 20),x + 130f, hp_y_pos);
        batch.end();
    }

    String progress_bar(double progress, int width) {
        int progressi = (int)(width * 2 * progress);
        String s = new String();
        for (int i = 0; i < width * 2 * progress; i += 2) {
            s += '=';
        }
        if (progressi % 2 == 1) {
            s += '-';
        }
        return s;
    }
}
