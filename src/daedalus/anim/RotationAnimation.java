package daedalus.anim;

import daedalus.entity.Entity;

public class RotationAnimation extends Animation {
	private double start;
	private double end;
	
	public RotationAnimation(Entity target, int length, double end) {
		super(target, length);
		this.start = target.getRot();
		this.end = end;
		if(this.end - this.start > Math.PI) this.end -= Math.PI * 2;
		else if(this.start - this.end > Math.PI) this.start -= Math.PI * 2;
	}
	
	public boolean isFinished() {
		return super.isFinished() || Math.abs(target.getRot() - end) <= Math.PI / 180;
	}
	
	public void tick() {
		if(!isFinished()) {
			this.target.setRot((end - start) * timeAlong() / length + start);
		} else {
			stop();
		}
	}
	
	public void finish() {
		this.target.setRot(end);		
	}
	
	public void reset() {
		target.setRot(start);
	}

	@Override
	public Animation invert() {
		RotationAnimation newAnim = new RotationAnimation(target, length, start);
		newAnim.start = end;
		return newAnim;
	}
}
