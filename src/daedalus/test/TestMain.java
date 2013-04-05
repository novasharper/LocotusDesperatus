package daedalus.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import daedalus.*;
import daedalus.combat.IR32;
import daedalus.entity.*;
import daedalus.graphics.*;
import daedalus.gui.*;
import daedalus.input.Gamepad;
import daedalus.level.*;
import daedalus.main.GameComponent;
import daedalus.main.GameContext;
import daedalus.settings.Options;



public class TestMain extends GameContext {
	private BufferedImage img;
	private Gamepad gamepad;
	private Level map;
	private static Entity chief;
	private static Entity other;
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
	
	public void render(Graphics2D gr) {
		Color saveColor = gr.getColor();
		gr.setColor(new Color(0.7f, 0.8f, 1f));
		gr.fillRect(0, 0, GameComponent.getGame().width, GameComponent.getGame().height);
		gr.setColor(Color.black);
		for(int r = 0; r < map.getHeight(); r++) {
			for(int c = 0; c < map.getWidth(); c++) {
				if(!map.getTile(c, r).isPassable()) {
					gr.fillRect(
						(int)(c * GameComponent.tileSize  - (int) ((BasicStroke) gr.getStroke()).getLineWidth() / 2),
						(int)(r * GameComponent.tileSize  - (int) ((BasicStroke) gr.getStroke()).getLineWidth() / 2),
						GameComponent.tileSize + (int) ((BasicStroke) gr.getStroke()).getLineWidth(),
						GameComponent.tileSize + (int) ((BasicStroke) gr.getStroke()).getLineWidth()
					);
				}
			}
		}		
		gr.setColor(saveColor);
		chief.render(gr);
		other.render(gr);
	}
	
	public void init() {
		map = new Level((int) (GameComponent.getGame().width / GameComponent.tileSize),
				GameComponent.getGame().height / GameComponent.tileSize);
		for(int r = 0; r < map.getHeight(); r++) {
			for(int c = 0; c < map.getWidth(); c++) {
				map.getTile(c, r).setPassable(Math.random() < .8);
			}
		}
	}
	
	public void tick() {
		//Gamepad g = GameComponent.getGamePad();
		chief.tick();
		other.tick();
		other.setRot(other.getRot() + Math.PI / 180);
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
