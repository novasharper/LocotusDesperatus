package daedalus.anim;

import daedalus.entity.Entity;

public abstract class Animation {
	protected Entity target;
	protected int length;
	protected boolean running;
	protected long startTime;
	
	public Animation(Entity target, int length) {
		this.target = target;
		this.length = length;
		this.startTime = -1;
	}
	
	public abstract void tick();
	public abstract void reset();
	public abstract void finish();
	public abstract Animation invert();
	
	public boolean isFinished() {
		return !(System.currentTimeMillis() - startTime <= length || startTime < 0);
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void start() {
		running = true;
		startTime = System.currentTimeMillis();
		reset();
	}
	
	public long timeAlong() {
		return System.currentTimeMillis() - startTime;
	}
	
	public void stop() {
		running = false;
	}
}
