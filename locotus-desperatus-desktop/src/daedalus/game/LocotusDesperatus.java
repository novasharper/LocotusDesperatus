package daedalus.game;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.*;
import daedalus.combat.AssaultRifle;
import daedalus.entity.*;
import daedalus.graphics.*;
import daedalus.level.*;
import daedalus.main.GameComponent;
import daedalus.main.GameContext;



public class LocotusDesperatus extends GameContext {
	private Level map;
	private Hero character;
//	public List<Entity> entities;
	private int xRadius, yRadius;
	private Spawner spawner;
	
	public LocotusDesperatus() {
		Random rand = new Random();
		map = new Level(30, 20);

		character = new Hero();
		character.setX(0.5);
		character.setY(0.5);
		character.equip(new AssaultRifle(character));
		map.getEntities().add(character);
		spawner = new Spawner(AIEntity.class, new Point(1, 1), 1.5);
		
		for(int r = 0; r < map.getHeight(); r++) {
			for(int c = 0; c < map.getWidth(); c++) {
				map.getTile(c, r).setPassable(rand.nextDouble() < .8 ||
						((r - character.getLoc().y) * (r - character.getLoc().y) + (c - character.getLoc().x) * (c - character.getLoc().x) <= 2.5 * 2.5) ||
						((r - map.getHeight() + 2) * (r - map.getHeight() + 2) + (c - map.getWidth() + 2) * (c - map.getWidth() + 2) <= 1.5 * 1.5));
			}
		}
		
		try {
			SpriteEngine.loadSprite("ma5c.hudsprite", "hud_sprite/assault_rifle.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(Entity e : map.getEntities()) {
			if(e instanceof AIEntity) {
				int xf = (int) (rand.nextDouble() * 30);
				int yf = (int) (rand.nextDouble() * 20);
				Point[] path = Pathfinding.ASTAR(map, map.getTile((int) e.getLoc().x, (int) e.getLoc().y), map.getTile(xf, yf));
				((AIEntity) e).setPath(path);
			}
		}
		Physics.setLevel(map);
	}
	@Override
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		map.render(character, sr, sb);
	}
	
	public void init() {
	}
	
	public void tick() {
		map.update(character);
		if(spawner != null)
			spawner.tick();
	}
	
	public Tile getTile(int x, int y) {
		return map.getTile(x, y);
	}
	
	public boolean isTransparent() {
		return false;
	}
	public Level getMap() {
		return map;
	}
	
	public Hero getHero() {
		return character;
	}
}
