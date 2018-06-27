/*
 * The MIT License
 *
 * Copyright 2018 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.comboslashersx.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Event;
import com.ray3k.comboslashersx.Core;
import com.ray3k.comboslashersx.Entity;
import com.ray3k.comboslashersx.states.GameState;

/**
 *
 * @author Raymond
 */
public class BarrelEntity extends EnemyEntity {
    private static int MAX_HEALTH = 3;

    public BarrelEntity() {
        super(Core.DATA_PATH + "/spine/barrel.json", "animation", GameState.twoColorPolygonBatch);
        getSkeleton().getRootBone().setScale(.25f);
        setHealth(MAX_HEALTH);
        
        getAnimationState().addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void complete(AnimationState.TrackEntry entry) {
                if (entry.getAnimation().getName().equals("02")) {
                    BarrelEntity.this.dispose();
                }
            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (event.getData().getName().equals("barrel")) {
                    GameState.inst().playSound("barrel");
                }
            }
        });
        
        setDepth(10);
    }

    @Override
    public void hurt() {
        setHealth(getHealth() - 1);
        if (getHealth() > 0) {
            getAnimationState().setAnimation(0, "01", false);
        } else {
            getAnimationState().setAnimation(0, "02", false);
            GameState.inst().addScore(4);
        }
    }
    
    @Override
    public void actSub(float delta) {
        setXspeed(GameState.scrollSpeed);
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
    }

    @Override
    public void actEnd(float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }
}
