package daedalus.entity;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.Physics;
import daedalus.combat.Weapon;
import daedalus.graphics.GraphicsElement;
import daedalus.main.GameComponent;
import daedalus.test.TestMain;


public abstract class Entity implements GraphicsElement {
	protected Point2D.Double location;
	protected double rotation;
	protected String name;
	protected float health;
	protected float maxHealth;
	protected long lastDamage;
	protected LinkedList<Weapon> arms;
	protected int colorIndex = 5;
	
	private static final Color[][] colorSet = {
		{ new Color(0.75f, 0f, 0f, 0.9f), new Color(1, 0, 0, 1) },
		{ new Color(0f, 0.75f, 0f, 0.9f), new Color(0, 1, 0, 1) },
		{ new Color(0f, 0f, 0.75f, 0.9f), new Color(0, 0, 1, 1) },
		{ new Color(0.75f, 0.75f, 0f, 0.9f), new Color(1, 1, 0, 1) },
		{ new Color(0f, 0.75f, 0.75f, 0.9f), new Color(0, 1, 1, 1) },
		{ new Color(0.75f, 0f, 0.75f, 0.9f), new Color(1, 0, 1, 1) }
	};
	
	public Entity(String name, float maxHealth) {
		this.name = name;
		this.location = new Point2D.Double();
		this.rotation = 0;
		this.maxHealth = maxHealth;
		this.health = maxHealth;
		this.arms = new LinkedList<Weapon>();
	}
	
	public void equip(Weapon weapon) {
		for(Weapon loaded : arms) {
			if(loaded.getClass().isInstance(weapon)) {
				loaded.recharge(weapon.getTotal());
				return;
			}
		}
		arms.addFirst(weapon);
	}
	
	private void tri(ShapeRenderer sr, float size, ShapeType type) {
		Point2D.Double loc = getDrawLoc();
		sr.begin(type);
		sr.triangle((float) loc.x + size * (float) Math.cos(getRot()), (float) loc.y + size * (float) Math.sin(getRot()),
				(float) loc.x + size * (float) Math.cos(getRot() - 5 * Math.PI / 4),
				(float) loc.y + size * (float) Math.sin(getRot() - 5 * Math.PI / 4),
				
				(float) loc.x + size * (float) Math.cos(getRot() + 5 * Math.PI / 4),
				(float) loc.y + size * (float) Math.sin(getRot() + 5 * Math.PI / 4));
		sr.end();
	}
	
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		sr.setColor(colorSet[colorIndex][0]);
		tri(sr, 20, ShapeType.Filled);
		sr.setColor(colorSet[colorIndex][1]);
		tri(sr, 20, ShapeType.Line);
		sr.begin(ShapeType.Filled);
		sr.circle((float) getDrawX(), (float) getDrawY(), 2);
		sr.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
	
	public void tick() {
		if(System.currentTimeMillis() - lastDamage > 750) heal(1f);
	}
	
	public double getDrawX() {
		return GameComponent.tileSize * (getLoc().x);
	}
	
	public double getDrawY() {
		return GameComponent.tileSize * (getLoc().y);
	}
	
	public Point2D.Double getDrawLoc() {
		return new Point2D.Double(getDrawX(), getDrawY());
	}
	
	public String getSpriteName() {
		return this.name + ".sprite";
	}
	
	public Point2D.Double getLoc() {
		return this.location;
	}
	
	public void setX(double x) {
		this.location.x = x;
	}
	
	public void setY(double y) {
		this.location.y = y;
	}
	
	public double getRot() {
		return this.rotation;
	}
	
	public void setRot(double rot) {
		this.rotation = rot;
		while(this.rotation < 0) this.rotation += Math.PI * 2;
		while(this.rotation > Math.PI * 2) this.rotation -= Math.PI * 2;
	}
	
	public void damage(float damage) {
		if(damage < 0) throw new RuntimeException("Cannot deal negative damage.");
		lastDamage = System.currentTimeMillis();
		health -= damage;
		if(health < 0) health = 0;
	}
	
	public void heal(float health) {
		if(health < 0) throw new RuntimeException("Cannot give negative health.");
		this.health += health;
		if(this.health > maxHealth) this.health = maxHealth;
	}

	public float getHealth() {
		return health;
	}
	
	public float getMaxHealth() {
		return maxHealth;
	}
	
	public Weapon getHudWeapon() {
		if(!arms.isEmpty())
			return arms.getFirst();
		return null;
	}
	
	public boolean hasLOS(Entity other) {
		int x0 = (int) location.x,
				y0 = (int) location.y,
				x1 = (int) other.location.x,
				y1 = (int) other.location.y;
		double angle = Math.atan2(y1 - y0, x1 - x0);
		if(angle < 0) angle += Math.PI * 2;
		if(Math.abs(angle - rotation) > Math.PI / 6) return false;
		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if(steep) {
			int t = x0; x0 = y0; y0 = t;
			t = x1; x1 = y1; y1 = t;
		}
		if(x0 > x1) {
			int t = x0; x0 = x1; x1 = t;
			t = y0; y0 = y1; y1 = t;
		}
		int deltax = x1 - x0;
		int deltay = Math.abs(y1 - y0);
		int error = deltax / 2;
		int ystep;
		int y = y0;
		if(y0 < y1) ystep = 1;
		else ystep = -1;
		for(int x = x0; x <= x1; x++) {
			if(steep) { if(!Physics.getLevel().getTile(y, x).isPassable()) return false; }
			else if(!Physics.getLevel().getTile(x, y).isPassable()) return false;
			error -= deltay;
			if(error < 0) {
				y += ystep;
				error += deltax;
			}
		}
		return true;
	}
}
