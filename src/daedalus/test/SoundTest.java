package daedalus.test;

import java.io.InputStream;
import java.util.zip.ZipFile;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.TinySound;

public class SoundTest {
	public static void main(String[] args) {
		TinySound.init();
		try {
			ZipFile zf = new ZipFile("test.level");
			InputStream is = zf.getInputStream(zf.getEntry("song.ogg"));
			Music muzak = TinySound.loadMusic(is);
			muzak.play(false);
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		TinySound.shutdown();
	}
}
