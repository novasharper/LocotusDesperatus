package daedalus.level;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;

public class Pathfinding {	
	public static class Heuristics {
		private static enum H_FUNCS {
			MANHATTAN_H {
				public double h_val(Tile start, Tile end) {
					return Math.abs(start.getLocation().x - end.getLocation().x) + Math.abs(start.getLocation().y - end.getLocation().y);
				}
			},
			CROW_H {
				public double h_val(Tile start, Tile end) {
					return start.getLocation().distance(end.getLocation());
				}
			};
			
			public abstract double h_val(Tile start, Tile end);
		}
		
		private static int h_num = H_FUNCS.CROW_H.ordinal();
		public static double heuristic(Tile start, Tile end) {
			return H_FUNCS.values()[h_num].h_val(start, end);
		}
	}
	
	public final static double SQRT_2 = 1.41421356237;
	
	public final static Object[][] moves = {
		{0, 1, 1.0},
		{1, 1, SQRT_2 },
		{1, 0, 1.0},
		{1, -1, SQRT_2 },
		{0, -1, 1.0},
		{-1, -1, SQRT_2 },
		{-1, 0, 1.0},
		{-1, 1, SQRT_2 }
	};
	
	private static Object[][] getTiles(Level map, Tile current) {
		ArrayList<Object[]> toRet = new ArrayList<>(8);
		int x, y;
		for (Object[] move : moves) {
			x = current.getLocation().x + (int) move[0];
			y = current.getLocation().y + (int) move[1];
			try {
				Tile node = map.getTile(x, y);
				Tile pa = map.getTile(current.getLocation().x, y);
				Tile pb = map.getTile(x, current.getLocation().y);
				if(node.isPassable() && pa.isPassable() && pb.isPassable() && current.isPassable())
					toRet.add(new Object[] { node, (double) move[2] * (node.getCost() + pa.getCost() + pb.getCost()) / 3 });
			} catch(Exception e) {
			}
		}
		return toRet.toArray(new Object[0][0]);
	}
	
	private static Point[] retracePath(Tile c) {
		ArrayList<Point> path = new ArrayList<>();
		while (c != null) {
			path.add(0, c.getLocation());
			c = c.getParent();
		}
		return path.toArray(new Point[0]);
	}
	
	public static Point[] ASTAR(Level map, Tile start, Tile end) {
		Set<Tile> closedSet = new HashSet<>();
		Set<Tile> openSet = new HashSet<>();
		PriorityQueue<Tile> openHeap = new PriorityQueue<>();
		Tile current;
		
		openHeap.add(start);
		openSet.add(start);
		while (!openSet.isEmpty()) {
			current = openHeap.poll();
			if (current.equals(end))
				return retracePath(current);
			openSet.remove(current);
			closedSet.add(current);
			for (Object[] neighborPair : getTiles(map, current)) {
				Tile neighbor = (Tile) neighborPair[0];
				neighbor.setPath(end, start);
				double g_delta = (double) neighborPair[1];
				if (closedSet.contains(neighbor.getLocation()))
					continue;
				double tentative_g_score = current.getG_score() + g_delta;
				if (!(openSet.contains(neighbor) || closedSet.contains(neighbor)) || tentative_g_score < neighbor.getG_score()) {
					neighbor.setParent(current);
					if (!(openSet.contains(neighbor.getLocation()))) {
						openHeap.add(neighbor);
						openSet.add(neighbor);
					}
				}
			}
		}
		return new Point[0];
	}
	
	public static Point[] ASTAR(Level map, Point2D start, Point2D end) {
		return ASTAR(map, map.getTile((int) start.getX(), (int) start.getY()),
				map.getTile((int) end.getX(), (int) end.getY()));
	}
	
	private Pathfinding() {}
}