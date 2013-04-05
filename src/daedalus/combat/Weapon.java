package daedalus.combat;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import daedalus.entity.Entity;
import daedalus.graphics.GraphicsElement;
import daedalus.graphics.Sprite;
import daedalus.graphics.SpriteEngine;


public abstract class Weapon implements GraphicsElement {
	protected Entity wielder;
	protected int power_loaded;
	protected int power_reserve;
	protected Sprite owner;
	protected Sprite sprite;
	
	public Weapon(Entity wielder) {
		this.wielder = wielder;
		power_loaded = getMaxLoad();
		power_reserve = getMaxReserve();
	}
	
	public void render(Graphics2D gr) {
		if(sprite == null) sprite = SpriteEngine.getSprite(getSpriteName());
		if(owner == null) owner = SpriteEngine.getSprite(wielder.getSpriteName());
		
		Point2D.Double drawLoc = wielder.getDrawLoc();
		Point2D.Double drawCenter = (Point2D.Double) drawLoc.clone();
		
		drawLoc.x += owner.getWidth() - sprite.getWidth() / 2;
		drawLoc.y += owner.getHeight() - sprite.getHeight() / 2;
		
		drawCenter.x += owner.getCenter().getX();
		drawCenter.y += owner.getCenter().getY();
		
		sprite.render(drawLoc, drawCenter, wielder.getRot(), gr);
	}
	
	public abstract String getName();
	public String getSpriteName() {
		return getName() + ".sprite";
	}
	public String getHudSpriteName() {
		return getName() + ".hudsprite";
	}
	public void target(Entity target) {
		if(power_loaded <= 0) return;
		target.damage(getDamage());
		power_loaded -= 1;
	}
	
	public abstract float getDamage();
	public abstract int getMaxLoad();
	public abstract int getMaxReserve();
	public abstract int getPowerPerUse();
	
	public int getLoad() {
		return power_loaded;
	}
	
	public int getReserve() {
		return power_reserve;
	}
	
	public int getTotal() {
		return power_loaded + power_reserve;
	}
	
	public void recharge(int dPower) {
		power_loaded += dPower;
		if(power_loaded > getMaxLoad()) {
			power_reserve += power_loaded - getMaxLoad();
			power_loaded = getMaxLoad();
		}
		if(power_reserve > getMaxReserve()) {
			power_reserve = getMaxReserve();
		}
	}
	
	public void changeWielder(Entity wielder) {
		this.wielder = wielder;
	}
}
