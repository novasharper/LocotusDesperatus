package daedalus.entity;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;

import daedalus.Physics;
import daedalus.anim.Animation;
import daedalus.anim.RotationAnimation;
import daedalus.anim.TranslationAnimation;
import daedalus.ld.LDMain;
import daedalus.level.Pathfinding;
import daedalus.level.Tile;
import daedalus.main.GameComponent;
import daedalus.main.Path;
import daedalus.util.Util;


public class AIEntity extends Entity {
	protected LinkedList<Animation> animQueue;
	protected Path path;
	protected Point[] pathPoints;
	protected Point[] pathPointsBak;
	protected float notOnPatrol;
	protected double turnRateCap;
	protected double chaseSpeedCap;
	
	public AIEntity(String name) {
		super(name, 100, 1, true);
		animQueue = new LinkedList<Animation>();
		turnRateCap = 0.03;
		chaseSpeedCap = 0.01;
	}
	
	private Point2D.Double chiefLoc;
	private double targetRot;
	private boolean hasLOS;
	private double reloadTimer = -1;
	public void aiTick() {
		hasLOS = false;
		if(hasLOS(LDMain.ldm.chief)) {
			notOnPatrol += 2f;
			chiefLoc = (Point2D.Double) LDMain.ldm.chief.location.clone();
			targetRot = Math.atan2(chiefLoc.y - location.y, chiefLoc.x - location.x);
			while(targetRot < 0) targetRot += Math.PI * 2;
			hasLOS = true;
		}
		else if(notOnPatrol > 0) {
			notOnPatrol -= 1f;
			if(notOnPatrol < 0) notOnPatrol = 0;
			if(notOnPatrol == 0) {
				// Return to patrol
				chiefLoc = null;
				if(pathPoints.length > 0) {
					if(!canReach(new Point2D.Double(pathPoints[0].x, pathPoints[0].y))) {
						Tile t2 = Physics.getLevel().getTile(pathPoints[0].x, pathPoints[0].y);
						Tile t1 = Physics.getLevel().getTile((int) (Math.floor(getLoc().x)), (int) (Math.floor(getLoc().y)));
						pathPointsBak = Pathfinding.ASTAR(Physics.getLevel(), t1, t2);
					} else {
						moveTo(new Point2D.Double(pathPoints[0].x, pathPoints[0].y));
					}
				}
				path = null;
			}
		}
		if(notOnPatrol > 0) {
			if(targetRot != rotation) {
				double diff = targetRot - rotation;
				if(diff < 0) diff += Math.PI * 2;
				if(Util.angleDifference(targetRot, rotation) < turnRateCap) rotation = targetRot;
				else if(diff > Math.PI) setRot(rotation - turnRateCap);
				else setRot(rotation + turnRateCap);
			}
			if(!arms.isEmpty()) {
				if(arms.getFirst().getLoad() > 0) {
					if(Util.angleDifference(targetRot, rotation) <= 0 && hasLOS) {
						if(Math.random() < 0.1) arms.getFirst().trigger();
					} else
						arms.getFirst().releaseTrigger();
				} else {
					arms.getFirst().releaseTrigger();
					if(reloadTimer == 0) {
						arms.getFirst().reload();
						reloadTimer = -1;
					} else if(reloadTimer == -1) reloadTimer = 1;
					else if(reloadTimer < 0) reloadTimer = 0;
					else reloadTimer -= 1 / GameComponent.framerate;
				}
				arms.getFirst().tick();
			}
			boolean moved = false;
			if(chiefLoc.distance(location) > (hasLOS ? 1 : 0.01)) {
				if(chiefLoc.distance(location) > chiefLoc.distance(location.x + chaseSpeedCap * Math.cos(rotation), location.y + chaseSpeedCap * Math.sin(rotation))) {
					double dx = chaseSpeedCap * Math.cos(rotation);
					double dy = chaseSpeedCap * Math.sin(rotation);
					Tile t1 = Physics.getLevel().getTile((int) location.x, (int) Math.floor(location.y + dy));
					Tile t2 = Physics.getLevel().getTile((int) Math.floor(location.x + dx), (int) location.y);
					if(t1 == null || !t1.isPassable())
						dy = 0;
					if(t2 == null || !t2.isPassable())
						dx = 0;
					location.x += dx;
					location.y += dy;
					moved = true;
				}
			}
			if(!hasLOS && !moved) {
				rotation += turnRateCap;
				targetRot = rotation;
			}
		} else {
			if(path == null) {
				if(pathPointsBak != null) {
					if(pathPointsBak.length > 0) {
						path = new Path(pathPointsBak, false, false);
					}
					pathPointsBak = null;
				} else if(pathPoints != null) {
					path = new Path(pathPoints, true, true);
				}
				animQueue.clear();
			}
			if(!animQueue.isEmpty()) {
				if(!animQueue.getFirst().isFinished()) {
					if(!animQueue.getFirst().isRunning()) animQueue.getFirst().start();
					animQueue.getFirst().tick();
				} else {
					animQueue.getFirst().finish();
					animQueue.removeFirst();
				}
			} else if(path != null) {
				if(path.isFinished()) path = null;
				else moveTo(path.nextDestination());
			}
		}
	}
	
//	protected String getLabel() { return "" + targetRot; }

	public void moveTo(Point2D.Double dest) {
		animQueue.addLast(new RotationAnimation(this, 250,
				Math.atan2(dest.y - location.y, dest.x - location.x)));
		animQueue.addLast(new TranslationAnimation(this, speed * GameComponent.tileSize, dest));
	}
	
	public void setPath(Point[] newPath) {
		pathPoints = newPath;
	}
	
	public void clearPath() {
		if(path != null) path = null;
		ListIterator<Animation> it = animQueue.listIterator();
		while(it.hasNext()) {
			Animation an = it.next();
			if(an instanceof RotationAnimation || an instanceof TranslationAnimation)
				it.remove();
		}
	}
	
	public double getDrawX() {
		return (getLoc().x - LDMain.ldm.chief.getLoc().x) * GameComponent.tileSize + Gdx.graphics.getWidth() / 2;
	}
	
	public double getDrawY() {
		return (getLoc().y - LDMain.ldm.chief.getLoc().y) * GameComponent.tileSize + Gdx.graphics.getHeight() / 2;
	}
}
