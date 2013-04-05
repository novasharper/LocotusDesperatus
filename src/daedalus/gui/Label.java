package daedalus.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.UUID;

public class Label implements IGuiComponent {
	/**
	 * Cached text size
	 */
	private Rectangle2D.Float textSize;
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
	 * Render element.
	 */
	public void render(Graphics2D gr, Point location) {
		// Cache text size
		if(null == textSize) {
			textSize = (Rectangle2D.Float) gr.getFontMetrics().getStringBounds(text, gr);
			textSize.height -= 2 * gr.getFontMetrics().getAscent();
		}
		// Draw background
		gr.setColor(new Color(0x2222cc));
		Rectangle size = this.parent.getDefaultSize();
		gr.fillRect(location.x, location.y, size.width, size.height);
		
		// Draw label
		gr.setColor(Color.white);
		gr.drawString(this.text, location.x + (int) (size.width - textSize.getWidth()) / 2,
				location.y + (int) (size.height - textSize.getHeight()) / 2);
	}

	/**
	 * Set parent of element
	 */
	public void addTo(Menu m) {
		this.parent = m;
	}
	
	public void setText(String text) {
		if(!text.equals(this.text)) textSize = null;
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
