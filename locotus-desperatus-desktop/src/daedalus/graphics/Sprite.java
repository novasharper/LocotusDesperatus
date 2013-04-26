package daedalus.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import daedalus.Root;
import daedalus.main.GameComponent;


public class Sprite {
	private File file;
	private InputStream is;
	private BufferedImage img;
	private Texture tex;
	private Pixmap pixmap;
	private Point2D center;

	public Sprite(String path) {
		this(Root.class.getResourceAsStream(path));
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
	public void render(SpriteBatch sb, Point2D loc, Point2D centerLoc, double rot) {
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
		if(centerLoc == null) centerLoc = center;
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
