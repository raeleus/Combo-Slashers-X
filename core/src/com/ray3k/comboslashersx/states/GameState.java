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
package com.ray3k.comboslashersx.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.ray3k.comboslashersx.Core;
import com.ray3k.comboslashersx.EntityManager;
import com.ray3k.comboslashersx.InputManager;
import com.ray3k.comboslashersx.ScrollingTiledDrawable;
import com.ray3k.comboslashersx.State;
import com.ray3k.comboslashersx.entities.BarrelEntity;
import com.ray3k.comboslashersx.entities.CarEntity;
import com.ray3k.comboslashersx.entities.CrateEntity;
import com.ray3k.comboslashersx.entities.EnemyEntity;
import com.ray3k.comboslashersx.entities.GameOverTimerEntity;
import com.ray3k.comboslashersx.entities.PlayerEntity;
import java.text.DecimalFormat;

public class GameState extends State {
    private static GameState instance;
    private float score;
    private static float highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    public static EntityManager entityManager;
    public static TextureAtlas spineAtlas;
    public static TwoColorPolygonBatch twoColorPolygonBatch;
    public static final float GAME_WIDTH = 800;
    public static final float GAME_HEIGHT = 600;
    public static enum Move {
        UP, DOWN, LEFT, RIGHT, NONE
    }
    public static Array<Move> combo1 = new Array<Move>(new Move[] {Move.UP, Move.UP, Move.UP});
    public static Array<Move> combo2 = new Array<Move>(new Move[] {Move.UP, Move.UP, Move.UP, Move.UP, Move.UP});
    public static Array<Move> combo3 = new Array<Move>(new Move[] {Move.UP, Move.UP, Move.UP, Move.UP, Move.UP});
    public static enum CharacterChoice {
        NINJA, KNIGHT
    }
    public static CharacterChoice character;
    public static ScrollingTiledDrawable bg;
    public static float scrollSpeed;
    public static final float DEFAULT_SCROLL_SPEED = -150.0f;
    public static PlayerEntity player;
    private DecimalFormat df;
    
    public static GameState inst() {
        return instance;
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        instance = this;
        
        spineAtlas = Core.assetManager.get(Core.DATA_PATH + "/spine/Combo Slashers X.atlas", TextureAtlas.class);
        
        score = 15.0f;
        
        inputManager = new InputManager();
        
        gameCamera = new OrthographicCamera();
//        gameCamera.position.set(0.0f, 0.0f, 0.0f);
        gameViewport = new StretchViewport(GAME_WIDTH, GAME_HEIGHT, gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        gameViewport.apply();
        
        skin = Core.assetManager.get(Core.DATA_PATH + "/ui/Combo Slashers X.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        
        entityManager = new EntityManager();
        
        twoColorPolygonBatch = new TwoColorPolygonBatch(3100);
        
        df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        df.setMinimumFractionDigits(1);
        
        createStageElements();
        
        bg = new ScrollingTiledDrawable(spineAtlas.findRegion("street"));
        scrollSpeed = DEFAULT_SCROLL_SPEED;
        
        player = new PlayerEntity();
        player.setPosition(300.0f, 100.0f);
        entityManager.addEntity(player);
        
        updateComboDisplay();
        
        loadLevel("level1");
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Label label = new Label(df.format(score), skin, "small");
        label.setName("score");
        root.add(label).pad(10.0f);
        
        root.row();
        Table table = new Table();
        table.setName("combo");
        table.padBottom(100.0f);
        root.add(table).expand();
    }
    
    public void updateComboDisplay() {
        Table table = stage.getRoot().findActor("combo");
        table.clear();
        table.defaults().space(30.0f);
        
        Array<Move> combo = player.getTargetCombo();
        int index = player.getComboIndex();
        
        if (combo != null && index < combo.size) {
            for (int i = 0; i < combo.size; i++) {
                Move move = combo.get(i);
                if (i >= index) {
                    if (move == Move.UP) {
                        Image image = new Image(skin, "image-up");
                        table.add(image);
                    } else if (move == Move.DOWN) {
                        Image image = new Image(skin, "image-down");
                        table.add(image);
                    } else if (move == Move.LEFT) {
                        Image image = new Image(skin, "image-left");
                        table.add(image);
                    } else if (move == Move.RIGHT) {
                        Image image = new Image(skin, "image-right");
                        table.add(image);
                    }
                } else {
                    if (move == Move.UP) {
                        Image image = new Image(skin, "image-up-selected");
                        table.add(image);
                    } else if (move == Move.DOWN) {
                        Image image = new Image(skin, "image-down-selected");
                        table.add(image);
                    } else if (move == Move.LEFT) {
                        Image image = new Image(skin, "image-left-selected");
                        table.add(image);
                    } else if (move == Move.RIGHT) {
                        Image image = new Image(skin, "image-right-selected");
                        table.add(image);
                    }
                }
            }
        }
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0 / 255.0f, 146 / 255.0f, 69 / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        bg.draw(spriteBatch, 0.0f, 0.0f, GAME_WIDTH, GAME_HEIGHT);
        
        spriteBatch.end();
        
        twoColorPolygonBatch.setProjectionMatrix(gameCamera.combined);
        twoColorPolygonBatch.begin();
        twoColorPolygonBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        entityManager.draw(null, delta);
        twoColorPolygonBatch.end();
        
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        entityManager.draw(spriteBatch, delta);
        
        spriteBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        bg.setOffsetX(bg.getOffsetX() + scrollSpeed * delta);
        
        if (score > 0) {
            CarEntity car = entityManager.get(CarEntity.class);
            if (car.getHealth() > 0) {
                addScore(-delta);
            }
        } else {
            if (entityManager.get(GameOverTimerEntity.class) == null) {
                setScore(0);
                entityManager.addEntity(new GameOverTimerEntity(1.0f));
            }
        }
        
        entityManager.act(delta);
        
        stage.act(delta);
        
        if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            Core.stateManager.loadState("menu");
        }
        
    }

    @Override
    public void dispose() {
        if (twoColorPolygonBatch != null) {
            twoColorPolygonBatch.dispose();
        }
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, false);
        stage.getViewport().update(width, height, true);
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
        if (score > highscore) {
            highscore = score;
        }
        
        Label label = stage.getRoot().findActor("score");
        label.setText(df.format(this.score));
    }
    
    public void addScore(float score) {
        this.score += score;
        if (this.score > highscore) {
            highscore = this.score;
        }
        
        Label label = stage.getRoot().findActor("score");
        label.setText(df.format(this.score));
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }
    
    public void playSound(String name) {
        playSound(name, 1.0f, 1.0f);
    }
    
    public void playSound (String name, float volume) {
        playSound(name, volume, 1.0f);
    }
    
    /**
     * 
     * @param name
     * @param volume
     * @param pitch .5 to 2. 1 is default
     */
    public void playSound(String name, float volume, float pitch) {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/" + name + ".wav", Sound.class).play(volume, pitch, 0.0f);
    }
    
    public void loadLevel(String name) {
        FileHandle file = Gdx.files.local(Core.DATA_PATH + "/data/" + name + ".txt");
        String string = file.readString();
        float position = GAME_WIDTH + 100;
        for (String item : string.split(",")) {
            if (item != null) {
                if (item.equals("barrel")) {
                    BarrelEntity barrel = new BarrelEntity();
                    barrel.setPosition(position, 100.0f);
                    entityManager.addEntity(barrel);
                } else if (item.equals("crate")) {
                    CrateEntity crate = new CrateEntity();
                    crate.setPosition(position, 100.0f);
                    entityManager.addEntity(crate);
                } else if (item.equals("car")) {
                    CarEntity car = new CarEntity();
                    car.setPosition(position, 100.0f);
                    entityManager.addEntity(car);
                }
            }
            position += 200.0f;
        }
    }
}