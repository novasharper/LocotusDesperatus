package daedalus.gui;

import java.awt.Color;
import java.awt.Image;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;

import daedalus.main.GameComponent;

public class TestMainMenu extends Menu {
	private Button button, exit;
	private Label label;
	private Input input;
	public TestMainMenu() {
		super();
		addComponent(label = new Label("GUI Test"));
		button = new Button("Start Game");
		input = new Input("Input Random: ");
		exit = new Button("Exit");
		
		exit.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				GameComponent.getGame().stop();
			}
		});
		button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				//GameComponent.getGame().popContext();
			}
		});
		addComponent(button);
		addComponent(input);
		addComponent(exit);
		setBackgroundColor(new Color(0x0f131f));
		InputStream menuBackground = GameComponent.class.getClassLoader().getResourceAsStream("daedalus/res/menu_background.jpg");
		try {
			Gdx2DPixmap gpm = new Gdx2DPixmap(menuBackground, Gdx2DPixmap.GDX2D_FORMAT_RGB888);
			Pixmap pixmap = new Pixmap(gpm);
			setBackground(pixmap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		GameComponent.create("GUI Test", 800, 600, true, false);
		Menu menu = new TestMenu();
		GameComponent.getGame().setPauseMenu(menu);
		GameComponent.getGame().pushContext(new TestMainMenu());
		GameComponent.getGame().resume();
	}
}
