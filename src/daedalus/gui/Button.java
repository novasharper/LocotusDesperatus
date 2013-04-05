package daedalus.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.UUID;

import daedalus.input.GamepadEvent;
import daedalus.input.GamepadEvent.ComponentType;
import daedalus.input.GamepadEvent.EventType;
import daedalus.main.GameComponent;
import daedalus.settings.GamepadMapping;

public class Button implements IInput {
	/**
	 * List of event listeners for callbacks
	 */
	private LinkedList<EventListener> eventHandlers;
	/**
	 * Size of text for rendering
	 */
	private Rectangle2D.Float textSize;
	/**
	 * Text to be displayed on button
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
	 * Whether or not input is selected
	 */
	private boolean selected;
	/**
	 * Whether or not input is enabled
	 */
	private boolean enabled;
	
	public Button(String text) {
		this.text = text;
		this.id = UUID.randomUUID().toString();
		this.enabled = true;
	}
	
	/**
	 * Render element.
	 */
	public void render(Graphics2D gr, Point location) {
		// Get measurements to center text render (if needed)
		if(null == textSize) {
			textSize = (Rectangle2D.Float) gr.getFontMetrics().getStringBounds(text, (Graphics) gr);
			textSize.height -= 2 * gr.getFontMetrics().getAscent();
		}
		// Grey out component if it is disabled
		if(!enabled)
			gr.setColor(new Color(0x888888));
		// Make component more pale if it is selected
		else if(selected)
			gr.setColor(new Color(0x8888cc));
		// Default color
		else
			gr.setColor(new Color(0x2222cc));
		// Get size of component
		Rectangle size = this.parent.getDefaultSize();
		// Fill button background
		gr.fillRoundRect(location.x, location.y, size.width, size.height, 10, 10);
		// Draw button label
		gr.setColor(Color.white);
		gr.drawString(this.text, location.x + (int) (size.width - textSize.getWidth()) / 2,
				location.y + (int) (size.height - textSize.getHeight()) / 2);
		// If button is selected, draw border
		if(selected) {
			gr.setColor(Color.white);
			Stroke save = gr.getStroke();
			gr.setStroke(new BasicStroke(2.5f));
			gr.drawRoundRect(location.x, location.y, size.width, size.height, 10, 10);
			gr.setStroke(save);
		}
	}

	/**
	 * Set parent of element
	 */
	public void addTo(Menu m) {
		this.parent = m;
	}
	
	/**
	 * Mark element as selected
	 */
	public void select() {
		selected = true;
	}
	
	/**
	 * Mark element as not selected
	 */
	public void deselect() {
		selected = false;
	}
	
	/**
	 * Determine if element is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Set whether or not element is enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Handle keyboard input event
	 */
	public void handleInput(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.VK_ENTER) {
			Event ev = new Event(Event.BUTTON_ACTIVATE, id, Event.NOTHING, this);
			if(null != eventHandlers)
				for(EventListener handler : eventHandlers) handler.handleEvent(ev);
		}
	}
	
	/**
	 * Add event listener
	 * @param listener
	 */
	public void addEventListener(EventListener listener) {
		if(null == eventHandlers) eventHandlers = new LinkedList<EventListener>();
		eventHandlers.add(listener);
	}
	
	/**
	 * Handle gamepad input
	 */
	public void handleInput(GamepadEvent ev) {
		if(ev.getType() == EventType.RELEASED && ev.getCType() == ComponentType.BUTTON
				&& ev.getButtonID() == GamepadMapping.instance().X) {
			Event event = new Event(Event.BUTTON_ACTIVATE, id, Event.NOTHING, this);
			if(null != eventHandlers)
				for(EventListener handler : eventHandlers) handler.handleEvent(event);
		}
	}
}
