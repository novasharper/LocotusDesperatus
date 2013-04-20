package daedalus.graphics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpriteEngine {
	private static Map<String, Sprite> spriteMap;
	
	static {
		spriteMap = new HashMap<String, Sprite>();
	}
	
	public static Sprite getSprite(String name) {
		Sprite sprite = spriteMap.get(name);
		if(sprite == null) throw new RuntimeException("Sprite not found.");
		return sprite;
	}
	
	public static void registerSprite(String name, Sprite sprite) {
		if(sprite == null || name == null) throw new RuntimeException();
		if(spriteMap.containsKey(name)) throw new RuntimeException();
		spriteMap.put(name, sprite);
	}
	
	public static void loadSprite(String name, String path) throws IOException {
		registerSprite(name, new Sprite(path));
	}
}
