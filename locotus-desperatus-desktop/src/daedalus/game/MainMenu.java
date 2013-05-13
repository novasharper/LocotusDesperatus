package daedalus.game;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;

import daedalus.gui.Button;
import daedalus.gui.Event;
import daedalus.gui.EventListener;
import daedalus.gui.Label;
import daedalus.gui.Menu;
import daedalus.gui.Slider;
import daedalus.main.GameComponent;
import daedalus.util.Util;

public class MainMenu extends Menu {
	private Label title;
	private Button newGame, settings, exit;
	
	public MainMenu() {
		addComponent(title = new Label("Locotus Desperatus"));
		newGame = new Button("New Game");
		settings = new Button("Settings");
		exit = new Button("Exit");
		newGame.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				// Push on game context/whatever
			}
		});
		settings.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				// Push on settings context
			}
		});
		exit.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				GameComponent.getGame().stop();
			}
		});
		addComponent(newGame);
		addComponent(settings);
		addComponent(exit);
		addComponent(new Slider(1, 5, 1));
		setBackgroundColor(new Color(0x0f131f));
		File backgroundFile = new File(Util.getWorkingDir(), "resources/image/menu_background.jpg");
		Pixmap background = new Pixmap(new FileHandle(backgroundFile));
		setBackground(background);
	}
	
	public static void main(String[] args) {
		GameComponent.create("GUI Test", 800, 600, true, false);
		GameComponent.getGame().pushContext(new MainMenu());
		GameComponent.getGame().resume();
	}
}
