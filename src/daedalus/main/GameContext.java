package daedalus.main;

import java.awt.Graphics2D;

public abstract class GameContext {
	public abstract void init();
	public abstract void render(Graphics2D gr);
	public abstract void tick();
	public abstract boolean isTransparent();
}
