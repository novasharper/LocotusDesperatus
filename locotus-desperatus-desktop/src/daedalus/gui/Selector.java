package daedalus.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.Root;
import daedalus.input.GamepadEvent;

public class Selector implements IInput {
	/**
	 * List of event listeners for callbacks
	 */
	private LinkedList<EventListener> eventHandlers;
	/**
	 * Size of text for rendering
	 */
	private Rectangle2D.Float textSize;
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
	/**
	 * Label for selector
	 */
	private String label;
	/**
	 * Width of arrows
	 */
	private int charWidth = -1;
	/**
	 * Index of selected element
	 */
	private int selectedIndex = -1;
	/**
	 * ID of element
	 */
	public final String id;
	/**
	 * List of elements
	 */
	private List<String> elements;
	
	/**
	 * "Pretty" constructor
	 * @param label Label for selector
	 * @param elements Syntactical sugar var-args list of selector elements
	 */
	public Selector(String label, String... elements) {
		this(label, Arrays.asList(elements));
	}
	
	/**
	 * Constructor for selector
	 * @param label Label for selector
	 * @param elements List of selector elements
	 */
	public Selector(String label, List<String> elements) {
		this.label = label;
		selected = false;
		enabled = true;
		this.elements = elements;
		if(this.elements.size() > 0) selectedIndex = 0;
		this.id = UUID.randomUUID().toString();
	}
	
	/**
	 * Set parent of element
	 */
	public void addTo(Menu m) {
		parent = m;
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
	 * Move to left element
	 */
	public void moveLeft() {
		selectedIndex--; // Go backwards
		// Wrap around
		if(selectedIndex < 0) selectedIndex += elements.size();
		// Call out to all listeners
		Event ev = new Event(Event.SELECTOR_INDEX_CHANGE, id, Event.SELECTOR_LEFT, this);
		if(null != eventHandlers)
			for(EventListener handler : eventHandlers) handler.handleEvent(ev);
	}
	
	/**
	 * Move to right element
	 */
	public void moveRight() {
		// Go forwards and wrap
		selectedIndex = (selectedIndex + 1) % elements.size();
		// Call out to all listeners
		Event ev = new Event(Event.SELECTOR_INDEX_CHANGE, id, Event.SELECTOR_RIGHT, this);
		if(null != eventHandlers)
			for(EventListener handler : eventHandlers) handler.handleEvent(ev);
	}
	
	/**
	 * Handle keyboard input event
	 */
	public void handleInput(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.VK_LEFT) {
			moveLeft();
		} else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
			moveRight();
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
	 * Get index of selected element
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	/**
	 * Get selected element
	 */
	public String getSelected() {
		return elements.get(selectedIndex);
	}

	public void handleInput(GamepadEvent ev) {
		
	}
	
	public void render(SpriteBatch sb, ShapeRenderer sr, Point p) {
		Color color;
		if(!enabled) color = new Color(0.533f, 0.533f, 0.533f, 1f);
		else if(selected) color = new Color(0.533f, 0.533f, 0.8f, 1f);
		else color = new Color(0.133f, 0.133f, 0.8f, 1f);
		Rectangle size = parent.getDefaultSize();
		
		BitmapFont font = Root.getFont(24);
		TextBounds lBound = font.getBounds(label);
		float width = lBound.width;
		float height = lBound.height;
		
		float charWidth = font.getBounds("<").width;
		
		sr.begin(ShapeType.Filled);
		sr.setColor(Color.WHITE);
		sr.rect(p.x - 2.5f, p.y - 3f, size.width + 6, size.height + 6);
		sr.setColor(color);
		sr.rect(p.x, p.y, size.width, size.height);
		if(selected) {
			sr.setColor(Color.WHITE);
			sr.rect(p.x + (10 + charWidth), p.y, 3f, size.height);
			sr.rect(p.x + size.width - (10 + charWidth + 3), p.y, 3f, size.height);
		}
		if(!label.isEmpty()) {
			sr.setColor(new Color(0.5f, 0.75f, 1.0f, 0.5f));
			sr.rect(p.x - width - 30, p.y, width + 20, size.height);
		}
		sr.end();
		
		String selectedElement = "";
		if(0 <= selectedIndex && !elements.isEmpty()) {
			selectedElement = elements.get(selectedIndex);
		}
		
		sb.begin();
		font.setColor(Color.WHITE);
		font.draw(sb, selectedElement, p.x + 40, p.y + size.height - height / 2);
		font.draw(sb, "<", p.x + 5, p.y + size.height - height / 2);
		font.draw(sb, ">", p.x + size.width - 4 - charWidth, p.y + size.height - height / 2);
		if(!label.isEmpty()) {
			font.draw(sb, label, p.x - width - 20, p.y + size.height - height / 2);
		}
		sb.end();
	}
}
