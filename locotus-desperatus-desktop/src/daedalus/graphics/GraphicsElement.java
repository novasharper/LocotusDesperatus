package daedalus.graphics;

import java.awt.Graphics2D;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface GraphicsElement {
	public void render(SpriteBatch sb, ShapeRenderer sr);
	public void tick();
}
