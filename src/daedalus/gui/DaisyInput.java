package daedalus.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import daedalus.input.Gamepad;
import daedalus.input.GamepadEvent;
import daedalus.input.GamepadEvent.ComponentType;
import daedalus.input.GamepadEvent.EventType;
import daedalus.input.IGamepadEventHandler;
import daedalus.main.GameComponent;
import daedalus.main.GameContext;
import daedalus.settings.GamepadMapping;

public class DaisyInput extends GameContext implements IGamepadEventHandler {
	// Current input text
	private String text = "";
	// Index of selected petal
	private int selected;
	// Colors of each button
	private Color[] buttonColors = {new Color(0x14457f), new Color(0xb38d1e), new Color(0xa51c0a), new Color(0x69910a)};
	// Colors for use in drawing DaisyInput 
	private Color
			selectedWheel = new Color(0x385b6f),
			wheel = new Color(0x213846),
			background = new Color(0x1d303e),
			charColor = new Color(0xbabfc2);
	// Cache of petals when none are selected
	private BufferedImage cache;
	// Whether cache needs to be redrawn (optimization for how slow java2d is)
	private boolean cacheOutdated = true;
	// Allowed letters
	private String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ?!,\\&.";
	// Radius of each petal and distance from center of petal to center of daisy input
	private double r, dist; 
	private int cx, cy; // Center of daisy input
	
	// X = 0, Y = 1, B = 2, A = 3
	private HashMap<Integer, Integer> buttonMap; // Map buttons to letter
	// Only init once
	private boolean initDone = false;
	
	public void init() {
		if(!initDone) {
			r = 50;
			dist = r / Math.sin(Math.PI * 2 / 16);
			cache = new BufferedImage((int) (2 * dist + 3 * r), (int) (2 * dist + 3 * r), BufferedImage.TYPE_INT_ARGB);
			cx = GameComponent.getGame().width / 2;
			cy = GameComponent.getGame().height - cache.getHeight() / 2 - 10;
			GameComponent.getGamePad().addEventHandler(this);
			buttonMap = new HashMap<Integer, Integer>();
			buttonMap.put(GamepadMapping.instance().X, 0);
			buttonMap.put(GamepadMapping.instance().Y, 1);
			buttonMap.put(GamepadMapping.instance().A, 3);
			buttonMap.put(GamepadMapping.instance().B, 2);
			initDone = true;
		}
	}
	/**
	 * Generate image cache
	 */
	private void genCache(Font font) {
		System.out.println(font.getFamily() + " " + font.getFontName());
		Graphics2D cache_gr = (Graphics2D) cache.getGraphics();
		cache_gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		cache_gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		cache_gr.setColor(background);
		cache_gr.fillOval(0, 0, (int) (2 * dist + 3 * r), (int) (2 * dist + 3 * r));
		cache_gr.setColor(wheel);
		cache_gr.fillOval((int) (3 * r / 2), (int) (3 * r / 2), (int) (2 * dist), (int) (2 * dist));
		cache_gr.setFont(font.deriveFont(24f).deriveFont(Font.BOLD));
		// Draw each petal
		for(int petal = 0; petal < 8; petal++) {
			cache_gr.setColor(wheel);
			double x = r / 2 + (1 + Math.cos((2 - petal) * Math.PI / 4)) * dist;
			double y = dist + r / 2 - Math.sin((2 - petal) * Math.PI / 4) * dist;
			// Fill background
			cache_gr.fillOval((int) (x),
					(int) (y),
					(int) (r * 2), (int) (r * 2));
			// Draw each letter
			for(int charIndex = 0; charIndex < 4; charIndex++) {
				Rectangle2D b = cache_gr.getFontMetrics().getStringBounds("" + chars.charAt(petal * 4 + charIndex), cache_gr);
				cache_gr.setColor(charColor);
				cache_gr.drawString(chars.charAt(petal * 4 + charIndex) + "",
						(float) (x + Math.cos((charIndex - 2) * Math.PI / 2) * r / 2 + r - b.getWidth() / 2),
						(float) (y + Math.sin((charIndex - 2) * Math.PI / 2) * r / 2 + r + b.getHeight() / 4));
			}
		}
	}
	/**
	 * Render
	 */
	public void render(Graphics2D gr) {
		Font f = gr.getFont();
		// Generate cache if necessary
		if(cacheOutdated) {
			genCache(f);
			cacheOutdated = false;
		}
		// Draw cached wheel
		gr.drawImage(cache, cx - cache.getWidth() / 2, cy - cache.getHeight() / 2,
				cache.getWidth(), cache.getHeight(), GameComponent.getDrawCanvas());
		// Draw text so far
		gr.setFont(f.deriveFont(36f).deriveFont(Font.BOLD));
		gr.setColor(Color.black);
		Rectangle2D sb = gr.getFontMetrics().getStringBounds(text, gr);
		gr.drawString(text, cx - (float) sb.getWidth() / 2, 50 + (float) sb.getHeight() / 2);
		if(selected != -1) {
			// Update font for drawing selected wheel
			gr.setFont(f.deriveFont(24f).deriveFont(Font.BOLD));
			// Draw selected wheel
			gr.setColor(selectedWheel);
			double x = cx + Math.cos((2 - selected) * Math.PI / 4) * dist - r;
			double y = cy - Math.sin((2 - selected) * Math.PI / 4) * dist - r;
			gr.fillOval((int) (x),
					(int) (y),
					(int) (r * 2), (int) (r * 2));
			// Draw each button
			for(int charIndex = 0; charIndex < 4; charIndex++) {
				Rectangle2D b = gr.getFontMetrics().getStringBounds("" + chars.charAt(selected * 4 + charIndex), gr);
				gr.setColor(buttonColors[charIndex]);
				gr.fillOval((int) (x + Math.cos((charIndex - 2) * Math.PI / 2) * r / 2 + r - b.getHeight() / 2),
						(int) (y + Math.sin((charIndex - 2) * Math.PI / 2) * r / 2 + r - b.getHeight() / 2),
						(int) b.getHeight(), (int) b.getHeight());
				gr.setColor(charColor);
				gr.drawString(chars.charAt(selected * 4 + charIndex) + "",
						(float) (x + Math.cos((charIndex - 2) * Math.PI / 2) * r / 2 + r - b.getWidth() / 2),
						(float) (y + Math.sin((charIndex - 2) * Math.PI / 2) * r / 2 + r + b.getHeight() / 4));
			}
		}
		gr.setFont(f);
	}
	// How long since last 
	private int bsTick;
	public void tick() {
		Gamepad gamepad = GameComponent.getGamePad();
		selected = gamepad.getDPad();
		if(gamepad.pollButton(GamepadMapping.instance().BACKSPACE)) {
			if(text.length() > 0 && bsTick == 0)
				text = text.substring(0, text.length() - 1);
			bsTick = (bsTick + 1) % 12;
		}
		else bsTick = 0;
	}
	/**
	 * Handle gamepad input
	 */
	public void handleInput(GamepadEvent ev) {
		if(!GameComponent.getGame().isActive(this)) return;
		int buttonID = ev.getButtonID();
		EventType type = ev.getType();
		Gamepad gamepad = ev.getSender();
		if(type == EventType.RELEASED && ev.getCType() == ComponentType.BUTTON) {
			if(buttonID < 4) {
				if(selected >= 0) {
					String concat = (chars.charAt(selected * 4 + buttonMap.get(buttonID)) + "");
					if(Math.abs(gamepad.pollTrigger()) < 0.5f) concat = concat.toLowerCase();
					text += concat;
				}
			} else if(buttonID == GamepadMapping.instance().SPACE) {
				text += " ";
			} else if(buttonID == GamepadMapping.instance().START) {
				GameComponent.getGame().popContext();
			}
		}
	}
	/**
	 * Components cannot be updated/rendered in background
	 */
	public boolean isTransparent() {
		return false;
	}
	/**
	 * Get inputed text
	 */
	public String getText() {
		return text;
	}
	/**
	 * Clear input
	 */
	public void clear() {
		text = "";
	}
	public static void main(String[] args) {
		GameComponent.create("Daisywheel", 800, 600, true);
		GameComponent gc = GameComponent.getGame();
		gc.pushContext(new DaisyInput());
		gc.setPaused(false);
		gc.start();
	}
}
