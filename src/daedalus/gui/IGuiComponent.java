package daedalus.gui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

import daedalus.input.IGamepadEventHandler;

public interface IGuiComponent {
	public void addTo(Menu m);
	public void render(Graphics2D gr, Point location);
}
