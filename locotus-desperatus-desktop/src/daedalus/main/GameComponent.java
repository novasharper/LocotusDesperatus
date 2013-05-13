package daedalus.main;

import daedalus.Root;
import daedalus.graphics.GStreamerLibrary;
import daedalus.gui.*;
import daedalus.input.Gamepad;
import daedalus.input.GamepadEvent;
import daedalus.input.GamepadEvent.ComponentType;
import daedalus.input.GamepadEvent.EventType;
import daedalus.input.IGamepadEventHandler;
import daedalus.input.F310;
import daedalus.settings.GamepadMapping;
import daedalus.sound.SoundSystem;
import daedalus.util.ScreenshotSaver;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.*;
import java.io.IOException;
import java.util.LinkedList;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * Main game class. Cannot be subclassed.
 * In order to use, create an instance using the create method.
 * Then, push and remove GameContext elements.
 *  
 * @author pat
 *
 */
public final class GameComponent extends InputAdapter implements IGamepadEventHandler, ApplicationListener {
	private static GameComponent game;
	public final int width, height;
	private boolean running;
	public static final int framerate = 60;
	public static final int tileSize = 128;
	private LinkedList<GameContext> gameContextStack;
	private LinkedList<KeyEvent> keyEventQueue;
	private Gamepad gamepad;
	private boolean useGamepad;
	private BitmapFont fpsFont;
	private static boolean showFPS = false;
	private Menu pauseMenu;
	private boolean exitKeyPressed = false;
	private final int exitKey = Keys.ESCAPE;
	private boolean paused = false;
	private boolean fullscreen;
	private SpriteBatch sb;
	private ShapeRenderer sr;
	private OrthographicCamera camera;
	private InputMultiplexer inputStack;
	
	/**
	 * Create an instance of GameComponent
	 * @param width Width of game canvas
	 * @param height Height of game canvas
	 * @param title Window title
	 * @param useGamepad Whether gamepad will be used for input
	 */
	
	private GameComponent(int width, int height, String title, boolean useGamepad, boolean fullscreen) {
		// Save width and height
		this.width = width;
		this.height = height;
		
		gameContextStack = new LinkedList<GameContext>();
		inputStack = new InputMultiplexer(this);
		this.useGamepad = useGamepad;
		
		keyEventQueue = new LinkedList<KeyEvent>();
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = title;
		cfg.useGL20 = true;
		if(!fullscreen) {
			cfg.width = width;
			cfg.height = height;
		} else {
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			cfg.width = gd.getDisplayMode().getWidth();
			cfg.height = gd.getDisplayMode().getHeight();
			cfg.fullscreen = true;
		}
		cfg.resizable = false;
		cfg.vSyncEnabled = true;
		cfg.samples = 4;
		cfg.foregroundFPS = framerate + 1;
		cfg.backgroundFPS = framerate + 1;
		cfg.disableAudio = true;
		
		if(useGamepad) {
			gamepad = new Gamepad();
			gamepad.addEventHandler(this);
		}
		
		this.fullscreen = fullscreen;
		
		new LwjglApplication(this, cfg);
	}
	
	public static int getSize() {
		if(game == null) return -1;
		if(!game.fullscreen) return game.height;
		else {
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			return gd.getDisplayMode().getHeight();
		}
	}
	
	public static void toggleFPSCounter() {
		showFPS = !showFPS;
	}
	
	public static void create(String title, int width, int height, boolean useGamepad, boolean fullscreen) {
		if(game != null) throw new RuntimeException("Cannot create more than one game.");
		game = new GameComponent(width, height, title, useGamepad, fullscreen);
	}
	
	public static GameComponent getGame() {
		if(game == null) throw new RuntimeException("Game has not been instantiated yet.");
		return game;
	}
	
	public static Gamepad getGamePad() {
		if(game == null) throw new RuntimeException("Game has not been instantiated yet.");
		return game.gamepad;
	}
	
	private boolean initDone = false;
	public void init() {
		if(initDone) return;
		for(GameContext ctxt : gameContextStack) ctxt.init();
		if(pauseMenu != null) pauseMenu.init();
		GamepadMapping.load();
		initDone = true;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public void setPauseMenu(Menu pauseMenu) {
		this.pauseMenu = pauseMenu;
	}
	
	/**
	 * Tick. There are [framerate] ticks per second.
	 */
	private void tick() {
		SoundSystem.update();
		if(useGamepad) gamepad.tick();
		if(exitKeyPressed) {
			if(pauseMenu == null) { stop(); return; }
			else { paused = !paused; exitKeyPressed = false; }
		}
		if(paused) {
			if(pauseMenu != null) pauseMenu.tick();
		} else {
			if(gameContextStack.isEmpty()) return;
			// Iterate backwards to get index of first context that is updated
			int first;
			for(first = gameContextStack.size() - 1; first >= 0; first--) {
				if(!gameContextStack.get(first).isTransparent()) break;
			}
			
			// Update all contexts starting with that one
			for(; first < gameContextStack.size(); first++) {
				gameContextStack.get(first).tick();
			}
		}
	}
	
	/**
	 * Get queue of unprocessed keyboard events.
	 * @return
	 */
	public LinkedList<KeyEvent> getKeyEventQueue() {
		return keyEventQueue;
	}
	
	/**
	 * Add context to context stack
	 * @param newGameContext context to add
	 */
	public void pushContext(GameContext newGameContext) {
		gameContextStack.addLast(newGameContext);
		inputStack.addProcessor(newGameContext);
		// Initialize context
		if(running) gameContextStack.getLast().init();
	}
	
	/**
	 * Remove last context
	 * @return removed context
	 */
	public GameContext popContext() {
		GameContext last = gameContextStack.removeLast();
		inputStack.removeProcessor(last);
		return last;
	}
	
	public boolean isActive(GameContext ctxt) {
		if(!paused) {
			for(int first = gameContextStack.size() - 1; first >= 0; first--) {
				if(gameContextStack.get(first) == ctxt) return true;
				if(!gameContextStack.get(first).isTransparent()) break;
			}
		}
		else if(ctxt == pauseMenu) 
			return true;
		return false;
	}
	
	public void stop() {
		GamepadMapping.save();
		SoundSystem.shutdown();
		Gdx.app.exit();
	}
	
	public void handleInput(GamepadEvent ev) {
		if(ev.getType() == EventType.RELEASED && ev.getCType() == ComponentType.BUTTON) {
			if(ev.getButtonID() == F310.BUTTON_BACK) stop();
			else if(ev.getButtonID() == F310.BUTTON_START) setPaused(!paused);
		}
	}
	public void create() {
		Gdx.input.setInputProcessor(inputStack);
		camera = new OrthographicCamera(1, (float) height / width);
		fpsFont = Root.getFont(36, "data/fps_counter.ttf");
		sr = new ShapeRenderer();
		sb = new SpriteBatch();
		init();
	}
	public void resize(int width, int height) {
		camera = new OrthographicCamera(1, (float) height / width);
		sb = new SpriteBatch();
		sr = new ShapeRenderer();
		Root.reloadFonts();
	}
	
	public boolean keyUp(int keyid) {
		if(keyid == exitKey) exitKeyPressed = true;
		else if(keyid == Keys.F2) try {
			ScreenshotSaver.saveScreenshot();
		} catch(IOException ex) {}
		else if(keyid == Keys.F11) {
			if(fullscreen) {
				Gdx.graphics.setDisplayMode(width, height, false);
				fullscreen = false;
			} else {
				DisplayMode dm = Gdx.graphics.getDesktopDisplayMode();
				Gdx.graphics.setDisplayMode(dm.width, dm.height, true);
				fullscreen = true;
			}
			if(gamepad != null) gamepad.reload();
		} else if(keyid == Keys.F7) {
			toggleFPSCounter();
		}
		return true;
	}
	public void render() {
		tick();
		Gdx.graphics.getGL20().glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.graphics.getGL20().glClear(GL11.GL_COLOR_BUFFER_BIT);
		if(paused && pauseMenu != null) pauseMenu.render(sb, sr);
		else {
			if(gameContextStack.isEmpty()) return;
			// Iterate backwards to get index of first context that is shown
			int first;
			for(first = gameContextStack.size() - 1; first >= 0; first--) {
				if(!gameContextStack.get(first).isTransparent()) break;
			}
			// Render all contexts starting with that one
			for(; first < gameContextStack.size(); first++) {
				gameContextStack.get(first).render(sb, sr);
			}
		}
		if(showFPS) {
			sr.begin(ShapeType.Filled);
			sr.setColor(1, 1, 0, 1);
			sr.rect(0, Gdx.graphics.getHeight() - 35, Math.max(fpsFont.getBounds("" + Gdx.graphics.getFramesPerSecond()).width + 30, 80), 35);
			sr.end();
			sb.begin();
			fpsFont.setColor(1, 0, 0, 1);
			fpsFont.draw(sb, "" + Gdx.graphics.getFramesPerSecond(),
					15, Gdx.graphics.getHeight() - (35 - fpsFont.getCapHeight()) / 2f);
			sb.end();
		}
	}
	public void pause() {
		setPaused(true);
	}
	public void resume() {
		setPaused(false);
	}
	public void dispose() {
	}

	static {
		try {
			GStreamerLibrary.init();
		} catch (Exception e) {
		}
	}
}
