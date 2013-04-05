package daedalus.main;

import daedalus.gui.*;
import daedalus.input.Gamepad;
import daedalus.input.GamepadEvent;
import daedalus.input.GamepadEvent.ComponentType;
import daedalus.input.GamepadEvent.EventType;
import daedalus.input.IGamepadEventHandler;
import daedalus.settings.GamepadMapping;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Main game class. Cannot be subclassed.
 * In order to use, create an instance using the create method.
 * Then, push and remove GameContext elements.
 *  
 * @author pat
 *
 */
public final class GameComponent extends JFrame implements Runnable, IGamepadEventHandler {
	private static final long serialVersionUID = 8041997620788772424L;
	
	private class KeyEventHandler implements KeyEventDispatcher {
		public boolean dispatchKeyEvent(KeyEvent event) {
			keyEventQueue.addLast(event);
			if(event.getID() == KeyEvent.KEY_RELEASED && event.getKeyCode() == exitKey)
				exitKeyPressed = true;
			if(event.getID() == KeyEvent.KEY_RELEASED && event.getKeyCode() == KeyEvent.VK_F7)
				toggleFPSCounter();
			if(event.getID() == KeyEvent.KEY_RELEASED && event.getKeyCode() == KeyEvent.VK_F2)
				screenshot();
			return false;
		}
	}
	
	private static GameComponent game;
	public final int width, height;
	private boolean running;
	private Thread gameThread;
	public static final double framerate = 60;
	public static final int tileSize = 64;
	private int fps;
	private LinkedList<GameContext> gameContextStack;
	private LinkedList<KeyEvent> keyEventQueue;
	private Gamepad gamepad;
	private Canvas canvas;
	private boolean useGamepad;
	public static final Font fpsFont;
	private static boolean showFPS = true;
	private Menu pauseMenu;
	private boolean exitKeyPressed = false;
	private final int exitKey = KeyEvent.VK_ESCAPE;
	private boolean paused = true;
	
	static {
		// Specify that canvas mus be OpenGL accelerated
		System.setProperty("sun.java2d.opengl", "true");
		
		// load font for fps counter
		InputStream fontStream = GameComponent
				.class.getClassLoader()
				.getResourceAsStream("daedalus/res/fps_counter.ttf");
		Font font;
		try { // Try to create font
			font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
		} catch (Exception e) { // If that fails, use size 24 Arial
			font = new Font("Arial", Font.BOLD, 24);
		}
		fpsFont = font;
	}
	
	/**
	 * Create an instance of GameComponent
	 * @param width Width of game canvas
	 * @param height Height of game canvas
	 * @param title Window title
	 * @param useGamepad Whether gamepad will be used for input
	 */
	
	private GameComponent(int width, int height, String title, boolean useGamepad) {
		// Save width and height
		this.width = width;
		this.height = height;
		
		Dimension size = new Dimension(width, height);
		
		// Create game canvas
		canvas = new Canvas();
		// Add canvas to window
		this.add(canvas);
		
		canvas.setMinimumSize(size);
		canvas.setMaximumSize(size);
		canvas.setSize(size);
		canvas.setPreferredSize(size);
		canvas.setBounds(0, 0, width, height);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setIgnoreRepaint(true);
		getContentPane().setIgnoreRepaint(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stop();
			}
		});
		
		setTitle(title);
		gameContextStack = new LinkedList<>();
		this.useGamepad = useGamepad;
		
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyEventHandler());
		keyEventQueue = new LinkedList<KeyEvent>();
	}
	
	public static void toggleFPSCounter() {
		showFPS = !showFPS;
	}
	
	public static void create(String title, int width, int height, boolean useGamepad) {
		if(game != null) throw new RuntimeException("Cannot create more than one game.");
		game = new GameComponent(width, height, title, useGamepad);
	}
	
	public static GameComponent getGame() {
		if(game == null) throw new RuntimeException("Game has not been instantiated yet.");
		return game;
	}
	
	public static Canvas getDrawCanvas() {
		if(game == null) throw new RuntimeException("Game has not been instantiated yet.");
		return game.canvas;
	}
	
	public static Gamepad getGamePad() {
		if(game == null) throw new RuntimeException("Game has not been instantiated yet.");
		return game.gamepad;
	}
	
	public void init() {
		if(useGamepad) {
			gamepad = new Gamepad();
			gamepad.addEventHandler(this);
		}
		for(GameContext ctxt : gameContextStack) ctxt.init();
		if(pauseMenu != null) pauseMenu.init();
		GamepadMapping.load();
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public void setPauseMenu(Menu pauseMenu) {
		this.pauseMenu = pauseMenu;
	}
	
	private synchronized void render(Graphics2D gr) {
		// Enable anti-aliasing and interpolation
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		// Clear the canvas
		gr.clearRect(0, 0, width, height);
		
		if(paused) {
			if(pauseMenu != null) pauseMenu.render(gr);
		} else {
			if(gameContextStack.isEmpty()) return;
			// Iterate backwards to get index of first context that is shown
			int first;
			for(first = gameContextStack.size() - 1; first >= 0; first--) {
				if(!gameContextStack.get(first).isTransparent()) break;
			}
			// Render all contexts starting with that one
			for(; first < gameContextStack.size(); first++) {
				gameContextStack.get(first).render(gr);
			}
		}
		
		if(showFPS) {
			Font saveFont = gr.getFont();
			gr.setFont(fpsFont);
			gr.setColor(new Color(1, 1, 0, 1f));
			gr.fillRect(0, 0, 80, 35);
			gr.setColor(new Color(1, 0, 0, 1f));
			gr.setFont(gr.getFont().deriveFont(32f));
			gr.drawString("" + fps, 20, 25);
			gr.setFont(saveFont);
		}
	}
	
	/**
	 * Tick. There are [framerate] ticks per second.
	 */
	private void tick() {
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
		// Initialize context
		if(running) gameContextStack.getLast().init();
	}
	
	/**
	 * Remove last context
	 * @return removed context
	 */
	public GameContext popContext() {
		return gameContextStack.removeLast();
	}
	
	public boolean isActive(GameContext ctxt) {
		for(int first = gameContextStack.size() - 1; first >= 0; first--) {
			if(gameContextStack.get(first) == ctxt) return true;
			if(!gameContextStack.get(first).isTransparent()) break;
		}
		if(ctxt == pauseMenu && paused) 
			return true;
		return false;
	}
	
	/**
	 * Main loop
	 */
	public void run() {
		running = true;
		
		long lastTime = System.nanoTime();
		// Number of unprocessed events
		double unprocessed = 0;
		int frames = 0;
		// FPS second timer
		long lastTimer1 = System.currentTimeMillis();
		
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		int toTick = 0;

		long lastRenderTime = System.nanoTime();
		int min = 999999999;
		int max = 0;
		
		while(running) {
			boolean shouldRender = false;
			double nsPerTick = 1000000000.0 / framerate;
			
			while (unprocessed >= 1) {
				toTick++;
				unprocessed -= 1;
			}
			
			int tickCount = toTick;
			if (toTick > 0 && toTick < 3) {
				tickCount = 1;
			}
			if (toTick > 20) {
				toTick = 20;
			}
			
			for (int i = 0; i < tickCount; i++) {
				toTick--;
				tick();
				shouldRender = true;
			}
			
			BufferStrategy bs = canvas.getBufferStrategy();
			if(bs == null) {
				canvas.createBufferStrategy(2);
				continue;
			}
			
			if(shouldRender) {
				frames++;
				Graphics2D g = (Graphics2D) bs.getDrawGraphics();
				
				render(g);
				
				long renderTime = System.nanoTime();
				int timePassed = (int) (renderTime - lastRenderTime);
				if (timePassed < min) {
					min = timePassed;
				}
				if (timePassed > max) {
					max = timePassed;
				}
				lastRenderTime = renderTime;
			}
			
			long now = System.nanoTime();
			unprocessed += (now - lastTime) / nsPerTick;
			lastTime = now;
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(shouldRender) {
				if(bs != null) {
					bs.show();
				}
			}
			
			if (System.currentTimeMillis() - lastTimer1 > 1000) {
				lastTimer1 = System.currentTimeMillis();
				fps = frames;
				frames = 0;
			}
		}
		
		GamepadMapping.save();
		
		System.exit(0);
	}
	
	public void start() {
		// Game has already been started
		if(gameThread != null) return;
		// Pack window to fit canvas
		pack();
		// Show game window
		setVisible(true);
		// Create game thread
		gameThread = new Thread(this, "game-thread");
		// Start game
		gameThread.start();
	}
	
	public void stop() {
		running = false;
	}
	
	public void handleInput(GamepadEvent ev) {
		if(ev.getType() == EventType.RELEASED && ev.getCType() == ComponentType.BUTTON
				&& ev.getButtonID() == GamepadMapping.instance().BACK)
			stop();
	}
	
	public void screenshot() {
		Rectangle screenRect = new Rectangle(canvas.getLocationOnScreen(), canvas.getSize());
		BufferedImage capture;
		try {
			capture = new Robot().createScreenCapture(screenRect);
			ImageIO.write(capture, "PNG", new File("scrn.png"));
		} catch (IOException e) {
		} catch (AWTException e1) {
		}
	}
}
