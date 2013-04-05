package daedalus.gui;

import java.awt.Color;
import java.awt.Image;
import java.io.InputStream;

import javax.imageio.ImageIO;

import daedalus.main.GameComponent;

public class TestMainMenu extends Menu {
	private Button button, exit;
	private Label label;
	public TestMainMenu() {
		super();
		addComponent(label = new Label("GUI Test"));
		button = new Button("Start Game");
		exit = new Button("Exit");
		
		exit.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				GameComponent.getGame().stop();
			}
		});
		button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				GameComponent.getGame().popContext();
			}
		});
		addComponent(button);
		addComponent(exit);
		setBackgroundColor(new Color(0x0f131f));
		InputStream menuBackground = GameComponent.class.getClassLoader().getResourceAsStream("daedalus/res/menu_background.jpg");
		try {
			Image img = ImageIO.read(menuBackground);
			setBackground(img);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		GameComponent.create("GUI Test", 800, 600, true);
		Menu menu = new TestMenu();
		GameComponent.getGame().setPauseMenu(menu);
		GameComponent.getGame().pushContext(new TestMainMenu());
		GameComponent.getGame().start();
	}
}
