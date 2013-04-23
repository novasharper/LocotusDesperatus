package daedalus.graphics;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import daedalus.Root;
import daedalus.combat.Weapon;
import daedalus.entity.Entity;
import daedalus.main.GameComponent;
import daedalus.main.GameContext;
import daedalus.test.TestMain;


public class HUD extends GameContext {
	
	private int healthBarWidth;
	private int hudWidth, hudHeight;
	private Entity target;
	private Color hudColor;
	private Color displayColor;
	private int dAmmoDraw;
	
	public HUD(Entity target) {
		this.target = target;
		healthBarWidth = 400;
		hudWidth = 400;
		hudHeight = 100;
		hudColor = new Color(.3f, .7f, 1f, .75f);
		displayColor = new Color(.16f, 1f, 1f, 1f);
		dAmmoDraw = 50;
	}
	
	public void init() {
	}
	
	float r = 1;
	
	public void tick() {
		// Deal random damage
		if(Math.random() <= .02) this.target.damage(20f);
	}
	
	public boolean isTransparent() {
		return true;
	}

	@Override
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		if(hudColor == null || displayColor == null) return;
		Graphics2D gr = null;
		Weapon hudWeapon = target.getHudWeapon();
		float health = target.getHealth();
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		sr.begin(ShapeType.Filled);
		sr.setColor(hudColor);
		sr.rect(Gdx.graphics.getWidth() - hudWidth, Gdx.graphics.getHeight() - hudHeight, hudWidth, hudHeight);
		sr.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
		// Draw weapon hud info
		
		Sprite s =  SpriteEngine.getSprite(hudWeapon.getHudSpriteName());
		s.render(sb, new Point(Gdx.graphics.getWidth() - hudWidth + 10, Gdx.graphics.getHeight() - 10 - s.getHeight()), 0);
		sb.begin();
		BitmapFont font = Root.getFont(24);
		font.setColor(displayColor);
		font.draw(sb, "", 0, 0);
		
		// Draw weapon ammo
		int drawX = Gdx.graphics.getWidth() - dAmmoDraw - 10;
		if(hudWeapon.getReserve() == 0) font.setColor(Color.RED);
		font.draw(sb, "" + hudWeapon.getReserve(), drawX, Gdx.graphics.getHeight() - 45 - font.getCapHeight());
		font.setColor(displayColor);
		font.draw(sb, "/", drawX -= 20, Gdx.graphics.getHeight() - 45 - font.getCapHeight());
		if(hudWeapon.getLoad() == 0) font.setColor(Color.RED);
		font.draw(sb, "" + hudWeapon.getLoad(), drawX - dAmmoDraw, Gdx.graphics.getHeight() - 45 - font.getCapHeight());
		sb.end();
		
		// Draw health bar //
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		sr.begin(ShapeType.Filled);
		// If health is too low, background pulses red 
		if(health <= this.target.getMaxHealth() * .1)
			sr.setColor(
						.9f - (float) (Math.sin(System.currentTimeMillis() / 100.) * 0.1),
						.9f,
						.9f,
						.9f - (float) (Math.sin(System.currentTimeMillis() / 100.) * 0.1));
		// Otherwise, background is a somewhat transparent white
		else sr.setColor(1, 1, 1, 0.9f);
		// Fill Background
		sr.rect((Gdx.graphics.getWidth() - healthBarWidth) / 2 - 2,
				Gdx.graphics.getHeight() - 39, healthBarWidth + 4, 34);
		
		// Health bar scales from blue to red
		float r = 1.2f - health / 100f; if(r > 1) r = 1; if(r < 0) r = 0;
		float g = 0.25f;
		float b = 0f + health / 100f; if(b > 1) b = 1; if(b < 0) b = 0;
		sr.setColor(r, g, b, .5f);
		
		// Draw health bar
		sr.rect((Gdx.graphics.getWidth() - health * healthBarWidth / 100) / 2,
				Gdx.graphics.getHeight() - 37, (health * healthBarWidth / 100), 30);
		sr.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
}
