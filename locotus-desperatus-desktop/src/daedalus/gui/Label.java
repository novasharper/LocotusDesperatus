package daedalus.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.UUID;

import com.badlogic.gdx.graphics.Color;
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
		Color color = new Color(0.133f, 0.133f, 0.8f, 1f);
		Rectangle size = parent.getDefaultSize();
		
		sr.begin(ShapeType.Filled);
		sr.setColor(color);
		sr.rect(p.x, p.y, size.width, size.height);
		sr.end();
		
		sb.begin();
		BitmapFont font = Root.getFont(36);
		font.setColor(Color.WHITE);
		TextBounds bounds = font.getBounds(text);
		font.draw(sb, text, p.x + (size.width - bounds.width) / 2f, p.y + size.height - bounds.height / 2);
		sb.end();
	}
}
