/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pio.roguelike.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.pio.roguelike.InputAction;
import com.pio.roguelike.map.ASCIIMap;
import java.util.Observable;

/**
 *
 * @author xfix
 */
public class ActorSprite extends Observable {

    int xPosition;
    int yPosition;
    float horizontalVelocity;
    float verticalVelocity;
    int framesToComplete;
    ASCIIMap map;
    Sprite sprite;
    // TODO: Po dodaniu wiekszej liczby aktorów, przenieść kamerę do klasy Player która bedzie dziedziczyła po klasie Actor
    OrthographicCamera camera;

    public ActorSprite(ASCIIMap map) {
        if (map == null) {
            return;
        }
        xPosition = map.start_pos_x();
        yPosition = map.start_pos_y();

        Texture texture = new Texture(Gdx.files.internal("ascii/fira_mono_medium_24.png"));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        sprite = new Sprite(texture, 131, 156, 21, 25);
        sprite.set(sprite);

        this.map = map;

        sprite.translate(map.tile_width() * xPosition, map.tile_height() * (yPosition + 1));

        camera = new OrthographicCamera(800, 600);
        camera.position.set(sprite.getX() + sprite.getWidth() / 2f, sprite.getY() + sprite.getHeight() / 2f, 0);
        camera.update();
    }

    public void execute(InputAction action) {
        int width = xPosition;
        int height = yPosition;
        if (framesToComplete != 0) {
            return;
        }
        switch (action) {
            case MOVE_UP:
                height += 1;
                break;
            case MOVE_DOWN:
                height -= 1;
                break;
            case MOVE_RIGHT:
                width += 1;
                break;
            case MOVE_LEFT:
                width -= 1;
                break;
            default:
                return;
        }
        if (map.is_valid(width, height)) {
            framesToComplete = 15;
            horizontalVelocity = ((float) map.tile_width() / (float) framesToComplete) * (float) (width - xPosition);
            verticalVelocity = ((float) map.tile_height() / (float) framesToComplete) * (float) (height - yPosition);
            xPosition = width;
            yPosition = height;
        }
    }

    public void update() {
        if (framesToComplete > 0) {
            sprite.translate(horizontalVelocity, verticalVelocity);
            camera.translate(horizontalVelocity, verticalVelocity);
            framesToComplete--;
            if (framesToComplete == 0) {
                horizontalVelocity = 0;
                verticalVelocity = 0;
                setChanged();
                notifyObservers(new Move(xPosition, yPosition));
            }
        }
    }
    
    public void draw(SpriteBatch batch, float progress) {
        sprite.translate(horizontalVelocity * progress, verticalVelocity * progress);
        sprite.draw(batch);
        sprite.translate(-horizontalVelocity * progress, -verticalVelocity * progress);
    }

    public Matrix4 cameraCombined() { return camera.combined; }

    public void cameraBegin(float progress) {
        camera.translate(horizontalVelocity * progress, verticalVelocity * progress);
        camera.update();
    }

    public void cameraEnd(float progress) {
        camera.translate(-horizontalVelocity * progress, -verticalVelocity * progress);
    }
}
