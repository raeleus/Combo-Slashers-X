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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Event;
import com.ray3k.comboslashersx.Core;
import com.ray3k.comboslashersx.Entity;
import com.ray3k.comboslashersx.SpineTwoColorEntity;
import com.ray3k.comboslashersx.states.GameState;
import com.ray3k.comboslashersx.states.GameState.Move;

/**
 *
 * @author Raymond
 */
public class PlayerEntity extends SpineTwoColorEntity {
    public static enum Mode {
        RUNNING, STANDING, ATTACKING
    }
    private Mode mode;
    private Array<Move> moveQueue;
    private EnemyEntity targetEnemy;
    private Array<Move> targetCombo;
    private int comboIndex;
    private boolean releasedKeys;

    public PlayerEntity() {
        setTwoColorPolygonBatch(GameState.twoColorPolygonBatch);
        if (GameState.character == GameState.CharacterChoice.NINJA) {
            setSkeletonData(Core.DATA_PATH + "/spine/ninja.json", "run");
        } else {
            setSkeletonData(Core.DATA_PATH + "/spine/knight.json", "run");
        }
        
        getSkeleton().getRootBone().setScale(.25f);
        
        mode = Mode.RUNNING;
        
        getAnimationState().getData().setDefaultMix(.09f);
        getAnimationState().getData().setMix("run", "stand", .25f);
        
        getAnimationState().addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void complete(AnimationState.TrackEntry entry) {
                if (mode == Mode.ATTACKING) {
                    if (comboIndex >= targetCombo.size) {
                        mode = Mode.RUNNING;
                        getAnimationState().addAnimation(0, "run", true, 1.0f);
                        targetEnemy.hurt();
                        targetEnemy = null;
                    } else {
                        mode = Mode.STANDING;
                        if (targetEnemy != null) {
                            targetEnemy.hurt();
                        }
                    }
                }
            }

            @Override
            public void start(AnimationState.TrackEntry entry) {
                if (entry.getAnimation().getName().equals("run")) {
                    GameState.scrollSpeed = GameState.DEFAULT_SCROLL_SPEED;
                }
            }
            
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (event.getData().getName().equals("sword")) {
                    GameState.inst().playSound("sword", .25f, MathUtils.random(.5f, 1.0f));
                } else if (event.getData().getName().equals("step")) {
                    GameState.inst().playSound("step", .1f);
                } else if (event.getData().getName().equals("woosh")) {
                    GameState.inst().playSound("woosh");
                }
            }
        });
        
        moveQueue = new Array<Move>();
        releasedKeys = true;
        comboIndex = 0;
    }

    @Override
    public void actSub(float delta) {
        if (releasedKeys) {
            if (Gdx.input.isKeyPressed(Keys.UP)) {
                moveQueue.add(Move.UP);
                releasedKeys = false;
            } else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
                moveQueue.add(Move.DOWN);
                releasedKeys = false;
            } else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
                moveQueue.add(Move.LEFT);
                releasedKeys = false;
            } else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
                moveQueue.add(Move.RIGHT);
                releasedKeys = false;
            }
        } else {
            if (!Gdx.input.isKeyPressed(Keys.UP) && !Gdx.input.isKeyPressed(Keys.DOWN) && !Gdx.input.isKeyPressed(Keys.LEFT) && !Gdx.input.isKeyPressed(Keys.RIGHT)) {
                releasedKeys = true;
            }
        }
        
        if (mode == Mode.RUNNING) {
            for (EnemyEntity enemy : GameState.entityManager.getAll(EnemyEntity.class)) {
                if (enemy.getHealth() > 0 && enemy.getSkeletonBounds().aabbIntersectsSkeleton(getSkeletonBounds())) {
                    enemy.setMotion(0.0f, 0.0f);
                    getAnimationState().setAnimation(0, "stand", false);
                    GameState.scrollSpeed = 0.0f;
                    mode = Mode.STANDING;
                    targetEnemy = enemy;
                    moveQueue.clear();
                    comboIndex = 0;
                    
                    if (enemy.getHealth() == 3) {
                        targetCombo = GameState.combo1;
                    } else {
                        if (MathUtils.randomBoolean()) {
                            targetCombo = GameState.combo2;
                        } else {
                            targetCombo = GameState.combo3;
                        }
                    }
                    
                    GameState.inst().updateComboDisplay();
                    break;
                }
            }
        } else if (mode == Mode.STANDING) {    
            if (moveQueue.size > 0) {
                if (targetCombo.get(comboIndex) == moveQueue.first()) {
                    if (comboIndex < targetCombo.size - 1) {
                        if (moveQueue.first() == Move.UP) {
                            getAnimationState().setAnimation(0, "slash-up", false);
                        } else if (moveQueue.first() == Move.DOWN) {
                            getAnimationState().setAnimation(0, "slash-down", false);
                        } else if (moveQueue.first() == Move.LEFT) {
                            getAnimationState().setAnimation(0, "spin", false);
                        } else if (moveQueue.first() == Move.RIGHT) {
                            getAnimationState().setAnimation(0, "stab", false);
                        }
                        
                        moveQueue.removeIndex(0);
                    } else {
                        if (moveQueue.first() == Move.UP) {
                            getAnimationState().setAnimation(0, "slash-up-hard", false);
                        } else if (moveQueue.first() == Move.DOWN) {
                            getAnimationState().setAnimation(0, "slash-down-hard", false);
                        } else if (moveQueue.first() == Move.LEFT) {
                            getAnimationState().setAnimation(0, "spin-hard", false);
                        } else if (moveQueue.first() == Move.RIGHT) {
                            getAnimationState().setAnimation(0, "stab-hard", false);
                        }
                        
                        moveQueue.clear();
                    }
                    
                    mode = Mode.ATTACKING;
                    comboIndex++;
                    GameState.inst().updateComboDisplay();
                } else {
                    moveQueue.clear();
                    GameState.inst().playSound("wrong");
                    GameState.inst().addScore(-1.0f);
                }
            }
        }
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

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Array<Move> getTargetCombo() {
        return targetCombo;
    }

    public int getComboIndex() {
        return comboIndex;
    }
}
