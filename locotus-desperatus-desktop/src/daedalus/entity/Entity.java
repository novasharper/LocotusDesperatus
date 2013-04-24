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
//		if(arms.getFirst() != null)
//			arms.getFirst().render(sb, sr);
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
}
