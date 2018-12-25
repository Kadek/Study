package com.pio.roguelike;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.pio.roguelike.map.ASCIIMap;
import com.pio.roguelike.map.ASCIITextureInfo;
import com.pio.roguelike.actor.Actor;
import com.pio.roguelike.actor.ActorSprite;
import com.pio.roguelike.actor.Death;
import com.pio.roguelike.actor.Move;

import javax.swing.JOptionPane;
import java.util.HashMap;
import java.util.Iterator;

public class Game extends ApplicationAdapter {
    private class KeyActionMapping {
        int key;
        InputAction action;

        KeyActionMapping(int key, InputAction action) {
            this.key = key;
            this.action = action;
        }
    }

    ASCIIMap map;
    ASCIITextureInfo texture_info;
    long prev_time, lag;
    final long UPDATE_TIME_NS = 16666666;
    ActorSprite sprite;
    SpriteBatch batch;
    BitmapFont gdx_font;
    UI ui;
    final Color bg_color = new Color(0x2b303bff);

    // Możemy powiadomić rożne obiekty, ale tylko jeden w danym czasie
    InputListener listener;

    KeyActionMapping[] keyActions = {
        new KeyActionMapping(Input.Keys.W, InputAction.MOVE_UP),
        new KeyActionMapping(Input.Keys.S, InputAction.MOVE_DOWN),
        new KeyActionMapping(Input.Keys.A, InputAction.MOVE_LEFT),
        new KeyActionMapping(Input.Keys.D, InputAction.MOVE_RIGHT),

        new KeyActionMapping(Input.Keys.K, InputAction.MOVE_UP),
        new KeyActionMapping(Input.Keys.J, InputAction.MOVE_DOWN),
        new KeyActionMapping(Input.Keys.H, InputAction.MOVE_LEFT),
        new KeyActionMapping(Input.Keys.L, InputAction.MOVE_RIGHT),

        new KeyActionMapping(Input.Keys.DPAD_UP, InputAction.MOVE_UP),
        new KeyActionMapping(Input.Keys.DPAD_DOWN, InputAction.MOVE_DOWN),
        new KeyActionMapping(Input.Keys.DPAD_LEFT, InputAction.MOVE_LEFT),
        new KeyActionMapping(Input.Keys.DPAD_RIGHT, InputAction.MOVE_RIGHT),

        new KeyActionMapping(Input.Keys.F, InputAction.INTERACT),
    };

    @Override
    public void create() {
        texture_info = new ASCIITextureInfo("ascii/fira_mono_medium_24.sfl");
        map = new ASCIIMap("big", texture_info);
        generateFont();
        ui = new UI(800, 600, gdx_font, bg_color);

        batch = new SpriteBatch();
        Actor actor = new Actor(map, "Player", gdx_font);
        addObservers(actor);

        sprite = actor.getSprite();
        listener = actor;

        prev_time = System.nanoTime();
        lag = 0;
    }

    @Override
    public void render() {
        long current_time = System.nanoTime();
        long elapsed = current_time - prev_time;
        prev_time = current_time;
        lag += elapsed;

        while (lag >= UPDATE_TIME_NS) {
            update();
            lag -= UPDATE_TIME_NS;
        }

        float progress = (float)lag / (float)UPDATE_TIME_NS;
        Gdx.gl.glClearColor(bg_color.r, bg_color.g, bg_color.b, bg_color.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sprite.cameraBegin(progress);

        map.render(sprite.cameraCombined());

        batch.setProjectionMatrix(sprite.cameraCombined());
        batch.begin();

        sprite.update();
        sprite.draw(batch, progress);

        batch.end();
        sprite.cameraEnd(progress);
        ui.draw();
    }

    void update() {
        for (KeyActionMapping mapping : keyActions) {
            if (Gdx.input.isKeyPressed(mapping.key)) {
                listener.execute(mapping.action);
            }
        }

        sprite.update();
    }

    void generateFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Courier Prime.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameters.size = 13;
        gdx_font = generator.generateFont(parameters);
    }

    void addObservers(Actor actor) {
        actor.addObserver((object, event) -> {
            if (event instanceof Death) {
                final String deathMessage = "Do you want your possessions identified?";
                final String deathTitle = "You die!";
                JOptionPane.showMessageDialog(null, deathMessage, deathTitle, JOptionPane.INFORMATION_MESSAGE);
            }
        });

        actor.addObserver((object, event) -> {
            if (event instanceof Move) {
                Move m = (Move)event;

                Iterator<Object> it = map.get_traps().iterator();
                while(it.hasNext()) {
                    HashMap trap = (HashMap) it.next();
                    int x = Integer.parseInt((String)trap.get("x"));
                    int y = Integer.parseInt((String)trap.get("y"));
                    if (m.xPosition == x && m.yPosition == y) {
                        Actor a = (Actor)object;
                        a.damage(33);
                    }
                }
            }
        });
        actor.addObserver(ui);
        actor.created();
    }
}
