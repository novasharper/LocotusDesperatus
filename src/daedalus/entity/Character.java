package daedalus.entity;

import java.util.ArrayList;

import daedalus.combat.Weapon;
import daedalus.graphics.SpriteEngine;
import daedalus.main.GameComponent;


public class Character extends Entity {
	protected double speed;
	
	public Character(String name) {
		super(name, 100);
		speed = 4;
	}
	
	public void tick() {
		double dxl = GameComponent.getGamePad().getXLeft();
		double dyl = GameComponent.getGamePad().getYLeft();
		double dxr = GameComponent.getGamePad().getXRight();
		double dyr = GameComponent.getGamePad().getYRight();
		// Allow strafing
		if(dxr * dxr + dyr * dyr >= GameComponent.getGamePad().rot_deadzone)
			rotation = Math.atan2(dyr, dxr);
		else if(dxl * dxl + dyl * dyl >= GameComponent.getGamePad().rot_deadzone)
			rotation = Math.atan2(dyl, dxl);
		location.y += dyl * speed / GameComponent.framerate;
		location.x += dxl * speed / GameComponent.framerate;
		super.tick();
	}
}
