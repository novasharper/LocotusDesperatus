package daedalus.entity;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.Physics;
import daedalus.Root;
import daedalus.combat.Weapon;
import daedalus.graphics.GraphicsElement;
import daedalus.ld.LDMain;
import daedalus.level.Level;
import daedalus.main.GameComponent;


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
		double drawx = getDrawX();
		double drawy = getDrawY();
		String label = "" + name; //getLoc();
		if(drawx < -40 || drawx > Gdx.graphics.getWidth() + 40) return;
		if(drawy < -40 || drawy > Gdx.graphics.getHeight() + 40) return;
		BitmapFont font = Root.getFont(12);
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
		sb.begin();
		sb.enableBlending();
		font.setColor(Color.WHITE);
		font.draw(sb, label, (float) getDrawX() - font.getBounds(label).width / 2,
				(float) getDrawY() + 20 + font.getBounds(label).height + 10);
		sb.end();
		if(!arms.isEmpty() && arms.getFirst() != null)
			arms.getFirst().render(sb, sr);
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
		int res = 25;
		int x0 = (int) (location.x * res);
		int x1 = (int) (other.location.x * res);
		int y0 = (int) (location.y * res);
		int y1 = (int) (other.location.y * res);
	    double rot = Math.atan2(y1 - y0, x1 - x0);
	    if(Math.abs((rotation + Math.PI - rot) % (Math.PI * 2) - Math.PI) > Math.PI / 4) return false;
		int dx =  Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
	    int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
	    int err = dx + dy, e2;
	    
		Level lvl = Physics.getLevel();

	    for (;;) {
	    	if(!lvl.getTile(x0 / res, y0 / res).isPassable()) return false;

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
	    return true;
	}
}
