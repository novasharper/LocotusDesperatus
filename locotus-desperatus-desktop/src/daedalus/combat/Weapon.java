package daedalus.combat;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.Physics;
import daedalus.entity.Entity;
import daedalus.graphics.GraphicsElement;
import daedalus.graphics.Sprite;
import daedalus.graphics.SpriteEngine;


public abstract class Weapon implements GraphicsElement {
	protected Entity wielder;
	protected int power_loaded;
	protected int power_reserve;
	
	public Weapon(Entity wielder) {
		this.wielder = wielder;
		power_loaded = getMaxLoad();
		power_reserve = getMaxReserve();
	}
	
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		Point2D.Double drawLoc = wielder.getDrawLoc();
		double rot = wielder.getRot();
		sr.begin(ShapeType.Line);
		sr.setColor(Color.WHITE);
		double leny = Gdx.graphics.getHeight() / 2, lenx = Gdx.graphics.getWidth() / 2;
		double leny2 = Math.min(leny, lenx * Math.abs(Math.sin(rot) / Math.cos(rot)));
		double lenx2 = Math.min(lenx, leny * Math.abs(Math.cos(rot) / Math.sin(rot)));
		double len = Math.sqrt(lenx2 * lenx2 + leny2 * leny2);
		sr.line((float) (drawLoc.x + 20 * Math.cos(rot)), (float) (drawLoc.y + 20 * Math.sin(rot)),
				(float) (drawLoc.x + len * Math.cos(rot)), (float) (drawLoc.y + len * Math.sin(rot)));
		sr.end();
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
