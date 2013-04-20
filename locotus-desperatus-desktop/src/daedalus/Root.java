package daedalus;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Root {
	private static Map<Integer, BitmapFont> fonts = null;
	private static FreeTypeFontGenerator fontGen = null;
	
	public static synchronized BitmapFont getFont(int fontSize) {
		if(fonts == null) fonts = new HashMap<Integer, BitmapFont>();
		if(!fonts.containsKey(fontSize)) {
			if(fontGen == null) fontGen = new FreeTypeFontGenerator(Gdx.files.internal("data/arial_bold.ttf"));
			fonts.put(fontSize, fontGen.generateFont(fontSize));
		}
		return fonts.get(fontSize);
	}
	
	public static BitmapFont getFont(int fontSize, String path) {
		FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(Gdx.files.internal(path));
		BitmapFont font = fontGen.generateFont(fontSize);
		fontGen.dispose();
		return font;
	}
	
	public static void reloadFonts() {
		if(fontGen == null || fonts == null) return;
		fontGen.dispose();
		fontGen = new FreeTypeFontGenerator(Gdx.files.internal("data/arial_bold.ttf"));
		for(int size : fonts.keySet()) {
			fonts.put(size, fontGen.generateFont(size));
		}
	}
	
	public static FileHandle loadFile(String fname) {
		return Gdx.files.internal(fname);
	}
	
	public static Texture loadTexture(InputStream is) {
		try {
			Gdx2DPixmap gPixmap = new Gdx2DPixmap(is, Gdx2DPixmap.GDX2D_FORMAT_RGB888);
			Pixmap pixmap = new Pixmap(gPixmap);
			return new Texture(pixmap);
		} catch (Exception e) {
			return null;
		}
	}
}
