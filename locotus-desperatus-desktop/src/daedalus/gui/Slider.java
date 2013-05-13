package daedalus.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

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

public class Slider implements IInput {
	int minVal, maxVal, step, val;
	private Menu parent;
	private boolean selected, enabled;

	public Slider(int minVal, int maxVal, int step) {
		this.minVal = minVal;
		this.maxVal = maxVal;
		this.step = step;
		this.val = 0;
		this.enabled = true;
	}
	
	public void addTo(Menu m) {
		parent = m;
	}
	
	public void render(SpriteBatch sb, ShapeRenderer sr, Point location) {
		Color color;
		if(!enabled) color = new Color(0.533f, 0.533f, 0.533f, 1f);
		else if(selected) color = new Color(0.533f, 0.533f, 0.8f, 1f);
		else color = new Color(0.133f, 0.133f, 0.8f, 1f);
		
		float border = 5f, height = 20f;
		
		Rectangle size = parent.getDefaultSize();
		
		sr.begin(ShapeType.Filled);
		
		// Draw track
		sr.setColor(Color.BLACK);
		sr.rect(location.x - 2.5f, location.y - 2.5f, size.width + 5, 25);
		sr.setColor(color);
		sr.rect(location.x, location.y, size.width, 20);
		
		// Draw slider
		float cx = location.x + size.width * Math.min(1f * step * val, maxVal - minVal) / (maxVal - minVal);
		float width_ptr = 20f, height_ptr = 30f;
		sr.setColor(Color.BLACK);
		sr.rect(cx - (border + width_ptr) / 2f, location.y - (height_ptr - height + border) / 2f, width_ptr + border, height_ptr + border);
		sr.setColor(0.133f, 0.133f, 0.8f, 1f);
		sr.rect(cx - width_ptr / 2f, location.y - (height_ptr - height) / 2f, width_ptr, height_ptr);
		sr.end();
		
		// Label
		String label = "" + Math.min(val * step + minVal, maxVal);
		BitmapFont font = Root.getFont(24);
		TextBounds lBound = font.getBounds(label);
		float width_t = lBound.width;
		
		sb.begin();
		if(!label.isEmpty()) {
			font.draw(sb, label, location.x - width_t - 20, location.y + height);
		}
		sb.end();
	}

	@Override
	public void handleInput(GamepadEvent ev) {
		if(ev.getType() == EventType.RELEASED && ev.getCType() == ComponentType.DPAD) {
			if(ev.getButtonID() == 2) {
				val++;
				if(val > Math.round(1.0 * (maxVal - minVal) / step)) val = (int) Math.round(1.0 * (maxVal - minVal) / step);
			} else if(ev.getButtonID() == 6) {
				val--;
				if(val < 0) val = 0;
			}
		}
	}

	@Override
	public void select() {
		selected = true;
	}

	@Override
	public void deselect() {
		selected = false;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void handleInput(KeyEvent event) {
	}
	
	public int getValue() {
		return Math.min(minVal + val * step, maxVal);
	}

}
