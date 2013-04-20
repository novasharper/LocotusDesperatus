package daedalus.test;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.*;
import daedalus.anim.TranslationAnimation;
import daedalus.combat.IR32;
import daedalus.entity.*;
import daedalus.entity.Character;
import daedalus.graphics.*;
import daedalus.gui.*;
import daedalus.input.Gamepad;
import daedalus.level.*;
import daedalus.main.GameComponent;
import daedalus.main.GameContext;
import daedalus.main.Path;
import daedalus.settings.Options;



public class TestMain extends GameContext {
	private Gamepad gamepad;
	private Level map;
	private static Character chief;
	private static NPC other;
	public static TestMain tm;
	
	public static Entity getHero() {
		return chief;
	}
	
	public TestMain() {
		other = new NPC("chief");
		other.setX(0.5);
		other.setY(0.5);
		chief = new Hero();
		chief.setX(10.5);
		chief.setY(5.5);
		chief.equip(new IR32(chief));
		try {
			SpriteEngine.loadSprite("chief.sprite", "test/res/sprite.png");
			SpriteEngine.loadSprite("ir32.sprite", "test/res/rifle.png");
			SpriteEngine.loadSprite("ir32.hudsprite", "test/res/rifle_hud.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		tm = this;
	}

	@Override
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		sr.begin(ShapeType.Filled);
		sr.setColor(0.7f, 0.8f, 1f, 1f);
		sr.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		sr.setColor(Color.BLACK);
		for(int r = 0; r < map.getHeight(); r++) {
			for(int c = 0; c < map.getWidth(); c++) {
				if(!map.getTile(c, r).isPassable()) {
					sr.rect(c * GameComponent.tileSize, r * GameComponent.tileSize, GameComponent.tileSize, GameComponent.tileSize);
				}
			}
		}
		sr.end();
		chief.render(sb, sr);
		other.render(sb, sr);
	}
	
	public void init() {
		map = new Level((int) (GameComponent.getGame().width / GameComponent.tileSize),
				GameComponent.getGame().height / GameComponent.tileSize);
		for(int r = 0; r < map.getHeight(); r++) {
			for(int c = 0; c < map.getWidth(); c++) {
				map.getTile(c, r).setPassable(Math.random() < .8);
			}
		}
		Point[] path = Pathfinding.ASTAR(map, map.getTile(0, 0), map.getTile(map.getWidth() - 1, map.getHeight() - 1));
		
		other.setPath(new Path(path, true, true));
	}
	
	public void tick() {
		chief.tick();
		other.tick();
	}
	
	public Tile getTile(int x, int y) {
		return map.getTile(x, y);
	}
	
	public boolean isTransparent() {
		return false;
	}
	
	public static void main(String[] args) {
		GameComponent.create("Locotus Desperatus", 1280, 704, true);
		GameComponent.getGame().pushContext(new TestMain());
		GameComponent.getGame().pushContext(new HUD(chief));
		GameComponent.getGame().start();
		GameComponent.getGame().setPaused(false);
	}
}
