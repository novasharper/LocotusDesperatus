package daedalus.level;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.openal.OpenALAudio;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.Color;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import daedalus.entity.Entity;
import daedalus.graphics.Sprite;
import daedalus.graphics.SpriteEngine;
import daedalus.main.GameComponent;
import daedalus.sound.SoundContext;
import daedalus.sound.SoundSystem;
import daedalus.util.RandomSource;
import daedalus.util.Util;

public class Level {
	public static class LevelInfo {
		private String name;
		
		private Map<Integer, TileInfo> tile_info;
		
		public LevelInfo() {
			tile_info = new HashMap<Integer, TileInfo>();
		}
	}
	
	public static class TileInfo {
		private int id;
		private boolean passable;
		private String name;
		private TILE_TYPE type;
		private Color tileColor = new Color(0.7f, 0.8f, 1f, 1f);
		
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
	}
	
	private int width, height;
	private Tile[][] map;
	private LevelInfo level_info;
	private List<Entity> entities = new ArrayList<Entity>();
	
	public Level(int width, int height) {
		this.width = width;
		this.height = height;
		this.map = new Tile[height][width];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				map[y][x] = new Tile(x, y, null, 1, 0, 0);
			}
		}
	}
	
	public Level(String name) {
		try {
			File levelDir = new File(Util.getWorkingDir(), "resources/level/" + name);
			Gson gson = new Gson();
			// Read in level info
			level_info = gson.fromJson(new FileReader(new File(levelDir, "info.json")), LevelInfo.class);
			// Read in level
			InputStream is = new FileInputStream(new File(levelDir, "data.png"));
			BufferedImage img = ImageIO.read(is);
			width = img.getWidth();
			height = img.getHeight();
			// Parse level
			map = new Tile[height][width];
			for(int r = 0; r < height; r++) {
				for(int c = 0; c < width; c++) {
					int pixel = img.getRGB(c, r);
					int cost = 1;
					int id = (pixel >> 16) & 0xFF; // Red
					int data = (pixel >> 8) & 0xFF; // Green
					int ext_data = (pixel >> 0) & 0xFF; // Blue
					map[r][c] = new Tile(c, r, getInfo(id), cost, data, ext_data);
				}
			}
			// Load level-specific sound context
			SoundContext ctxt = SoundContext.loadFromLevel(levelDir);
			if(ctxt != null)
				SoundSystem.addContext(ctxt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Tile getTile(int x, int y) {
		if(x < 0 || x >= width) return null;
		if(y < 0 || y >= height) return null;
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
	
	public void render(Entity character, ShapeRenderer sr, SpriteBatch sb) {
		sr.begin(ShapeType.Filled);
		sr.setColor(0.7f, 0.8f, 1f, 1f);
		float fuzzX = (float) character.getLoc().x % 2;
		float fuzzY = (float) character.getLoc().y % 2;
		if (Math.abs(fuzzX) > 1)
			fuzzX = fuzzX % 1f;
		if (Math.abs(fuzzY) > 1)
			fuzzY = fuzzY % 1f;
		int xRadius = Gdx.graphics.getWidth() / (2 * GameComponent.tileSize) + 1;
		int yRadius = Gdx.graphics.getHeight() / (2 * GameComponent.tileSize) + 1;
		for(int r = -yRadius; r <= yRadius; r++) {
			for(int c = -xRadius; c <= xRadius; c++) {
				Tile tile = getTile(c + (int) character.getLoc().x, r + (int) character.getLoc().y);
				if(tile == null || !tile.isPassable()) {
					if(tile != null && tile.getInfo() != null) {
						sr.setColor(tile.getInfo().tileColor);
					} else {
						sr.setColor(0.7f, 0.8f, 1f, 1f);
					}
					sr.rect((float) character.getDrawX() + (c - fuzzX) * GameComponent.tileSize,
							(float) character.getDrawY() + (r - fuzzY) * GameComponent.tileSize,
							GameComponent.tileSize, GameComponent.tileSize);
				}
			}
		}
		sr.end();
		for(Entity entity : entities) {
			entity.render(sb, sr);
		}
	}
	
	public void update(Entity character) {
		for(int i = 0; i < entities.size(); i++) {
			entities.get(i).tick();
			if(entities.get(i).isDead() && entities.get(i) != character) {
				entities.remove(i);
				i--;
			}
		}
	}
	
	public void addEntity(Entity e) {
		if(!entities.contains(e)) entities.add(e);
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public void startBackgroundMusic() {
		SoundSystem.playSong(level_info.name + "/background", true);
	}
	
	public static void main(String[] args) {
		SoundSystem.init();
		Level level = new Level("test");
		SoundSystem.playSong("test/background");
		while(SoundSystem.isPlaying()) {
			SoundSystem.update();
		}
		SoundSystem.shutdown();
	}
}