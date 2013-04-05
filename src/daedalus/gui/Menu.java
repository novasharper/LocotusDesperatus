package daedalus.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import daedalus.input.GamepadEvent;
import daedalus.input.GamepadEvent.ComponentType;
import daedalus.input.GamepadEvent.EventType;
import daedalus.input.IGamepadEventHandler;
import daedalus.main.GameComponent;
import daedalus.main.GameContext;


public class Menu extends GameContext implements IGamepadEventHandler {
	/* List of gui components */
	private LinkedList<IGuiComponent> guiComponents;
	/* List of inputs */
	private LinkedList<IInput> inputs;
	/* Font for menu */
	private Font menuFont;
	/* Selected index */
	private int selectedIndex;
	/* Whether selected index has changed */
	private boolean selectedIndexChange = true;
	/* Pointer to selected component */
	private IInput selectedComponent;
	/* Default size of components */
	private Rectangle componentSize;
	/* Background image. */
	private Image background;
	/* Width, height, x, and y of background image. */
	private int bgWidth, bgHeight, bgX, bgY;
	/* Background color for menu, overridden by background image. */
	private Color bgColor;
	/* Specify whether or not to use custom font. */
	private static final boolean useCustomFont = false;
	
	/**
	 * Initialize menu
	 */
	public Menu() {
		// Create lists of inputs and gui elements
		inputs = new LinkedList<IInput>();
		guiComponents = new LinkedList<IGuiComponent>();
		// Set default component size
		componentSize = new Rectangle(300, 50);
		// Set background color
		bgColor = Color.black;
		menuFont = new Font("Dialog.plain", Font.BOLD, 28);
		if(useCustomFont) {
			// Try to create font from included font file
			InputStream fontStream = GameComponent.class.getClassLoader().getResourceAsStream("daedalus/res/label.ttf");
			try {
				menuFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(28f).deriveFont(Font.BOLD);
			} catch(Exception e) {
			}
		}
	}
	
	public void init() {
		if(GameComponent.getGamePad() != null) {
			GameComponent.getGamePad().addEventHandler(this);
		}
	}
	
	/**
	 * Render menu
	 */
	public void render(Graphics2D gr) {
		// Save graphics values
		Font saveFont = gr.getFont();
		Color saveBG = gr.getBackground();
		Color saveColor = gr.getColor();
		// Render background
		if(null == background) {
			gr.setColor(bgColor);
			gr.fillRect(0, 0, GameComponent.getGame().width, GameComponent.getGame().height);
		} else {
			gr.drawImage(background, bgX, bgY, bgWidth, bgHeight, GameComponent.getDrawCanvas());
		}
		// Update font
		if(null != menuFont) gr.setFont(menuFont);
		// Render all gui elements
		Point location = new Point((GameComponent.getGame().width - componentSize.width) / 2 + 50, 30);
		for(IGuiComponent guiComponent : guiComponents) {
			guiComponent.render(gr, location);
			location.translate(0, componentSize.height + 10);
		}
		// Reset any changed graphics values
		gr.setColor(saveColor);
		gr.setBackground(saveBG);
		gr.setFont(saveFont);
	}
	
	/**
	 * Tick
	 */
	public void tick() {
		// Cache selected component
		if(selectedComponent == null) selectedComponent = inputs.get(selectedIndex);
		// Process all key events that have happened
		LinkedList<KeyEvent> unprocessedKeyEvents = GameComponent.getGame().getKeyEventQueue();
		while(!unprocessedKeyEvents.isEmpty()) {
			// Get event
			KeyEvent event = unprocessedKeyEvents.remove();
			// Only process key release events
			if(!(event.getID() == KeyEvent.KEY_RELEASED)) continue;
			// Move down one element
			if(event.getKeyCode() == KeyEvent.VK_DOWN) {
				selectedIndex = (selectedIndex + 1) % inputs.size();
				if(!inputs.get(selectedIndex).isEnabled())
					selectedIndex = (selectedIndex + 1) % inputs.size();
				selectedIndexChange = true;
			}
			// Move up one element
			else if(event.getKeyCode() == KeyEvent.VK_UP) {
				selectedIndex = (selectedIndex - 1 + inputs.size()) % inputs.size();
				if(!inputs.get(selectedIndex).isEnabled())
					selectedIndex = (selectedIndex - 1 + inputs.size()) % inputs.size();
				selectedIndexChange = true;
			}
			// Have selected component process event
			else {
				if(null != selectedComponent)
					selectedComponent.handleInput(event);
			}
		}
		// Update selected element
		if(selectedIndexChange) {
			// Iterate through all elements
			int index = 0;
			for(IInput component : inputs) {
				// If element is the one selected, mark it selected and update
				// Selected element pointer
				if(index == selectedIndex) {
					component.select();
					selectedComponent = component;
				}
				// Otherwise, mark it not selected
				else component.deselect();
				index++;
			}
			// Selected index has not changed
			selectedIndexChange = false;
		}
	}
	
	/**
	 * Menu is not transparent (nothing can be rendered or updated behind it)
	 */
	public boolean isTransparent() {
		return false;
	}
	
	/**
	 * Get default component size
	 */
	public Rectangle getDefaultSize() {
		return componentSize;
	}
	
	/**
	 * Add component to menu
	 * @param component Componrnt to add
	 */
	public void addComponent(IGuiComponent component) {
		component.addTo(this); // Register parent
		// If component is an input, add it to inputs
		if(component instanceof IInput) inputs.add((IInput) component);
		// Add component to components
		guiComponents.add(component);
	}
	
	/**
	 * Get menu font
	 */
	public Font getFont() {
		return menuFont;
	}
	
	/**
	 * Set background image for menu
	 * @param newBackground Background image
	 */
	public void setBackground(Image newBackground) {
		this.background = newBackground; // Set background
		// Get image dimensions
		int imgWidth = background.getWidth(GameComponent.getDrawCanvas()),
			imgHeight = background.getHeight(GameComponent.getDrawCanvas());
		// Get image aspect ratio
		double imgAspect = (1.0 * imgWidth) / imgHeight;
		// Get display aspect ratio
		double displayAspect = (1.0 * GameComponent.getGame().width) / GameComponent.getGame().height;
		// If game aspect ratio is wider than picture aspect ratio, scale image to fit width of menu
		if(displayAspect > imgAspect) {
			bgWidth = GameComponent.getGame().width;
			bgHeight = GameComponent.getGame().width * imgHeight / imgWidth;
		}
		// Otherwise, scale it to fit height
		else {
			bgHeight = GameComponent.getGame().height;
			bgWidth = GameComponent.getGame().height * imgWidth / imgHeight;
		}
		// Set top left corner of background
		bgY = (GameComponent.getGame().height - bgHeight) / 2;
		bgX = (GameComponent.getGame().width - bgWidth) / 2;
	}
	
	/**
	 * Set background color
	 * @param newColor Background color
	 */
	public void setBackgroundColor(Color newColor) {
		if(null != newColor) {
			bgColor = newColor;
		}
	}
	public void handleInput(GamepadEvent ev) {
		if(!GameComponent.getGame().isActive(this)) return;
		if(ev.getType() == EventType.RELEASED && ev.getCType() == ComponentType.DPAD) {
			int dir = ev.getButtonID();
			if(dir == 0) {
				selectedIndex = (selectedIndex - 1 + inputs.size()) % inputs.size();
				if(!inputs.get(selectedIndex).isEnabled())
					selectedIndex = (selectedIndex - 1 + inputs.size()) % inputs.size();
				selectedIndexChange = true;
			} else if(dir == 4) {
				selectedIndex = (selectedIndex + 1) % inputs.size();
				if(!inputs.get(selectedIndex).isEnabled())
					selectedIndex = (selectedIndex + 1) % inputs.size();
				selectedIndexChange = true;
			}
		}
		if(selectedComponent != null) selectedComponent.handleInput(ev);
	}
}
