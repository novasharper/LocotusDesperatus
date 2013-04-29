package daedalus.entity;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;

import daedalus.anim.Animation;
import daedalus.anim.RotationAnimation;
import daedalus.anim.TranslationAnimation;
import daedalus.ld.LDMain;
import daedalus.main.GameComponent;
import daedalus.main.Path;


public class AIEntity extends Entity {
	protected LinkedList<Animation> animQueue;
	protected Path path;
	
	public AIEntity(String name) {
		super(name, 100, 1, true);
		animQueue = new LinkedList<Animation>();
	}
	
	public void aiTick() {
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

	public void moveTo(Point2D.Double dest) {
		animQueue.addLast(new RotationAnimation(this, 250,
				Math.atan2(dest.y - location.y, dest.x - location.x)));
		animQueue.addLast(new TranslationAnimation(this, speed * GameComponent.tileSize, dest));
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
	
	public double getDrawX() {
		return (getLoc().x - LDMain.ldm.chief.getLoc().x) * GameComponent.tileSize + Gdx.graphics.getWidth() / 2;
	}
	
	public double getDrawY() {
		return (getLoc().y - LDMain.ldm.chief.getLoc().y) * GameComponent.tileSize + Gdx.graphics.getHeight() / 2;
	}
}
