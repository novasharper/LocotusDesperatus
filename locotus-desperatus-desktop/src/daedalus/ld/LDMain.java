package daedalus.ld;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.*;
import daedalus.combat.AssaultRifle;
import daedalus.entity.*;
import daedalus.graphics.*;
import daedalus.input.Gamepad;
import daedalus.level.*;
import daedalus.main.GameComponent;
import daedalus.main.GameContext;
import daedalus.main.Path;



public class LDMain extends GameContext {
	private Gamepad gamepad;
	private Level map;
	public Hero chief;
	public List<Entity> entities;
	public static LDMain ldm;
	private int xRadius, yRadius;
	
	public LDMain() {
		entities = new ArrayList<Entity>();
		AIEntity other = new AIEntity("Chief");
		other.setX(0.5);
		other.setY(0.5);
		entities.add(other);
		chief = new Hero();
		chief.setX(0.5);
		chief.setY(0.5);
		chief.equip(new AssaultRifle(chief));
		entities.add(chief);
		try {
			SpriteEngine.loadSprite("ma5c.hudsprite", "ld/res/rifle_hud.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		ldm = this;
	}
	@Override
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		sr.begin(ShapeType.Filled);
		sr.setColor(Color.BLACK);
		sr.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		sr.setColor(0.7f, 0.8f, 1f, 1f);
		float fuzzX = (float) chief.getLoc().x % 2;
		float fuzzY = (float) chief.getLoc().y % 2;
		if (Math.abs(fuzzX) > 1)
			fuzzX = fuzzX % 1f;
		if (Math.abs(fuzzY) > 1)
			fuzzY = fuzzY % 1f;
		
		for(int r = -yRadius; r <= yRadius; r++) {
			for(int c = -xRadius; c <= xRadius; c++) {
				Tile tile = map.getTile(c + (int) chief.getLoc().x, r + (int) chief.getLoc().y);
				if(tile == null || !tile.isPassable()) {
					sr.rect((float) chief.getDrawX() + (c - fuzzX) * GameComponent.tileSize,
							(float) chief.getDrawY() + (r - fuzzY) * GameComponent.tileSize,
							GameComponent.tileSize, GameComponent.tileSize);
				}
			}
		}
		sr.end();
		for(Entity entity : entities) {
			entity.render(sb, sr);
		}
	}
	
	public void init() {
		xRadius = (int) (GameComponent.getGame().width / GameComponent.tileSize) / 2 + 1;
		yRadius = (int) (GameComponent.getGame().height / GameComponent.tileSize) / 2 + 1;
		map = new Level(7, 7);
		for(int r = 0; r < map.getHeight(); r++) {
			for(int c = 0; c < map.getWidth(); c++) {
				map.getTile(c, r).setPassable(Math.random() < .8 ||
						((r - chief.getLoc().y) * (r - chief.getLoc().y) + (c - chief.getLoc().x) * (c - chief.getLoc().x) <= 1.5 * 1.5) ||
						((r - map.getHeight() + 2) * (r - map.getHeight() + 2) + (c - map.getWidth() + 2) * (c - map.getWidth() + 2) <= 1.5 * 1.5));
			}
		}
		Physics.setLevel(map);
	}
	
	public void tick() {
		for(int i = 0; i < entities.size(); i++) {
			entities.get(i).tick();
			if(entities.get(i).isDead() && entities.get(i) != chief) {
				entities.remove(i);
				i--;
			}
		}
	}
	
	public Tile getTile(int x, int y) {
		return map.getTile(x, y);
	}
	
	public boolean isTransparent() {
		return false;
	}
	
	public static void main(String[] args) {
		GameComponent.create("Locotus Desperatus", 1280, 704, true, true);
		GameComponent.getGame().pushContext(new LDMain());
		GameComponent.getGame().pushContext(new HUD(ldm.chief));
		GameComponent.getGame().start();
		GameComponent.getGame().setPaused(false);
	}
}
