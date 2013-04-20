package daedalus.main;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class GameContext extends InputAdapter {
	public abstract void init();
	public abstract void tick();
	public abstract boolean isTransparent();
	public abstract void render(SpriteBatch sb, ShapeRenderer sr);
}
