package daedalus.sound;

public class SoundSystem {
	
}

/*
import java.util.HashMap;
import java.util.Map;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

public class SoundSystem {
	private static Map<String, SoundContext> ctxtMap;
	private static Music nowPlaying;
	
	public static void init() {
		TinySound.init();
		ctxtMap = new HashMap<String, SoundContext>();
		nowPlaying = null;
	}
	
	public static void addContext(SoundContext ctxt) {
		ctxtMap.put(ctxt.getName(), ctxt);
	}
	
	public static void playSong(String song) {
		int split = song.indexOf('/');
		SoundContext ctxt = ctxtMap.get(song.substring(0, split));
		Music toPlay = ctxt.getRandomSong(song.substring(split + 1));
		if(nowPlaying != null && nowPlaying.playing()) nowPlaying.stop();
		toPlay.play(false);
		nowPlaying = toPlay;
	}
	
	public static void playSound(String sound) {
		int split = sound.indexOf('/');
		SoundContext ctxt = ctxtMap.get(sound.substring(0, split));
		Sound toPlay = ctxt.getRandomSound(sound.substring(split + 1));
		toPlay.play();
	}
	
	public static void shutdown() {
		TinySound.shutdown();
	}
}
*/