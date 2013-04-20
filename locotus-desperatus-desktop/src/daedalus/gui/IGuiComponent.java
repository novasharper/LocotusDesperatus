package daedalus.gui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import daedalus.input.IGamepadEventHandler;

public interface IGuiComponent {
	public void addTo(Menu m);
	public void render(SpriteBatch sb, ShapeRenderer sr, Point location);
}
