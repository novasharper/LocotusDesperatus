package daedalus.ld;

import com.badlogic.gdx.Gdx;

import daedalus.entity.Character;
import daedalus.entity.NPC;

public class Hero extends Character {
	public Hero() {
		super("chief");
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
		if(hasLOS(LDMain.other)) colorIndex = 0;
		else colorIndex = 1;
	}
}
