package daedalus;

import daedalus.level.Level;

public class Physics {
	private static Level level;
	
	public static void setLevel(Level level) {
		Physics.level = level;
	}
	
	public static Level getLevel() {
		return level;
	}
}
