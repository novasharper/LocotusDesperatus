package daedalus.gui;

import java.awt.event.KeyEvent;

import daedalus.input.IGamepadEventHandler;

public interface IInput extends IGuiComponent, IGamepadEventHandler {
	public void select();
	public void deselect();
	public boolean isEnabled();
	public void setEnabled(boolean enabled);
	public void handleInput(KeyEvent event);
}
