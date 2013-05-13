package daedalus.sound;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.openal.OpenALAudio;

public class SoundSystem {
	private static Map<String, SoundContext> ctxtMap;
	private static Music nowPlaying;
	private static String nowPlayingString;
	private static long startn;
	private static boolean looping = false;
	private static double maxSongTimeLimit = 7.0;
	public static final OpenALAudio audio = new OpenALAudio();
	
	public static void init() {
		ctxtMap = new HashMap<String, SoundContext>();
		nowPlaying = null;
	}
	
	public static void addContext(SoundContext ctxt) {
		ctxtMap.put(ctxt.getName(), ctxt);
	}
	
	public static void playSong(String song) {
		playSong(song, false);
	}
	
	public static void playSong(String song, boolean looping) {
		SoundSystem.looping = looping;
		int split = song.indexOf('/');
		SoundContext ctxt = ctxtMap.get(song.substring(0, split));
		Music toPlay = ctxt.getRandomSong(song.substring(split + 1));
		if(nowPlaying != null && nowPlaying.isPlaying()) nowPlaying.stop();
		toPlay.play();
		nowPlaying = toPlay;
		nowPlayingString = song;
		startn = System.nanoTime();
	}
	
	public static void playSound(String sound) {
		int split = sound.indexOf('/');
		SoundContext ctxt = ctxtMap.get(sound.substring(0, split));
		Sound toPlay = ctxt.getRandomSound(sound.substring(split + 1));
		toPlay.play();
	}
	
	public static void shutdown() {
		audio.dispose();
	}
	
	public static boolean isPlaying() {
		return nowPlaying != null && nowPlaying.isPlaying();
	}
	
	public static void next() {
		if(nowPlayingString != null) {
			playSong(nowPlayingString);
		}
	}
	
	public static void update() {
		if(nowPlayingString != null && System.nanoTime() - startn >= (long) (maxSongTimeLimit * 60000000000L)) {
			if(looping) {
				SoundSystem.next();
			} else {
				nowPlaying.stop();
				nowPlaying = null;
				nowPlayingString = null;
			}
		}
		SoundSystem.audio.update();
	}
	
	public static SoundContext getContext(String name) {
		return ctxtMap.get(name);
	}
}
