package daedalus.input;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.desktop.DesktopControllerManager;
import com.badlogic.gdx.utils.Array;

import daedalus.input.GamepadEvent.ComponentType;
import daedalus.input.GamepadEvent.EventType;

public class Gamepad extends ControllerAdapter {
	public static final float deadzone = 0.004f;
	public static final float rot_deadzone = 0.01f;
	
	private ArrayList<IGamepadEventHandler> handlers;
	private int hatPosition = -1;
	private static Map<PovDirection, Integer> povMap;
	
	static {
		povMap = new HashMap<PovDirection, Integer>();
		povMap.put(PovDirection.center, -1);
		povMap.put(PovDirection.north, 0);
		povMap.put(PovDirection.northEast, 1);
		povMap.put(PovDirection.east, 2);
		povMap.put(PovDirection.southEast, 3);
		povMap.put(PovDirection.south, 4);
		povMap.put(PovDirection.southWest, 5);
		povMap.put(PovDirection.west, 6);
		povMap.put(PovDirection.northWest, 7);
	}

	private Controller gamepad = null;
	
	public Gamepad() {
		handlers = new ArrayList<IGamepadEventHandler>();
	}
	
	public int getDPad() {
		return hatPosition;
	}
	
	public float pollAxis(int axis) {
		float val = 0;
		if(gamepad != null) val = gamepad.getAxis(axis);
		if(-deadzone <= val && val <= deadzone) return 0;
		return val;
	}
	
	public boolean pollButton(int buttonID) {
		if(gamepad == null) return false;
		return gamepad.getButton(buttonID);
	}
	
	public synchronized void reload() {
		if(Gdx.input != null) {
			Array<Controller> controllers = new DesktopControllerManager().getControllers();
			if(controllers.size > 0) gamepad = controllers.first();
		}
		if(gamepad != null) {
			gamepad.addListener(this);
		}
		hatPosition = -1;
	}
	
	public synchronized void tick() {
		if(gamepad == null) {
			reload();
		}
	}
	
	public void addEventHandler(IGamepadEventHandler handler) {
		handlers.add(handler);
	}
	
	public boolean buttonDown(Controller controller, int buttonID) {
		GamepadEvent ev = new GamepadEvent(EventType.PRESSED, ComponentType.BUTTON, buttonID, this);
		for(IGamepadEventHandler handler : handlers) {
			handler.handleInput(ev);
		}
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonID) {
		GamepadEvent ev = new GamepadEvent(EventType.RELEASED, ComponentType.BUTTON, buttonID, this);
		for(IGamepadEventHandler handler : handlers) {
			handler.handleInput(ev);
		}
		return false;
	}

	@Override
	public boolean povMoved(Controller controller, int componentIndex, PovDirection povDirection) {
		GamepadEvent ev = null;
		int newHatPosition = povMap.get(povDirection);
		if(newHatPosition == -1 && hatPosition != -1) {
			ev = new GamepadEvent(EventType.RELEASED, ComponentType.DPAD, hatPosition, this);
		} else if(newHatPosition != -1 && hatPosition == -1) {
			ev = new GamepadEvent(EventType.PRESSED, ComponentType.DPAD, hatPosition, this);
		}
		if(ev != null) {
			for(IGamepadEventHandler handler : handlers) {
				handler.handleInput(ev);
			}
		}
		hatPosition = newHatPosition;
		return false;
	}
}
