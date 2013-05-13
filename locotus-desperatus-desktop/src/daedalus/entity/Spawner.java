package daedalus.entity;

import java.awt.Point;
import java.awt.geom.Point2D;

import com.badlogic.gdx.Gdx;

import daedalus.game.LocotusDesperatus;
import daedalus.game.Main;
import daedalus.main.GameComponent;

public class Spawner {
	private Class<? extends Entity> toSpawn;
	private Point location;
	private double radius;
	public Spawner(Class<? extends Entity> toSpawn, Point location, double radius) {
		this.toSpawn = toSpawn;
		this.location = location;
		this.radius = radius;
	}
	
	protected float timeSpan = 3;
	protected float minTime = 4;
	private int ticksLeft;
	public void tick() {
		if(ticksLeft <= 0) {
			ticksLeft = (int) (GameComponent.framerate * (minTime + Math.random() * timeSpan));
			// Spawn
			try {
				Entity en = toSpawn.newInstance();
				en.setX(location.x + 0.5 + (Math.random() - 0.5) * radius * 2);
				en.setY(location.y + 0.5 + (Math.random() - 0.5) * radius * 2);
				Main.game.getMap().addEntity(en);
			} catch(Exception ex) {
			}
		} else {
			ticksLeft--;
		}
	}
}
