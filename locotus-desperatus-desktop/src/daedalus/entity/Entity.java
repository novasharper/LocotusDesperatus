package daedalus.entity;

import java.awt.Point;
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
import daedalus.input.F310;
import daedalus.input.Gamepad;
import daedalus.ld.LDMain;
import daedalus.level.Level;
import daedalus.level.Tile;
import daedalus.main.GameComponent;
import daedalus.util.Util;


public abstract class Entity implements GraphicsElement {
	protected Point2D.Double location;
	protected double rotation;
	protected String name;
	protected float health;
	protected float maxHealth;
	protected long lastDamage;
	protected boolean isAI;
	protected double speed;
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
	
	public Entity(String name, float maxHealth, double speed, boolean isAI) {
		this.name = name;
		this.location = new Point2D.Double();
		this.rotation = 0;
		this.maxHealth = maxHealth;
		this.health = maxHealth;
		this.arms = new LinkedList<Weapon>();
		this.speed = speed;
		this.isAI = isAI;
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
		double rot = getRot();
		sr.begin(type);
		sr.triangle(
				(float) loc.x + size * (float) Math.cos(rot),
				(float) loc.y + size * (float) Math.sin(rot),
				
				(float) loc.x + size * (float) Math.cos(rot - 5 * Math.PI / 4),
				(float) loc.y + size * (float) Math.sin(rot - 5 * Math.PI / 4),
				
				(float) loc.x + size * (float) Math.cos(rot + 5 * Math.PI / 4),
				(float) loc.y + size * (float) Math.sin(rot + 5 * Math.PI / 4));
		sr.end();
	}
	
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		double drawx = getDrawX();
		double drawy = getDrawY();
		if(drawx < -40 || drawx > Gdx.graphics.getWidth() + 40) return;
		if(drawy < -40 || drawy > Gdx.graphics.getHeight() + 40) return;
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
		if(!arms.isEmpty() && arms.getFirst() != null)
			arms.getFirst().render(sb, sr);
		if(isAI) {
			float barWidth = 60;
			BitmapFont font = Root.getFont(12);
			String label = getLabel();
			sb.begin();
			sb.enableBlending();
			font.setColor(Color.WHITE);
			font.draw(sb, label, (float) getDrawX() - font.getBounds(label).width / 2,
					(float) getDrawY() + 40 + font.getBounds(label).height);
			sb.end();
			sr.begin(ShapeType.Filled);
			float red = Math.min(1f, 2f * (1f - health / maxHealth));
			float green = Math.min(1f, 2f * (health / maxHealth));
			sr.setColor(red, green, 0, 1f);
			sr.rect((float) getDrawX() - barWidth / 2, (float) getDrawY() + 30, (float) (barWidth * health / maxHealth), 5);
			sr.end();
		}
	}
	
	private void humanTick() {
		if(System.currentTimeMillis() - lastDamage > 750) heal(1f);
		double dxl = GameComponent.getGamePad().pollAxis(F310.AXIS_LEFT_X);
		double dyl = GameComponent.getGamePad().pollAxis(F310.AXIS_LEFT_Y);
		double dxr = GameComponent.getGamePad().pollAxis(F310.AXIS_RIGHT_X);
		double dyr = GameComponent.getGamePad().pollAxis(F310.AXIS_RIGHT_Y);
		// Allow strafing
		if(dxr * dxr + dyr * dyr >= Gamepad.rot_deadzone)
			rotation = 2 * Math.PI - Math.atan2(dyr, dxr);
		else if(dxl * dxl + dyl * dyl >= Gamepad.rot_deadzone)
			rotation = 2 * Math.PI - Math.atan2(dyl, dxl);
		if(Math.abs(dxl) < 0.1) dxl = 0;
		if(Math.abs(dyl) < 0.1) dyl = 0;
		double deltay = dyl * speed / GameComponent.framerate;
		double deltax = dxl * speed / GameComponent.framerate;
		Tile t1 = Physics.getLevel().getTile((int) location.x, (int) Math.floor(location.y - deltay));
		Tile t2 = Physics.getLevel().getTile((int) Math.floor(location.x + deltax), (int) location.y);
		if(t1 == null || !t1.isPassable())
			deltay = 0;
		if(t2 == null || !t2.isPassable())
			deltax = 0;
		location.y -= deltay;
		location.x += deltax;
		if(!arms.isEmpty()) {
			arms.get(0).tick();
		}
	}
	
	protected String getLabel() { return name; }
	
	protected void aiTick() {
	}
	
	public void tick() {
		if(isAI) {
			aiTick();
		} else {
			humanTick();
		}
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
		return hasLOS(other, Math.PI / 4, getRot());
	}
	
	public boolean hasLOS(Entity other, double halfFovX) {
		return hasLOS(other, halfFovX, getRot());
	}
	
	public boolean canReach(Point2D.Double other) {
		int res = 25;
		int x0 = (int) (location.x * res);
		int x1 = (int) (other.x * res);
		int y0 = (int) (location.y * res);
		int y1 = (int) (other.y * res);
	    double rot = Math.atan2(y1 - y0, x1 - x0);
	    double angle = getRot();
		int dx =  Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
	    int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
	    if(Math.abs(dy) >= Gdx.graphics.getHeight() / 2) return false;
	    if(dx >= Gdx.graphics.getWidth() / 2) return false;
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
	
	public boolean hasLOS(Entity other, double halfFovX, double overrideAngle) {
		int res = 25;
		int x0 = (int) (location.x * res);
		int x1 = (int) (other.location.x * res);
		int y0 = (int) (location.y * res);
		int y1 = (int) (other.location.y * res);
	    double rot = Math.atan2(y1 - y0, x1 - x0);
	    double angle = getRot();
	    if(overrideAngle > 0) angle = overrideAngle;
	    if(Util.angleDifference(angle, rot) > halfFovX) return false;
		int dx =  Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
	    int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
	    if(Math.abs(dy * 1.0 / res) >= Gdx.graphics.getHeight() / (2 * GameComponent.tileSize)) return false;
	    if(dx * 1.0 / res >= Gdx.graphics.getWidth() / (2 * GameComponent.tileSize)) return false;
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
	
	public boolean isDead() {
		return health <= 0;
	}
	
	public boolean isAI() {
		return isAI;
	}
}
