package daedalus.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import daedalus.util.Util;


public class Options {
	// TODO: learn how to write an actual options class
	
	private static Options instance = null;
	
	public static void load() {
		if(instance != null) return;
		instance = new Options();
		Gson gson = new Gson();
		File appDir = Util.getWorkingDir();
		File keySettings = new File(appDir, "keymap.json");
		try {
			FileReader reader = new FileReader(keySettings);
			instance = (Options) gson.fromJson(reader, Options.class);
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
			String sav = gson.toJson(instance);
			System.out.println(sav);
			FileWriter writer = new FileWriter(keySettings);
			gson.toJson(instance, writer);
			writer.close();
		} catch(Exception e) {}
	}
	
	public static Options instance() {
		if(instance == null) load();
		return instance;
	}
}
