package daedalus.entity;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.ListIterator;

import daedalus.anim.Animation;
import daedalus.anim.RotationAnimation;
import daedalus.anim.TranslationAnimation;
import daedalus.graphics.SpriteEngine;
import daedalus.main.GameComponent;
import daedalus.main.Path;
import daedalus.test.TestMain;


public class NPC extends Entity {
	protected LinkedList<Animation> animQueue;
	protected double speed;
	protected Path path;
	
	public NPC(String name) {
		super(name, 100);
		speed = GameComponent.tileSize * 1; // Per second
		animQueue = new LinkedList<Animation>();
	}
	
	public void tick() {
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
		super.tick();
	}

	public void moveTo(Point2D.Double dest) {
		animQueue.addLast(new RotationAnimation(this, 250,
				Math.atan2(dest.y - location.y, dest.x - location.x)));
		animQueue.addLast(new TranslationAnimation(this, speed, dest));
	}
	
	public void setPath(Path newPath) {
		path = newPath;
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
}
