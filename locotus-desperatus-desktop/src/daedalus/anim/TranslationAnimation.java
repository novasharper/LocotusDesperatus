package daedalus.anim;

import java.awt.geom.Point2D;

import daedalus.entity.Entity;
import daedalus.main.GameComponent;


public class TranslationAnimation extends Animation {
	private Point2D.Double end;
	private Point2D.Double start;
	
	public TranslationAnimation(Entity target, int length, Point2D.Double end) {
		super(target, length);
		this.end = end;
		this.start = (Point2D.Double) target.getLoc().clone();
	}
	
	public TranslationAnimation(Entity target, double speed, Point2D.Double end) {
		super(target, 0);
		this.end = end;
		this.start = (Point2D.Double) target.getLoc().clone();
		double dist = end.distance(start);
		this.length = (int) (dist / (speed / GameComponent.tileSize) * 1000);
	}
	
	public boolean isFinished() {
		return super.isFinished() || this.target.getLoc().distance(end) <= (32 / GameComponent.tileSize);
	}
	
	public void tick() {
		super.tick();
		if(!isFinished()) {
			this.target.setX((end.x - start.x) * timeAlong() / length + start.x);
			this.target.setY((end.y - start.y) * timeAlong() / length + start.y);
		} else {
			stop();
		}
	}
	
	public void finish() {
		this.target.setX(end.x);
		this.target.setY(end.y);
	}
	
	public void reset() {
		this.target.setX(start.x);
		this.target.setY(start.y);
	}
	
	public Animation invert() {
		TranslationAnimation newAnim = new TranslationAnimation(target, length, (Point2D.Double) start.clone());
		newAnim.start = (Point2D.Double) end.clone();
		return newAnim;
	}
	
	public Point2D.Double getDest() {
		return (Point2D.Double) end.clone();
	}
}
