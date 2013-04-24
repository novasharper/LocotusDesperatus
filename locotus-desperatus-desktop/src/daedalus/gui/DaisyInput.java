package daedalus.gui;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.Root;
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
	// Allowed letters
	private String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ?!,\\&.";
	// Radius of each petal and distance from center of petal to center of daisy input
	float r, R, dist;
	private long wait;
	
	private static Color[] buttonColors = {
		new Color(0.078f, 0.271f, 0.498f, 1f),
		new Color(0.702f, 0.553f, 0.118f, 1f),
		new Color(0.647f, 0.11f, 0.039f, 1f),
		new Color(0.412f, 0.569f, 0.039f, 1f)
	};
	private static Color
		selectedWheel = new Color(0.22f, 0.357f, 0.435f, 1f),
		wheel = new Color(0.129f, 0.22f, 0.275f, 1f),
		background = new Color(0.114f, 0.188f, 0.243f, 1f),
		charColor = new Color(0.729f, 0.749f, 0.761f, 1f);
	
	// X = 0, Y = 1, B = 2, A = 3
	private HashMap<Integer, Integer> buttonMap; // Map buttons to letter
	// Only init once
	private boolean initDone = false;
	private Input parent;
	
	public DaisyInput() {
		this(null);
	}
	public DaisyInput(Input parent) {
		this.parent = parent;
	}
	
	public void init() {
		if(!initDone) {
			r = 50;
			dist = r / (float) Math.sin(Math.PI * 2 / 16);
			R = (2 * dist + 3 * r) / 2f;
			GameComponent.getGamePad().addEventHandler(this);
			buttonMap = new HashMap<Integer, Integer>();
			buttonMap.put(GamepadMapping.instance().X, 0);
			buttonMap.put(GamepadMapping.instance().Y, 1);
			buttonMap.put(GamepadMapping.instance().A, 3);
			buttonMap.put(GamepadMapping.instance().B, 2);
			initDone = true;
			if(parent != null) {
				text = parent.getText();
			}
		}
		wait = System.currentTimeMillis();
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
		if(System.currentTimeMillis() - wait < 30) return;
		wait = 0;
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
				if(parent != null) {
					parent.setText(text);
				}
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
	
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		sr.begin(ShapeType.Filled);
		float r = 50;
		float dist = r / (float) Math.sin(Math.PI * 2 / 16);
		float R = (2 * dist + 3 * r) / 2f;
		float cx = Gdx.graphics.getWidth() / 2;
		float cy = (2 * dist + 3 * r) / 2f + 10;
		BitmapFont textFont = Root.getFont(24);
		sr.setColor(background);
		sr.circle(cx, cy, R, 60);
		sr.setColor(wheel);
		sr.circle(cx, cy, dist, 60);
		for(int petal = 0; petal < 8; petal++) {
			float x = cx + (float) (Math.cos((2 - petal) * Math.PI / 4)) * dist;
			float y = cy - dist + (float) (1 + Math.sin((2 - petal) * Math.PI / 4)) * dist;
			if(selected == petal) {
				sr.setColor(selectedWheel);
			}
			else {
				sr.setColor(wheel);
			}
			sr.circle(x, y, r, 60);
			if(selected == petal) {
				for(int charIndex = 0; charIndex < 4; charIndex++) {
					String char_ = "" + chars.charAt(petal * 4 + charIndex);
					TextBounds bounds = textFont.getBounds(char_);
					sr.setColor(buttonColors[charIndex]);
					sr.circle(x + (float) Math.cos((charIndex - 2) * Math.PI / 2) * r / 2,
							y - (float) Math.sin((charIndex - 2) * Math.PI / 2) * r / 2,
							bounds.height - 2, 60);
				}
			}
		}
		sr.end();
		sb.begin();
		textFont.setColor(charColor);
		for(int petal = 0; petal < 8; petal++) {
			float x = cx + (float) (Math.cos((2 - petal) * Math.PI / 4)) * dist;
			float y = cy - dist + (float) (1 + Math.sin((2 - petal) * Math.PI / 4)) * dist;
			for(int charIndex = 0; charIndex < 4; charIndex++) {
				String char_ = "" + chars.charAt(petal * 4 + charIndex);
				TextBounds bounds = textFont.getBounds(char_);
				textFont.draw(sb, "" + char_,
						x  + (float) Math.cos((charIndex - 2) * Math.PI / 2) * r / 2 - bounds.width / 2,
						y - (float) Math.sin((charIndex - 2) * Math.PI / 2) * r / 2 + bounds.height / 2);
			}
		}
		textFont = Root.getFont(36);
		textFont.setColor(Color.BLACK);
		textFont.draw(sb, text, (Gdx.graphics.getWidth() - textFont.getBounds(text).width) / 2, Gdx.graphics.getHeight() - 40);
		sb.end();
	}
	
	public static void main(String[] args) {
		GameComponent.create("Daisywheel", 800, 600, true, false);
		GameComponent gc = GameComponent.getGame();
		gc.pushContext(new DaisyInput());
		gc.setPaused(false);
		gc.start();
	}
}
