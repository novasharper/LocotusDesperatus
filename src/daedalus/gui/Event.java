package daedalus.gui;

public class Event {
	public static final int NOTHING = 0;
	public static final int BUTTON_ACTIVATE = 1;
	public static final int SELECTOR_INDEX_CHANGE = 2;
	public static final int TEXT_VALUE_CHANGE = 3;
	public static final int SELECTOR_LEFT = 4;
	public static final int SELECTOR_RIGHT = 5;
	
	private int type;
	private String id;
	private int extendedInfo;
	private IInput component;
	
	public Event(int type, String componentID, int extendedInfo, IInput caller) {
		this.type = type;
		this.id = componentID;
		this.component = caller;
	}
	
	public String getID() {
		return id;
	}
	
	public int getEventType() {
		return type;
	}
	
	public int getExtendedInfo() {
		return extendedInfo;
	}
	
	public IInput getComponent() {
		return component;
	}
}
