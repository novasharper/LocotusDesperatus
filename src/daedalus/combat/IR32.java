package daedalus.combat;

import java.awt.Graphics2D;

import daedalus.entity.Entity;


public class IR32 extends Weapon {

	public IR32(Entity wielder) {
		super(wielder);
	}
	
	public void tick() {
	}
	
	public String getName() {
		return "ir32";
	}
	
	public float getDamage() {
		return 0.5f;
	}
	
	public int getPowerPerUse() {
		return 1;
	}
	
	public int getMaxLoad() {
		return 250;
	}
	
	public int getMaxReserve() {
		// TODO Auto-generated method stub
		return 1000;
	}

}
