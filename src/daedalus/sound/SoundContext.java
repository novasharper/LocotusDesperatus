package daedalus.sound;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

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
			ArrayList<Music> songs = new ArrayList<Music>();
			JsonArray songArr = entry.getValue().getAsJsonArray();
			for(int i = 0; i < songArr.size(); i++) {
				songs.add(TinySound.loadMusic(songArr.get(i).getAsString()));
			}
			songMap.put(entry.getKey(), songs);
		}
		for(Entry<String, JsonElement> entry : obj.get("sounds").getAsJsonObject().entrySet()) {
			ArrayList<Sound> sounds = new ArrayList<Sound>();
			JsonArray songArr = entry.getValue().getAsJsonArray();
			for(int i = 0; i < songArr.size(); i++) {
				sounds.add(TinySound.loadSound(songArr.get(i).getAsString()));
			}
			soundMap.put(entry.getKey(), sounds);
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
	
	public Music getRandomSong(String groupName) {
		ArrayList<Music> mList = songMap.get(groupName);
		if(mList == null) return null;
		int nextIndex = RandomSource.nextInt() % mList.size();
		return mList.get(nextIndex);
	}
	
	public Sound getRandomSound(String groupName) {
		ArrayList<Sound> mList = soundMap.get(groupName);
		if(mList == null) return null;
		int nextIndex = RandomSource.nextInt() % mList.size();
		return mList.get(nextIndex);
	}
	
	public static SoundContext loadFromZip(ZipFile zf) {
		SoundContext ctxt = new SoundContext();
		try {
			InputStream jIS = zf.getInputStream(zf.getEntry("songs/songs.json"));
			JsonObject obj = new JsonParser().parse(new InputStreamReader(jIS)).getAsJsonObject();
			ctxt.name = obj.get("name").getAsString();
			for(Entry<String, JsonElement> entry : obj.get("songs").getAsJsonObject().entrySet()) {
				ArrayList<Music> songs = new ArrayList<Music>();
				JsonArray songArr = entry.getValue().getAsJsonArray();
				for(int i = 0; i < songArr.size(); i++) {
					InputStream sIS = zf.getInputStream(zf.getEntry(songArr.get(i).getAsString()));
					songs.add(TinySound.loadMusic(sIS));
				}
				ctxt.songMap.put(entry.getKey(), songs);
			}
			for(Entry<String, JsonElement> entry : obj.get("sounds").getAsJsonObject().entrySet()) {
				ArrayList<Sound> sounds = new ArrayList<Sound>();
				JsonArray songArr = entry.getValue().getAsJsonArray();
				for(int i = 0; i < songArr.size(); i++) {
					InputStream sIS = zf.getInputStream(zf.getEntry(songArr.get(i).getAsString()));
					sounds.add(TinySound.loadSound(sIS));
				}
				ctxt.soundMap.put(entry.getKey(), sounds);
			}
		} catch(Exception ex) {
			return null;
		}
		return ctxt;
	}
}
