package daedalus.input;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import daedalus.input.GamepadEvent.ComponentType;
import daedalus.input.GamepadEvent.EventType;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller.Type;

public class Gamepad {
	public static final float deadzone = 0.004f;
	public static final float rot_deadzone = 0.01f;
	
	private ArrayList<IGamepadEventHandler> handlers;
	private Point2D.Float stick_l, stick_r;
	private float trigger;
	private int hatPosition;
	private ArrayList<Boolean> buttonsValues;

	private Controller gamepad = null;
	
	public Gamepad() {
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		for(int i = 0; i < controllers.length; i++) {
			if(controllers[i].getType() == Type.GAMEPAD) {
				gamepad = controllers[i];
				break;
			}
		}
		if(gamepad == null) throw new RuntimeException("No gamepad found.");
		gamepad.getName();
		stick_l = new Point2D.Float();
		stick_r = new Point2D.Float();
		buttonsValues = new ArrayList<Boolean>();
		handlers = new ArrayList<IGamepadEventHandler>();
	}
	
	public int getDPad() {
		if(!isControllerConnected()) throw new RuntimeException("Could not poll controller.");
		return hatPosition;
	}
	
	public float getXLeft() {
		if(!isControllerConnected()) throw new RuntimeException("Could not poll controller.");
		return stick_l.x;
	}
	
	public float getYLeft() {
		if(!isControllerConnected()) throw new RuntimeException("Could not poll controller.");
		return stick_l.y;
	}
	
	public float getXRight() {
		if(!isControllerConnected()) throw new RuntimeException("Could not poll controller.");
		return stick_r.x;
	}
	
	public float getYRight() {
		if(!isControllerConnected()) throw new RuntimeException("Could not poll controller.");
		return stick_r.y;
	}
	
	public boolean pollButton(int index) {
		if(!isControllerConnected()) throw new RuntimeException("Could not poll controller.");
		return buttonsValues.get(index);
	}
	
	public float pollTrigger() {
		if(!isControllerConnected()) throw new RuntimeException("Could not poll controller.");
		return trigger;
	}
	
	private float pollStick(Identifier.Axis axis) {
		Component component = gamepad.getComponent(axis);
		float val = component.getPollData();
		if(-deadzone <= val && val <= deadzone) return 0;
		return val;
	}
	
	public synchronized void tick() {
		if (!isControllerConnected())
			throw new RuntimeException("Could not poll controller.");

		stick_l.x = pollStick(Identifier.Axis.X);
		stick_l.y = pollStick(Identifier.Axis.Y);

		stick_r.x = pollStick(Identifier.Axis.RX);
		stick_r.y = pollStick(Identifier.Axis.RY);
		
		trigger = gamepad.getComponent(Identifier.Axis.Z).getPollData();
		
		GamepadEvent ev = null;
		int newHatPosition = (int) (gamepad.getComponent(Identifier.Axis.POV).getPollData() * 8);
		if(newHatPosition == 0) newHatPosition = -1;
		else newHatPosition = (newHatPosition + 6) % 8;
		if(newHatPosition == -1 && hatPosition != -1) {
			ev = new GamepadEvent(EventType.RELEASED, ComponentType.DPAD, hatPosition, this);
		} else if(newHatPosition != -1 && hatPosition == -1) {
			ev = new GamepadEvent(EventType.PRESSED, ComponentType.DPAD, hatPosition, this);			
		}
		if(ev != null) {
			for(int in = 0; in < handlers.size(); in++) {
				handlers.get(in).handleInput(ev);
			}
		}
		hatPosition = newHatPosition;
		
		Component[] components = gamepad.getComponents();
		
		int index = 0;
		for (int i = 0; i < components.length; i++) {
			Component component = components[i];
			
			// Add states of the buttons
			if (component.getName().contains("Button")) {
				while(index >= buttonsValues.size()) buttonsValues.add(false);
				boolean isPressed = component.getPollData() == 1.0f;
				boolean wasPressed = buttonsValues.get(index);
				buttonsValues.set(index, isPressed);
				ev = null;
				if(isPressed && !wasPressed) ev = new GamepadEvent(EventType.PRESSED, ComponentType.BUTTON, index, this);
				else if(!isPressed && wasPressed) ev = new GamepadEvent(EventType.RELEASED, ComponentType.BUTTON, index, this);
				if(ev != null) {
					for(int in = 0; in < handlers.size(); in++) {
						handlers.get(in).handleInput(ev);
					}
				}
				index++;
			}
		}
	}
	
	public void addEventHandler(IGamepadEventHandler handler) {
		handlers.add(handler);
	}
	
	/**
	 * Checks if the controller is connected/valid. It also poll the controller
	 * for data, but it doesn't save states of the buttons into buttons array
	 * list that is used by getButtonsValues() and getButtonValue(int index)
	 * methods.
	 * 
	 * @see joystick.JInputJoystick#pollController()
	 * 
	 * @return True if controller is connected, false otherwise.
	 */
	public boolean isControllerConnected() {
		try {
			return gamepad.poll();
		} catch (Exception e) {
			return false;
		}
	}
}
