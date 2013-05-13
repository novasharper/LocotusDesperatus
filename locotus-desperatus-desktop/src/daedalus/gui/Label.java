package daedalus.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.Root;

public class Label implements IGuiComponent {
	/**
	 * Text of label
	 */
	private String text;
	/**
	 * ID of element
	 */
	public final String id;
	/**
	 * Parent menu for element
	 */
	private Menu parent;
	
	/**
	 * Create label
	 * @param text Text of label
	 */
	public Label(String text) {
		// Set label text
		this.text = text;
		// Create unique ID (mabe used later)
		this.id = UUID.randomUUID().toString();
	}

	/**
	 * Set parent of element
	 */
	public void addTo(Menu m) {
		this.parent = m;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void render(SpriteBatch sb, ShapeRenderer sr, Point p) {
		Color color = new Color(0.5f, 0.5f, 1f, 0.4f);
		Rectangle size = parent.getDefaultSize();
		
		BitmapFont font = Root.getFont(36);
		font.setColor(Color.WHITE);
		TextBounds bounds = font.getBounds(text);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		sr.begin(ShapeType.Filled);
		sr.setColor(color);
		sr.rect(p.x + (size.width - bounds.width) / 2f - 10f, p.y, bounds.width + 20f, size.height);
		sr.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
		sb.begin();
		font.draw(sb, text, p.x + (size.width - bounds.width) / 2f, p.y + size.height - bounds.height / 2);
		sb.end();
	}
}
