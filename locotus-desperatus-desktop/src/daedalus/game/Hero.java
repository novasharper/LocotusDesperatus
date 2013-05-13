package daedalus.game;

import com.badlogic.gdx.Gdx;

import daedalus.entity.Entity;

public class Hero extends Entity {
	public Hero() {
		super(100, 2, false);
		colorIndex = 1;
	}
	
	public double getDrawX() {
		return Gdx.graphics.getWidth() / 2;
	}
	
	public double getDrawY() {
		return Gdx.graphics.getHeight() / 2;
	}
	
	public void tick() {
		super.tick();
//		if(hasLOS(LDMain.other)) colorIndex = 0;
//		else colorIndex = 1;
	}
}
