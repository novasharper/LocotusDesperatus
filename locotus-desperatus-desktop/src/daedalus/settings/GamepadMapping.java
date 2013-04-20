package daedalus.settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import daedalus.util.Util;

public class GamepadMapping {
	public int SPACE = 4;
	public int BACKSPACE = 5;
	public int BACK = 6;
	public int START = 7;
	public int A = 0;
	public int B = 1;
	public int X = 2;
	public int Y = 3;
	
	private static GamepadMapping instance = null;
	
	public static void load() {
		if(instance != null) return;
		instance = new GamepadMapping();
		Gson gson = new Gson();
		File appDir = Util.getWorkingDir();
		File keySettings = new File(appDir, "keymap.json");
		try {
			FileReader reader = new FileReader(keySettings);
			instance = (GamepadMapping) gson.fromJson(reader, GamepadMapping.class);
			reader.close();
		} catch(Exception e) {}
	}
	
	public static void save() {
		if(instance == null) return;
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		File appDir = Util.getWorkingDir();
		if(!appDir.exists()) appDir.mkdir();
		File keySettings = new File(appDir, "keymap.json");
		try {
			FileWriter writer = new FileWriter(keySettings);
			gson.toJson(instance, writer);
			writer.close();
		} catch(Exception e) {}
	}
	
	public static GamepadMapping instance() {
		if(instance == null) load();
		return instance;
	}
}
