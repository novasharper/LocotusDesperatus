package daedalus.main;

import java.awt.Point;
import java.awt.geom.Point2D;

public class Path {
	private Point[] points;
	private int nextIndex = 0, direction;
	private boolean continuous, reverses;
	
	public Path(Point[] points, boolean continuous, boolean reverses) {
		this.points = points;
		this.continuous = continuous;
		this.reverses = reverses;
		direction = 1;
		
		int dxc = -1, dyc = -1;
		for(int i = 1; i < points.length; i++) {
			Point last = points[i - 1];
			Point current = points[i];
			int la = (int) Math.toDegrees(Math.atan2(dyc, dxc));
			int ca = (int) Math.toDegrees(Math.atan2(last.y - current.y, last.x - current.x));
			if(la == ca && i != 1) {
				points[i - 1] = null;
			}
			dxc = last.x - current.x;
			dyc = last.y - current.y;
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
		return new Point2D.Double(next.x, next.y);
	}
}
