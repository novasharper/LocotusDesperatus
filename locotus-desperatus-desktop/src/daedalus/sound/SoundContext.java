package daedalus.sound;


import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import daedalus.util.RandomSource;

public class SoundContext {
	private String name;
	private HashMap<String, ArrayList<Music>> songMap;
	private HashMap<String, ArrayList<Sound>> soundMap;
	
	public SoundContext(String description) {
		this();
		JsonObject obj = new JsonParser().parse(description).getAsJsonObject();
		name = obj.get("name").getAsString();
		for(Entry<String, JsonElement> entry : obj.get("songs").getAsJsonObject().entrySet()) {
			JsonArray songArr = entry.getValue().getAsJsonArray();
			for(int i = 0; i < songArr.size(); i++) {
				addSong(entry.getKey(), new File(songArr.get(i).getAsString()));
			}
		}
		for(Entry<String, JsonElement> entry : obj.get("sounds").getAsJsonObject().entrySet()) {
			JsonArray songArr = entry.getValue().getAsJsonArray();
			for(int i = 0; i < songArr.size(); i++) {
				addSound(entry.getKey(), new File(songArr.get(i).getAsString()));
			}
		}
	}
	
	public SoundContext() {
		songMap = new HashMap<String, ArrayList<Music>>();
		soundMap = new HashMap<String, ArrayList<Sound>>();
		name = "blank";
	}
	
	public String getName() {
		return name;
	}
	
	public void addSong(String key, File songFile) {
		if(!songMap.containsKey(key)) songMap.put(key, new ArrayList<Music>());
		ArrayList<Music> songs = songMap.get(key);
		songs.add(SoundSystem.audio.newMusic(new FileHandle(songFile)));
	}
	
	public void addSound(String key, File soundFile) {
		if(!soundMap.containsKey(key)) soundMap.put(key, new ArrayList<Sound>());
		ArrayList<Sound> sounds = soundMap.get(key);
		sounds.add(SoundSystem.audio.newSound(new FileHandle(soundFile)));
	}
	
	public Music getRandomSong(String groupName) {
		ArrayList<Music> mList = songMap.get(groupName);
		if(mList == null) return null;
		int nextIndex = RandomSource.rs.nextInt() % mList.size();
		return mList.get(nextIndex);
	}
	
	public Sound getRandomSound(String groupName) {
		ArrayList<Sound> mList = soundMap.get(groupName);
		if(mList == null) return null;
		int nextIndex = RandomSource.rs.nextInt() % mList.size();
		return mList.get(nextIndex);
	}
	
	public static SoundContext loadFromLevel(File levelDir) {
		SoundContext ctxt = new SoundContext();
		try {
			File soundDir = new File(levelDir, "songs");
			FileReader reader = new FileReader(new File(soundDir, "songs.json"));
			JsonObject obj = new JsonParser().parse(reader).getAsJsonObject();
			ctxt.name = obj.get("name").getAsString();
			for(Entry<String, JsonElement> entry : obj.get("songs").getAsJsonObject().entrySet()) {
				JsonArray songArr = entry.getValue().getAsJsonArray();
				for(int i = 0; i < songArr.size(); i++) {
					ctxt.addSong(entry.getKey(), new File(soundDir, entry.getKey() + "/" + songArr.get(i).getAsString()));
				}
			}
			for(Entry<String, JsonElement> entry : obj.get("sounds").getAsJsonObject().entrySet()) {
				JsonArray songArr = entry.getValue().getAsJsonArray();
				for(int i = 0; i < songArr.size(); i++) {
					ctxt.addSound(entry.getKey(), new File(soundDir, entry.getKey() + "/" + songArr.get(i).getAsString()));
				}
			}
		} catch(Exception ex) {
			return null;
		}
		return ctxt;
	}
}
