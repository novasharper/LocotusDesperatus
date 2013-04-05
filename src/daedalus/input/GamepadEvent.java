package daedalus.input;

public class GamepadEvent {
	public static enum EventType {
		PRESSED,
		RELEASED
	}
	public static enum ComponentType {
		DPAD,
		TRIGGER,
		BUTTON
	}
	private EventType type;
	private int buttonID;
	private ComponentType cType;
	private Gamepad sender;
	
	public GamepadEvent(EventType type, ComponentType cType, int buttonID, Gamepad sender) {
		this.type = type;
		this.cType = cType;
		this.buttonID = buttonID;
		this.sender = sender;
	}
	
	public EventType getType() {
		return type;
	}
	
	public ComponentType getCType() {
		return cType;
	}
	
	public int getButtonID() {
		return buttonID;
	}
	
	public Gamepad getSender() {
		return sender;
	}
}
