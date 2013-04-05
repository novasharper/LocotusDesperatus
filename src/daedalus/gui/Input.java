package daedalus.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
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
import daedalus.input.GamepadEvent.EventType;
import daedalus.input.IGamepadEventHandler;
import daedalus.main.GameComponent;
import daedalus.settings.GamepadMapping;

public class Input implements IInput, IGamepadEventHandler {
	/**
	 * List of event listeners for callbacks
	 */
	private LinkedList<EventListener> eventHandlers;
	/**
	 * Size of text for rendering
	 */
	private Rectangle2D.Float textSize;
	/* Current inputed text, Label of input. */
	private String text, label;
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
	/**
	 * Font metrics to measure inputed text
	 */
	private FontMetrics textMeasure;
	/**
	 * Used to input text with gamepad
	 */
	private DaisyInput gamepadInput;
	
	/**
	 * Create input.
	 * @param label Text for input label.
	 */
	public Input(String label) {
		// Set label text
		this.label = label;
		// Initialize starting text to be empty
		this.text = "";
		// Generate random ID
		this.id = UUID.randomUUID().toString();
		// Enable component
		this.enabled = true;
		this.gamepadInput = new DaisyInput();
	}

	/**
	 * Set parent of element
	 */
	public void addTo(Menu m) {
		this.parent = m;
	}
	
	/**
	 * Render element.
	 */
	public void render(Graphics2D gr, Point location) {
		// Get font metrics for menu font
		if(null == textMeasure) {
			textMeasure = gr.getFontMetrics(parent.getFont());
		}
		// Get measurements to center text render (if needed)
		if(null == textSize) {
			textSize = (Rectangle2D.Float) gr.getFontMetrics(parent.getFont())
					.getStringBounds(label, (Graphics) gr);
			textSize.height -= 2 * gr.getFontMetrics(parent.getFont()).getAscent();
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
		Rectangle size = this.parent.getDefaultSize();
		gr.fillRoundRect(location.x, location.y, size.width, size.height, 10, 10);

		gr.setColor(Color.white);
		Stroke save = gr.getStroke();
		gr.setStroke(new BasicStroke(2.5f));
		gr.drawRoundRect(location.x, location.y, size.width, size.height, 10, 10);
		gr.setStroke(save);
		
		// Draw label
		if(!label.isEmpty()) {
			gr.setColor(new Color(0.5f, 0.75f, 1.0f, 0.5f));
			gr.fillRect(location.x - (int) textSize.getWidth() - 30, location.y, (int) textSize.getWidth() + 20, size.height);
			gr.setColor(Color.white);
			gr.drawString(this.label, location.x - (int) textSize.getWidth() - 20,
					location.y + (int) (size.height - textSize.getHeight()) / 2);
		}
		// \u2588
		gr.drawString(this.text + (selected && (System.currentTimeMillis() % 1500 > 750) ? "|" : ""), location.x + 10,
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
	 * Determine if input character is valid
	 * @param input Inputed character
	 * @return Whether or not it is valid
	 */
	private boolean isValidCharacter(char input) {
		if(	'A' <= input && input <= 'Z' ||
			'a' <= input && input <= 'z' ||
			'0' <= input && input <= '9' ||
			" .,;:-?!".contains("" + input)) return true;
		return false;
	}
	
	/**
	 * Handle keyboard input event
	 */
	public void handleInput(KeyEvent event) {
		char input = event.getKeyChar();
		Event ev = new Event(Event.TEXT_VALUE_CHANGE, id, Event.NOTHING, this);
		if((input == 8 || input == 127) && text.length() > 0) {
			text = text.substring(0, text.length() - 1);
			if(null != eventHandlers)
				for(EventListener handler : eventHandlers) handler.handleEvent(ev);
		}
		// Only add character if it is valid and text will fit
		if(isValidCharacter(input) &&
				textMeasure.getStringBounds(text + input + "|", null).getWidth()
				<= this.parent.getDefaultSize().width - 15) {
			text += input;
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
	 * Get inputed text
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Update text. Useful for gamepads because of DaisyInput.
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * Handle gamepad input
	 */
	public void handleInput(GamepadEvent ev) {
		if(!GameComponent.getGame().isActive(parent)) return;
		if(ev.getType() == EventType.RELEASED && ev.getButtonID() == GamepadMapping.instance().X) {
			GameComponent.getGame().pushContext(gamepadInput);
		}
	}
}
