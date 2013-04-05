package daedalus.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;

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
	}
	
	public void init() {
		healthBarWidth = 400;
		hudWidth = 400;
		hudHeight = 100;
		hudColor = new Color(.3f, .7f, 1f, .5f);
		displayColor = new Color(.16f, 1f, 1f);
		dAmmoDraw = 50;
	}
	
	float r = 1;
	public void render(Graphics2D gr) {
		Weapon hudWeapon = target.getHudWeapon();
		float health = target.getHealth();
		Stroke oldStroke = gr.getStroke();
		Color oldColor = gr.getColor();
		
		gr.setColor(hudColor);
		gr.fillRect(GameComponent.getGame().width - hudWidth, 0, hudWidth, hudHeight);
		
		// Draw weapon hud info
		
		SpriteEngine.getSprite(hudWeapon.getHudSpriteName())
			.render(new Point(GameComponent.getGame().width - hudWidth + 10, 10), 0, gr);
		gr.setStroke(new BasicStroke(10));
		gr.setFont(new Font("Arial", Font.BOLD, 24));
		
		gr.setColor(displayColor);
		
		// Draw weapon ammo
		int drawX = GameComponent.getGame().width - dAmmoDraw - 10;
		if(hudWeapon.getReserve() == 0) gr.setColor(Color.red);
		gr.drawString("" + hudWeapon.getReserve(), drawX, 45);
		gr.setColor(displayColor);
		gr.drawString("/", drawX -= 20, 45);
		if(hudWeapon.getLoad() == 0) gr.setColor(Color.red);
		gr.drawString("" + hudWeapon.getLoad(), drawX - dAmmoDraw, 45);
		
		// Draw health bar //
		
		// If health is too low, background pulses red 
		if(health <= this.target.getMaxHealth() * .1)
			gr.setColor(
				new Color(
						.9f - (float) (Math.sin(System.currentTimeMillis() / 100.) * 0.1),
						.9f,
						.9f,
						.9f - (float) (Math.sin(System.currentTimeMillis() / 100.) * 0.1)));
		// Otherwise, background is a somewhat transparent white
		else gr.setColor(new Color(1, 1, 1, 0.9f));
		// Fill Background
		gr.fillRect((GameComponent.getGame().width - healthBarWidth) / 2 - 2,
				5, healthBarWidth + 4, 34);
		
		// Health bar scales from blue to red
		float r = 1.2f - health / 100f; if(r > 1) r = 1; if(r < 0) r = 0;
		float g = 0.25f;
		float b = 0f + health / 100f; if(b > 1) b = 1; if(b < 0) b = 0;
		gr.setColor(new Color(r, g, b, .5f));
		
		// Draw health bar
		gr.fillRect((int) (GameComponent.getGame().width - health * healthBarWidth / 100) / 2,
				7, (int) (health * healthBarWidth / 100), 30);
		
		// Reset color and stroke
		gr.setColor(oldColor);
		gr.setStroke(oldStroke);
	}
	
	public void tick() {
		// Deal random damage
		if(Math.random() <= .02) this.target.damage(20f);
	}
	
	public boolean isTransparent() {
		return true;
	}
}
