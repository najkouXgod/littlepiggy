package com.niko.littlepiggy.screen;

import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen extends BaseScreen {
    public GameOverScreen() {
        System.out.println("DEAD");
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
    }
}
