package daedalus.anim;

import com.badlogic.gdx.Gdx;

import daedalus.entity.Entity;
import daedalus.main.GameComponent;

public abstract class Animation {
	protected Entity target;
	protected int length;
	protected boolean running;
	protected long startTime;
	protected long along;
	
	public Animation(Entity target, int length) {
		this.target = target;
		this.length = length;
		this.startTime = -1;
		this.along = 0;
	}
	
	public void tick() {
		along += (long) (1000 / GameComponent.framerate);
	}
	public abstract void reset();
	public abstract void finish();
	public abstract Animation invert();
	
	public boolean isFinished() {
		return along >= length; //!(System.currentTimeMillis() - startTime <= length || startTime < 0);
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void start() {
		running = true;
//		startTime = System.currentTimeMillis();
		reset();
	}
	
	public long timeAlong() {
		return along; //System.currentTimeMillis() - startTime;
	}
	
	public void stop() {
		running = false;
	}
}
