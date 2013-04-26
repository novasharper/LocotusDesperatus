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
import daedalus.level.Level;
import daedalus.level.Tile;
import daedalus.main.GameComponent;


public abstract class Weapon implements GraphicsElement {
	protected Entity wielder;
	protected int power_loaded;
	protected int power_reserve;
	protected int range;
	
	public Weapon(Entity wielder) {
		this.wielder = wielder;
		power_loaded = getMaxLoad();
		power_reserve = getMaxReserve();
		range = 10;
	}
	
	public double fireLen() {
		int res = GameComponent.tileSize;
		
		double rot = wielder.getRot();
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
		Point2D.Double drawLoc = wielder.getDrawLoc();
		double rot = wielder.getRot();
		sr.begin(ShapeType.Line);
		sr.setColor(Color.WHITE);
		double leny = Gdx.graphics.getHeight() / 2, lenx = Gdx.graphics.getWidth() / 2;
		double leny2 = Math.min(leny, lenx * Math.abs(Math.sin(rot) / Math.cos(rot)));
		double lenx2 = Math.min(lenx, leny * Math.abs(Math.cos(rot) / Math.sin(rot)));
		double len = Math.sqrt(lenx2 * lenx2 + leny2 * leny2);
		len = Math.min(Math.max(20, fireLen()), 3 * GameComponent.tileSize);
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
