package daedalus.level;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import daedalus.graphics.Sprite;
import daedalus.graphics.SpriteEngine;
import daedalus.sound.SoundContext;
import daedalus.sound.SoundSystem;

public class Level {
	public static class LevelInfo {
		@Expose private String name;
		
		@Expose private Map<Integer, TileInfo> tile_info;
		
		public LevelInfo() {
			tile_info = new HashMap<Integer, TileInfo>();
		}
	}
	
	public static class TileInfo {
		private int id;
		private boolean passable;
		private String name;
		private String spriteName;
		private TILE_TYPE type;
		
		public static enum TILE_TYPE {
			EMPTY,
			BLOCK,
			SPAWNER,
			ENTITY;
		}
		
		public TileInfo(int id, boolean passable, String name, int type) {
			this(id, passable, name, TILE_TYPE.values()[type]);
		}
		
		public TileInfo(int id, boolean passable, String name, TILE_TYPE type) {
			this.id = id;
			this.name = name;
			this.type = type;
			this.spriteName = name;
		}
		
		public int getID() {
			return id;
		}
		
		public TILE_TYPE getType() {
			return type;
		}
		
		public boolean getPassable() {
			return passable;
		}
		
		public String getName() {
			return name;
		}
		
		public Sprite getSprite() {
			return SpriteEngine.getSprite(name);
		}
	}
	
	private BufferedImage background;
	private int width, height;
	private Tile[][] map;
	private LevelInfo level_info;
	
	public Level(int width, int height) {
		this.width = width;
		this.height = height;
		this.map = new Tile[height][width];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				map[y][x] = new Tile(x, y, null, 0, 0, 0);
			}
		}
	}
	
	public Level(String name) {
		try {
			ZipFile zf = new ZipFile(name + ".level");
			Gson gson = new Gson();
			// Read in level info
			InputStream is = zf.getInputStream(zf.getEntry("info.json"));
			level_info = gson.fromJson(new InputStreamReader(is), LevelInfo.class);
			// Read sprites
			for(Entry<Integer, TileInfo> entry : level_info.tile_info.entrySet()) {
				is = zf.getInputStream(zf.getEntry("tiles/" + entry.getValue().spriteName + ".png"));
				SpriteEngine.registerSprite(entry.getValue().spriteName, new Sprite(is));
			}
			// Read in level
			is = zf.getInputStream(zf.getEntry("data.png"));
			BufferedImage img = ImageIO.read(is);
			width = img.getWidth();
			height = img.getHeight();
			// Parse level
			map = new Tile[height][width];
			for(int r = 0; r < height; r++) {
				for(int c = 0; c < width; c++) {
					int pixel = img.getRGB(c, r);
					int cost_raw = (pixel >> 24) & 0xFF; // Alpha
					int cost = (0xFF - cost_raw) * 8 / 0x100 + 1; // Cost will be 1-8
					int id = (pixel >> 16) & 0xFF; // Red
					int data = (pixel >> 8) & 0xFF; // Green
					int ext_data = (pixel >> 0) & 0xFF; // Blue
					map[r][c] = new Tile(c, r, getInfo(id), cost, data, ext_data);
				}
			}
			// Possibly read in backgound
			if(zf.getEntry("background.png") != null) {
				is = zf.getInputStream(zf.getEntry("background.png"));
				background = ImageIO.read(is);
			}
			// Load level-specific sound context
			SoundContext ctxt = SoundContext.loadFromZip(zf);
			if(ctxt != null)
				SoundSystem.addContext(ctxt);
			// Close zip file
			zf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Tile getTile(int x, int y) {
		if(x < 0 || x >= width) throw new IndexOutOfBoundsException();
		if(y < 0 || y >= width) throw new IndexOutOfBoundsException();
		return map[y][x];
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public TileInfo getInfo(int id) {
		return level_info.tile_info.get(id);
	}
	
	public void reset() {
		for(int r = 0; r < height; r++) {
			for(int c = 0; c < width; c++) {
				map[r][c].reset();
			}
		}
	}
}