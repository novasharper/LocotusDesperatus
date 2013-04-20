package daedalus.level;

import java.awt.Point;

import daedalus.graphics.Sprite;
import daedalus.level.Level.TileInfo;

public class Tile implements Comparable<Tile> {
	private Point location;
	private boolean passable;
	private double g_score, h_score;
	private Tile parent, start, end;
	private double cost;
	private TileInfo info;
	private int id;
	private int data;
	
	public Tile(int x, int y, TileInfo info, int cost, int data, int extended_data) {
		this.location = new Point(x, y);
		this.info = info;
		this.cost = cost;
		this.data = data;
		if(info != null)
			this.passable = info.getPassable();
		else
			this.passable = true;
		this.reset();
	}
	
	public Point getLocation() {
		return location;
	}
	
	public boolean isPassable() {
		return passable;
	}
	
	public void setPassable(boolean passable) {
		this.passable = passable;
	}

	public double getG_score() {
		return g_score;
	}

	public double getH_score() {
		return h_score;
	}

	public Tile getParent() {
		return parent;
	}

	public Tile getStart() {
		return start;
	}

	public Tile getEnd() {
		return end;
	}

	public double getCost() {
		return cost;
	}

	public int getID() {
		return id;
	}

	public int getData() {
		return data;
	}

	public void setParent(Tile parent) {
		this.parent = parent;
		this.g_score = parent.g_score + Pathfinding.Heuristics.heuristic(parent, this);
	}
	
	public void setPath(Tile end, Tile start) {
		if(this.start != start || this.end != end) {
			reset();
		}
		this.h_score = Pathfinding.Heuristics.heuristic(this, end);
		this.end = end;
		this.start = start;
	}
	
	public double getScore() {
		return this.g_score + this.h_score;
	}
	
	public boolean equals(Tile other) {
		return other.location.equals(location);
	}
	
	public int compareTo(Tile o) {
		return (int) ((getScore() - o.getScore()) * 100);
	}
	
	public void reset() {
		this.g_score = 0;
		this.h_score = 0;
		this.parent = null;
		this.start = null;
		this.end = null;
	}
	
	public String toString() {
		return "" + location;
	}
	
	public Sprite getSprite() {
		return info.getSprite();
	}
}
