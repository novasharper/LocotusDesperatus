package daedalus.graphics;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import daedalus.Root;
import daedalus.util.Util;


public class Sprite {
	private File file;
	private InputStream is;
	private Texture tex;
	private Point2D center;

	public Sprite(String path) {
		this(new File(Util.getWorkingDir(), "resources/image/" + path));
	}
	
	public Sprite(File imgFile) {
		file = imgFile;
	}
	
	public Sprite(InputStream imgIS) {
		is = imgIS;
	}
	public void render(SpriteBatch sb, Point2D loc, double rot) {
		render(sb, loc, center, rot);
	}
	private void tryLoad() {
		if(file != null) {
			tex = new Texture(new FileHandle(file));
			tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			center = new Point2D.Float(tex.getWidth() / 2f,
					tex.getHeight() / 2f);
			file = null;
		}
		if(is != null) {
			tex = Root.loadTexture(is);
			tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			if(tex != null)
				center = new Point2D.Float(tex.getWidth() / 2f,
						tex.getHeight() / 2f);
			is = null;
		}
	}
	public void render(SpriteBatch sb, Point2D loc, Point2D centerLoc, double rot) {
		if(file != null || is != null) tryLoad();
		if(centerLoc == null) centerLoc = center;
		if(tex == null) return;
		sb.begin();
		sb.enableBlending();
		sb.draw(tex, (float) loc.getX(), (float) loc.getY(), (float) centerLoc.getX(), (float) centerLoc.getY(),
				getWidth(), getHeight(), 1, 1, (float) rot, 0, 0,
				 getWidth(), getHeight(), false, false);
		sb.end();
	}
	
	public int getWidth() {
		if(tex != null)
			return tex.getWidth();
		return 0;
	}
	
	public int getHeight() {
		if(tex != null)
			return tex.getHeight();
		return 0;
	}
	
	public Point2D getCenter() {
		return center;
	}
	
	public void setCenter(Point2D center) {
		this.center = center;
	}
}
