package daedalus.graphics;

import java.awt.Graphics2D;

public interface GraphicsElement {
	public void render(Graphics2D gr);
	public void tick();
}
