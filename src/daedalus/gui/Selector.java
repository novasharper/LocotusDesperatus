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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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
	 * Render element.
	 */
	public void render(Graphics2D gr, Point location) {
		// Get measurements to center text render (if needed)
		if(null == textSize) {
			textSize = (Rectangle2D.Float) gr.getFontMetrics(parent.getFont()).getStringBounds(label, (Graphics) gr);
			textSize.height -= 2 * gr.getFontMetrics(parent.getFont()).getAscent();
		}
		// Get width of '<'
		if(-1 == charWidth) {
			charWidth = (int) gr.getFontMetrics(parent.getFont()).getStringBounds("<", (Graphics) gr).getWidth();
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
		// Draw background of component
		gr.fillRoundRect(location.x, location.y, size.width, size.height, 10, 10);
		
		// Set color for drawing border
		gr.setColor(Color.white);
		Stroke save = gr.getStroke();
		gr.setStroke(new BasicStroke(2.5f));
		// Draw border
		gr.drawRoundRect(location.x, location.y, size.width, size.height, 10, 10);
		// If selected, draw border between right & left arrows and text
		if(selected) {
			gr.drawLine(location.x + 10 + charWidth, location.y,
					location.x + 10 + charWidth, location.y + size.height);
			gr.drawLine(location.x + size.width - 10 - charWidth, location.y,
					location.x + size.width - 10 - charWidth, location.y + size.height);
		}
		// Reset stroke to default
		gr.setStroke(save);
		
		// Draw label
		if(!label.isEmpty()) {
			// Draw background of label
			gr.setColor(new Color(0.5f, 0.75f, 1.0f, 0.5f));
			gr.fillRect(location.x - (int) textSize.getWidth() - 30, location.y, (int) textSize.getWidth() + 20, size.height);
			// Draw label
			gr.setColor(Color.white);
			gr.drawString(this.label, location.x - (int) textSize.getWidth() - 20,
					location.y + (int) (size.height - textSize.getHeight()) / 2);
		}
		
		// get selected element
		String selectedElement = "";
		if(0 <= selectedIndex && !elements.isEmpty()) {
			selectedElement = elements.get(selectedIndex);
		}
		
		// Draw arrows and selected element
		gr.drawString(selectedElement, location.x + 30,
				location.y + (int) (size.height - textSize.getHeight()) / 2);
		gr.drawString("<", location.x + 5,
				location.y + (int) (size.height - textSize.getHeight()) / 2);
		gr.drawString(">", location.x + size.width - 4 - charWidth,
				location.y + (int) (size.height - textSize.getHeight()) / 2);
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
}
