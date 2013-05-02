package daedalus.main;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;

public class Path {
	private Point[] points;
	private int nextIndex = 0, direction;
	private boolean continuous, reverses;
	
	public Path(Point[] points, boolean continuous, boolean reverses) {
		this.points = points;
		this.continuous = continuous;
		this.reverses = reverses;
		direction = 1;
		
		if(points.length == 0) return;
		int dxc = -1, dyc = -1;
		Point last = points[0];
		for(int i = 1; i < points.length; i++) {
			Point current = points[i];
			if(current == null) continue;
			int la = (int) Math.toDegrees(Math.atan2(dyc, dxc));
			int ca = (int) Math.toDegrees(Math.atan2(last.y - current.y, last.x - current.x));
			if(la == ca && i != 1) {
				points[i - 1] = null;
			}
			dxc = last.x - current.x;
			dyc = last.y - current.y;
			last = current;
		}
	}
	
	public boolean isFinished() {
		return !continuous && nextIndex == points.length || points.length == 0;
	}
	
	public Point2D.Double nextDestination() {
		if(isFinished())
			return null;
		int last = nextIndex;
		Point next;
		do {
			next = points[nextIndex];
			nextIndex += direction;
		} while(next == null);
		if(continuous && (nextIndex >= points.length || nextIndex < 0)) {
			if(reverses) {
				direction = -direction;
				nextIndex += direction;
			} else {
				nextIndex %= points.length;
			}
		}
		return new Point2D.Double(next.x + 0.5, next.y + 0.5);
	}
}
