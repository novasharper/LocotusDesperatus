package daedalus.combat;

import java.awt.geom.Point2D;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.Physics;
import daedalus.entity.Entity;
import daedalus.graphics.GraphicsElement;
import daedalus.input.F310;
import daedalus.input.GamepadEvent;
import daedalus.input.GamepadEvent.ComponentType;
import daedalus.input.GamepadEvent.EventType;
import daedalus.input.IGamepadEventHandler;
import daedalus.ld.LDMain;
import daedalus.level.Level;
import daedalus.level.Tile;
import daedalus.main.GameComponent;


public abstract class Weapon implements GraphicsElement, IGamepadEventHandler {
	protected Entity wielder;
	protected int power_loaded;
	protected int power_reserve;
	protected int range;
	protected boolean aimAssist = true;
	protected double assistRange = Math.PI / 4;
	
	public Weapon(Entity wielder) {
		this.wielder = wielder;
		power_loaded = getMaxLoad();
		power_reserve = getMaxReserve();
		range = 5;
		if(!wielder.isAI()) GameComponent.getGamePad().addEventHandler(this);
	}
	
	public double getRotAimbot() {
		if(!aimAssist) return wielder.getRot();
		for(Entity entity : LDMain.ldm.entities) {
			if(entity == wielder) continue;
			else if(wielder.hasLOS(entity, assistRange, -1) && wielder.getLoc().distance(entity.getLoc()) <= range) {
				double angle = Math.PI + Math.atan2(wielder.getDrawY() - entity.getDrawY(), wielder.getDrawX() - entity.getDrawX());
				double delta = Math.abs((wielder.getRot() + Math.PI - angle) % (Math.PI * 2) - Math.PI);
				if(Math.abs(delta) > assistRange) return wielder.getRot();
				return angle;
			}
		}
		return wielder.getRot();
	}
	
	double ticks = 0;
	public void fire() {
		Entity target = null;
		double cd = 0;
		for(Entity entity : LDMain.ldm.entities) {
			double nd = wielder.getLoc().distance(entity.getLoc());
			if(wielder.hasLOS(entity, Math.PI / 48, getRotAimbot()) && wielder.getLoc().distance(entity.getLoc()) <= range) {
				if((target == null || nd < cd) && nd * GameComponent.tileSize > 10) {
					target = entity;
					cd = nd;
				}
			}
		}
		target(target);
	}
	
	private boolean toggle = false;
	public void tick() {
		boolean shouldFire = shouldFire();
		if(!shouldFire) {
			toggle = false;
			return;
		}
		boolean forceFire = (toggle == false) && shouldFire;
		toggle = true;
		if(forceFire) fire();
		else {
			ticks += 1000 / GameComponent.framerate;
			while(ticks >= 1000 / roundsPerSecond()) {
				fire();
				ticks -= 1000 / roundsPerSecond();
			}
		}
	}
	
	private double fireLen() {
		int res = GameComponent.tileSize;
		
		double rot = getRotAimbot();
		double leny2 = range * res * Math.abs(Math.sin(rot));
		double lenx2 = range * res * Math.abs(Math.cos(rot));
		
		int x0 = (int) (wielder.getLoc().x * res);
		int x1 = (int) Math.floor(x0 + lenx2 * Math.signum(Math.cos(rot)));
		int y0 = (int) (wielder.getLoc().y * res);
		int y1 = (int) Math.floor(y0 + leny2 * Math.signum(Math.sin(rot)));
		int dx =  Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
	    int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
	    int err = dx + dy, e2;
	    int x = x0;
	    int y = y0;
	    
		Level lvl = Physics.getLevel();
		
	    for (;;) {
	    	Tile tile = lvl.getTile((int) Math.floor(x0 * 1.0 / res), (int) Math.floor(y0 * 1.0 / res));
	    	if(tile == null || !tile.isPassable()) break;

	        if (x0 == x1 && y0 == y1) break;

	        e2 = 2 * err;

	        // EITHER horizontal OR vertical step (but not both!)
	        if (e2 > dy) { 
	            err += dy;
	            x0 += sx;
	        } else if (e2 < dx) { // <--- this "else" makes the difference
	            err += dx;
	            y0 += sy;
	        }
	    }
	    return Math.sqrt(Math.pow(x0 - x, 2) + Math.pow(y0 - y, 2));
	}
	
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		if(!shouldDrawFire() || getLoad() == 0) return;
		Point2D.Double drawLoc = wielder.getDrawLoc();
		double rot = getRotAimbot();
		sr.begin(ShapeType.Line);
		sr.setColor(Color.WHITE);
		double len = Math.max(20, fireLen());
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
			if(target != null) target.damage(getDamage() * (wielder.isAI() ? 0.5f : 1.0f));
			power_loaded -= 1;
	}
	
	public abstract boolean shouldFire();
	public abstract boolean shouldDrawFire();
	public abstract float getDamage();
	public abstract int getMaxLoad();
	public abstract int getMaxReserve();
	public abstract int getPowerPerUse();
	public abstract double roundsPerSecond();
	public abstract void trigger();
	public abstract void releaseTrigger();
	
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
	
	public void reload() {
		int delta = getMaxLoad() - power_loaded;
		power_loaded += Math.max(Math.min(power_reserve, delta), 0);
		power_reserve -= delta;
		power_reserve = Math.max(power_reserve, 0);
	}
	
	public void changeWielder(Entity wielder) {
		this.wielder = wielder;
	}

	@Override
	public void handleInput(GamepadEvent ev) {
		if(ev.getButtonID() == F310.BUTTON_L && ev.getCType() == ComponentType.BUTTON &&
				ev.getType() == EventType.RELEASED && !shouldFire()) {
			reload();
		}
	}
}
