package daedalus.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.UUID;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.Root;
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

	public void render(SpriteBatch sb, ShapeRenderer sr, Point p) {
		Color color;
		if(!enabled) color = new Color(0.533f, 0.533f, 0.533f, 1f);
		else if(selected) color = new Color(0.533f, 0.533f, 0.8f, 1f);
		else color = new Color(0.133f, 0.133f, 0.8f, 1f);
		Rectangle size = parent.getDefaultSize();
		
		sr.begin(ShapeType.Filled);
		if(selected) {
			sr.setColor(Color.WHITE);
			sr.rect(p.x - 2.5f, p.y - 3f, size.width + 6, size.height + 6);
		}
		sr.setColor(color);
		sr.rect(p.x, p.y, size.width, size.height);
		sr.end();
		
		sb.begin();
		BitmapFont font = Root.getFont(24);
		font.setColor(Color.WHITE);
		TextBounds bounds = font.getBounds(text);
		font.draw(sb, text, p.x + (size.width - bounds.width) / 2f, p.y + size.height - bounds.height);
		sb.end();
	}
}
