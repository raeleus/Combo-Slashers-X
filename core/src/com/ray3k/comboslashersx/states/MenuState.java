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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.comboslashersx.Core;
import com.ray3k.comboslashersx.State;
import static com.ray3k.comboslashersx.states.GameState.CharacterChoice.KNIGHT;
import static com.ray3k.comboslashersx.states.GameState.CharacterChoice.NINJA;
import com.ray3k.comboslashersx.states.GameState.Move;

public class MenuState extends State {
    private Stage stage;
    private Skin skin;
    private Table root;

    public MenuState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        skin = Core.assetManager.get(Core.DATA_PATH + "/ui/Combo Slashers X.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        Gdx.input.setInputProcessor(stage);
        
        createMenu();
    }
    
    private void createMenu() {
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        root.defaults().space(10.0f);
        Image image = new Image(skin, "logo");
        image.setScaling(Scaling.fit);
        root.add(image);
        
        root.defaults().minWidth(150.0f).minHeight(75.0f);
        root.row();
        TextButton textButton = new TextButton("Play", skin);
        root.add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Core.assetManager.get(Core.DATA_PATH + "/sfx/coin.wav", Sound.class).play(1.0f);
                showDialog();
            }
        });
        
        root.row();
        textButton = new TextButton("Quit", skin);
        root.add(textButton);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(100 / 255.0f, 100 / 255.0f, 100 / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void act(float delta) {
        stage.act(delta);
    }

    @Override
    public void dispose() {
        
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    private void showDialog() {
        boolean disabled = false;
        final Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    showCharacterSelect();
                }
            }
        };
        dialog.setFillParent(true);
        Table table = dialog.getContentTable();
        
        Label label = new Label("Combos", skin);
        table.add(label).colspan(5);
        
        table.row();
        for (int i = 0; i < 3; i++) {
            final ImageButton imageButton = new ImageButton(skin, "list");
            
            switch (GameState.combo1.get(i)) {
                case UP:
                    imageButton.setStyle(skin.get("list-up", ImageButtonStyle.class));
                    break;
                case DOWN:
                    imageButton.setStyle(skin.get("list-down", ImageButtonStyle.class));
                    break;
                case LEFT:
                    imageButton.setStyle(skin.get("list-left", ImageButtonStyle.class));
                    break;
                case RIGHT:
                    imageButton.setStyle(skin.get("list-right", ImageButtonStyle.class));
                    break;
                default:
                    disabled = true;
                    break;
            }
            
            table.add(imageButton);
            
            final int index = i;
            imageButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    dialog.hide();
                    showButtonDialog(GameState.combo1, index, new Runnable() {
                        @Override
                        public void run() {
                            showDialog();
                        }
                    });
                }
            });
        }
        
        table.row();
        for (int i = 0; i < 5; i++) {
            final ImageButton imageButton = new ImageButton(skin, "list");
            
            switch (GameState.combo2.get(i)) {
                case UP:
                    imageButton.setStyle(skin.get("list-up", ImageButtonStyle.class));
                    break;
                case DOWN:
                    imageButton.setStyle(skin.get("list-down", ImageButtonStyle.class));
                    break;
                case LEFT:
                    imageButton.setStyle(skin.get("list-left", ImageButtonStyle.class));
                    break;
                case RIGHT:
                    imageButton.setStyle(skin.get("list-right", ImageButtonStyle.class));
                    break;
                default:
                    disabled = true;
                    break;
            }
            
            table.add(imageButton);
            
            final int index = i;
            imageButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    dialog.hide();
                    showButtonDialog(GameState.combo2, index, new Runnable() {
                        @Override
                        public void run() {
                            showDialog();
                        }
                    });
                }
            });
        }
        
        table.row();
        for (int i = 0; i < 5; i++) {
            final ImageButton imageButton = new ImageButton(skin, "list");
            
            switch (GameState.combo3.get(i)) {
                case UP:
                    imageButton.setStyle(skin.get("list-up", ImageButtonStyle.class));
                    break;
                case DOWN:
                    imageButton.setStyle(skin.get("list-down", ImageButtonStyle.class));
                    break;
                case LEFT:
                    imageButton.setStyle(skin.get("list-left", ImageButtonStyle.class));
                    break;
                case RIGHT:
                    imageButton.setStyle(skin.get("list-right", ImageButtonStyle.class));
                    break;
                default:
                    disabled = true;
                    break;
            }
            
            table.add(imageButton);
            
            final int index = i;
            imageButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    dialog.hide();
                    showButtonDialog(GameState.combo3, index, new Runnable() {
                        @Override
                        public void run() {
                            showDialog();
                        }
                    });
                }
            });
        }
        
        dialog.show(stage);
        dialog.key(Keys.ESCAPE, false).key(Keys.ENTER, true);
        
        dialog.getButtonTable().defaults().minWidth(100.0f);
        dialog.getButtonTable().pad(15.0f);
        TextButton textButton = new TextButton("OK", skin, "small");
        textButton.setDisabled(disabled);
        dialog.button(textButton, true).button("Cancel", false, skin.get("small", TextButtonStyle.class));
    }
    
    public void showButtonDialog(final Array<Move> combo, final int index, final Runnable runnable) {
        final Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                runnable.run();
            }
        };
        dialog.setFillParent(true);
        Table table = dialog.getContentTable();
        
        ImageButton imageButton = new ImageButton(skin, "up");
        table.add(imageButton);
        
        imageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                combo.set(index, Move.UP);
                dialog.hide();
                runnable.run();
            }
        });
        
        imageButton = new ImageButton(skin, "down");
        table.add(imageButton);
        
        imageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                combo.set(index, Move.DOWN);
                dialog.hide();
                runnable.run();
            }
        });
        
        imageButton = new ImageButton(skin, "left");
        table.add(imageButton);
        
        imageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                combo.set(index, Move.LEFT);
                dialog.hide();
                runnable.run();
            }
        });
        
        imageButton = new ImageButton(skin, "right");
        table.add(imageButton);
        
        imageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                combo.set(index, Move.RIGHT);
                dialog.hide();
                runnable.run();
            }
        });
        
        dialog.show(stage);
        
        dialog.key(Keys.ESCAPE, null);
    }
    
    private void showCharacterSelect() {
        Dialog dialog = new Dialog("", skin);
        dialog.setFillParent(true);
        Table table = dialog.getContentTable();
        
        table.defaults().space(50.0f);
        ImageButton imageButton = new ImageButton(skin, "ninja");
        table.add(imageButton);
        
        imageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                GameState.character = NINJA;
                Core.stateManager.loadState("game");
                Core.assetManager.get(Core.DATA_PATH + "/sfx/coin.wav", Sound.class).play(1.0f);
            }
        });
        
        imageButton = new ImageButton(skin, "knight");
        table.add(imageButton);
        
        imageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                GameState.character = KNIGHT;
                Core.stateManager.loadState("game");
                Core.assetManager.get(Core.DATA_PATH + "/sfx/coin.wav", Sound.class).play(1.0f);
            }
        });
        
        dialog.show(stage);
        
        dialog.getButtonTable().defaults().minWidth(100.0f);
        dialog.getButtonTable().pad(15.0f);
        dialog.button("Cancel", false, skin.get("small", TextButtonStyle.class));
    }
}