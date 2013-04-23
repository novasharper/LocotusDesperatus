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
	private int xRadius, yRadius;
	
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
			SpriteEngine.loadSprite("ir32.hudsprite", "test/res/rifle_hud.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		tm = this;
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
		chief.render(sb, sr);
	}
	
	public void init() {
		xRadius = (int) (GameComponent.getGame().width / GameComponent.tileSize) / 2 + 1;
		yRadius = (int) (GameComponent.getGame().height / GameComponent.tileSize) / 2 + 1;
		map = new Level(30, 20);
		for(int r = 0; r < map.getHeight(); r++) {
			for(int c = 0; c < map.getWidth(); c++) {
				map.getTile(c, r).setPassable(Math.random() < .8);
			}
		}
		Physics.setLevel(map);
	}
	
	public void tick() {
		chief.tick();
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
