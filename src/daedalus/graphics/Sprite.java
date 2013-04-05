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

import daedalus.Root;
import daedalus.main.GameComponent;


public class Sprite {
	private BufferedImage img;
	private Point2D center;

	public Sprite(String path) {
		this(new File(Root.class.getResource(path).getFile()));
	}
	
	public Sprite(File imgFile) {
		try {
			img = ImageIO.read(imgFile);
			center = new Point2D.Float(img.getWidth() / 2f,
					img.getHeight() / 2f);
		} catch (IOException e) {
			img = null;
		}
	}
	
	public Sprite(InputStream imgIS) {
		try {
			img = ImageIO.read(imgIS);
			center = new Point2D.Float(img.getWidth() / 2f,
					img.getHeight() / 2f);
		} catch (IOException e) {
			img = null;
		}
	}
	
	public void render(Point2D loc, double rot, Graphics2D gr) {
		Color oldColor = gr.getColor();
		Paint oldPaint = gr.getPaint();
		
		gr.setColor(Color.white);
		gr.setPaint(Color.white);
		
		AffineTransform drawAT = AffineTransform.getRotateInstance(
			rot,
			loc.getX() + center.getX(),
			loc.getY() + center.getY()
		);
		drawAT.translate(loc.getX(), loc.getY());
		
		gr.drawImage(img, drawAT, GameComponent.getDrawCanvas());
		
		gr.setPaint(oldPaint);
		gr.setColor(oldColor);
	}
	
	public void render(Point2D loc, Point2D centerLoc, double rot, Graphics2D gr) {
		Color oldColor = gr.getColor();
		Paint oldPaint = gr.getPaint();
		
		gr.setColor(Color.white);
		gr.setPaint(Color.white);
		
		AffineTransform drawAT = AffineTransform.getRotateInstance(
			rot,
			centerLoc.getX(),
			centerLoc.getY()
		);
		drawAT.translate(loc.getX(), loc.getY());
		
		gr.drawImage(img, drawAT, GameComponent.getDrawCanvas());
		
		gr.setPaint(oldPaint);
		gr.setColor(oldColor);
	}
	
	public int getWidth() {
		return img.getWidth();
	}
	
	public int getHeight() {
		return img.getHeight();
	}
	
	public Point2D getCenter() {
		return center;
	}
	
	public void setCenter(Point2D center) {
		this.center = center;
	}
}
