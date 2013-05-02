package daedalus.combat;

import daedalus.entity.Entity;
import daedalus.input.F310;
import daedalus.input.GamepadEvent;
import daedalus.input.GamepadEvent.ComponentType;
import daedalus.input.GamepadEvent.EventType;
import daedalus.main.GameComponent;


public class AssaultRifle extends Weapon {

	public AssaultRifle(Entity wielder) {
		super(wielder);
	}
	
	public String getName() {
		return "ma5c";
	}
	
	public float getDamage() {
		return 5f;
	}
	
	public int getPowerPerUse() {
		return 1;
	}
	
	public int getMaxLoad() {
		return 32;
	}
	
	private boolean shouldFire;
	private boolean shouldDrawFire;
	public boolean shouldFire() {
		return shouldFire;
	}
	
	public boolean shouldDrawFire() {
		return shouldDrawFire;
	}
	
	public int getMaxReserve() {
		return 320;
	}
	
	public double roundsPerSecond() {
		return 650. / 60;
	}
	
	public void trigger() {
		shouldDrawFire = shouldFire = true;
	}
	
	public void releaseTrigger() {
		shouldDrawFire = shouldFire = false;
	}
	
	public void handleInput(GamepadEvent ev) {
		super.handleInput(ev);
		if(ev.getButtonID() == F310.BUTTON_R && ev.getCType() == ComponentType.BUTTON) {
			shouldFire = ev.getType() == EventType.PRESSED;
			shouldDrawFire = shouldFire;
		}
	}
}
