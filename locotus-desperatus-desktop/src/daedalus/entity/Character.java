package daedalus.entity;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

import daedalus.Physics;
import daedalus.combat.Weapon;
import daedalus.graphics.SpriteEngine;
import daedalus.level.Tile;
import daedalus.main.GameComponent;


public class Character extends Entity {
	protected double speed;
	
	public Character(String name) {
		super(name, 100);
		speed = 2;
	}
	
	public void tick() {
		double dxl = GameComponent.getGamePad().getXLeft();
		double dyl = GameComponent.getGamePad().getYLeft();
		double dxr = GameComponent.getGamePad().getXRight();
		double dyr = GameComponent.getGamePad().getYRight();
		// Allow strafing
		if(dxr * dxr + dyr * dyr >= GameComponent.getGamePad().rot_deadzone)
			rotation = 2 * Math.PI - Math.atan2(dyr, dxr);
		else if(dxl * dxl + dyl * dyl >= GameComponent.getGamePad().rot_deadzone)
			rotation = 2 * Math.PI - Math.atan2(dyl, dxl);
		if(Math.abs(dxl) < 0.1) dxl = 0;
		if(Math.abs(dyl) < 0.1) dyl = 0;
		double deltay = dyl * speed / GameComponent.framerate;
		double deltax = dxl * speed / GameComponent.framerate;
		Tile t1 = Physics.getLevel().getTile((int) location.x, (int) Math.floor(location.y - deltay));
		Tile t2 = Physics.getLevel().getTile((int) Math.floor(location.x + deltax), (int) location.y);
		if(t1 == null || !t1.isPassable())
			deltay = 0;
		if(t2 == null || !t2.isPassable())
			deltax = 0;
		location.y -= deltay;
		location.x += deltax;
		super.tick();
	}
}
