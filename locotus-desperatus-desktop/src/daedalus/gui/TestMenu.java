package daedalus.gui;

import java.awt.Color;

import daedalus.main.GameComponent;

public class TestMenu extends Menu {
	private Button button, exit;
	public TestMenu() {
		addComponent(new Label("GUI Test"));
		button = new Button("Exit to main menu");
		exit = new Button("Exit");
		exit.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				GameComponent.getGame().stop();
			}
		});
		
		button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				GameComponent.getGame().setPaused(false);
			}
		});
		addComponent(button);
		addComponent(exit);
		setBackgroundColor(new Color(0x0f131f));
	}
	
	public static void main(String[] args) {
		GameComponent.create("GUI Test", 800, 600, true, false);
		Menu menu = new TestMenu();
		GameComponent.getGame().setPauseMenu(menu);
		GameComponent.getGame().start();
	}
}
