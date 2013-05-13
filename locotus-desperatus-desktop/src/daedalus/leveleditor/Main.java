package daedalus.leveleditor;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

import daedalus.level.Level;
import daedalus.level.Tile;
import daedalus.level.Level.TileInfo;


public class Main extends JFrame {
	private Point selected;
	private int width = 30;
	private int height = 20;
	private int ts = 32;
	
	private ToolBox other;
	private MainCanvas canvas;
	private Level level;
	private Tile[][] tiles;
	
	public Main() {
		level = new Level(width, height);
		tiles = new Tile[height][width];
		for(int i = 0; i < height; i++) {
			for(int i2 = 0; i2 < width; i2++) {
				tiles[i][i2] = null;
			}
		}
		setTitle("Level Editor");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		canvas = new MainCanvas();
		add(canvas);
		canvas.setSize(width * ts, height * ts);
		canvas.setMinimumSize(new Dimension(width * ts, height * ts));
		canvas.setMaximumSize(new Dimension(width * ts, height * ts));
		canvas.setPreferredSize(new Dimension(width * ts, height * ts));
		setResizable(false);
		setVisible(true);
		pack();
		
		other = new ToolBox();
		other.setVisible(true);
		
		addComponentListener(new ComponentAdapter() {			
			public void componentMoved(ComponentEvent arg0) {
				Point newLoc = new Point(getLocationOnScreen());
				newLoc.x += getWidth() + 10;
				newLoc.y += 25;
				other.setLocation(newLoc);
			}
		});
		
		addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent arg0) {
				other.setState(JFrame.ICONIFIED);				
			}
			public void windowDeiconified(WindowEvent arg0) {
				other.setState(JFrame.NORMAL);
			}
		});
		
		setLocation(20, 20);
	}
	
	private void handleClick(Point p, int mouseButton) {
		if(mouseButton == 3) {
			tiles[p.y][p.x] = null;
		} else {
			selected = p;
			if(other.mode == ToolBox.PAINT) {
				tiles[selected.y][selected.x] = other.getSelectedTile();
			}
		}
		canvas.repaint();
		other.repaint();
	}
	
	public Tile getTypeForSelected() {
		if(selected != null) {
			return tiles[selected.y][selected.x];
		}
		return null;
	}
	
	private class ToolBox extends JFrame {
		private int numElements;
		public BufferedImage sprites[];
		public int mode;
		public int selected;
		public static final int EDIT = 0;
		public static final int PAINT = 1;
		private JToggleButton editBTN;
		private JToggleButton paintBTN;
		private static final int width = 6;
		private Point tl = new Point(10, 10);
		
		public ToolBox() {
			setSize(250, 600);
			setUndecorated(true);
			
			numElements = 10;
			sprites = new BufferedImage[numElements];
			try {
				BufferedImage ss = ImageIO.read(Main.class.getResourceAsStream("/data/chars.png"));
				for(int i = 0; i < ss.getWidth() / 32; i++) {
					sprites[i] = ss.getSubimage(i * 32, 0, 32, 32);
				}
			} catch(Exception ex) {
			}
			addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent arg0) {
					Point p = arg0.getPoint();
					p.x = (p.x - tl.x) / (ts + 5);
					p.y = (p.y - tl.y) / (ts + 5);
					int selected;
					if(p.x > width - 1 || p.y > numElements / width || (p.x + p.y * width) > numElements) selected = -1;
					else selected = p.x + p.y * width;
					if(Main.this.selected != null && mode == EDIT) {
						tiles[Main.this.selected.y][Main.this.selected.x] = getSelectedTile();
					}
					other.selected = selected;
					repaint();
					Main.this.canvas.repaint();
				}
			});
			mode = PAINT;
			initSwing();
		}
		
		private void initSwing() {
			setLayout(null);
			editBTN = new JToggleButton("Edit");
			editBTN.setBounds(10, 100, 230, 35);
			add(editBTN);
			
			paintBTN = new JToggleButton("Paint");
			paintBTN.setBounds(10, 140, 230, 35);
			add(paintBTN);
			
			ButtonGroup grp = new ButtonGroup();
			grp.add(editBTN);
			grp.add(paintBTN);
			
			paintBTN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					mode = PAINT;
				}
			});
			
			editBTN.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					mode = EDIT;
				}
			});
			
			paintBTN.doClick();
		}
		
		public Tile getSelectedTile() {
			return null;
		}
		
		private void drawSprite(Object sprite, int number, Graphics gr, boolean selected) {
			int x = (number % width) * (ts + 5) + tl.x;
			int y = (number / width) * (ts + 5) + tl.y;
			
			if(selected) {
				gr.setColor(Color.black);
				gr.fillRect(x - 2, y - 2, ts + 4, ts + 4);
			}
			
			gr.setColor(Color.white);
			gr.drawImage(sprites[number], x, y, this);
		}
		
		public void paint(Graphics gr) {
			BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D gr_ = (Graphics2D) img.getGraphics();
			gr_.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			gr_.setColor(Color.red);
			gr_.fillRect(0, 0, getWidth(), getHeight());
			for(int i = 0; i < numElements; i++) drawSprite(null, i, gr_, false);//i == ((mode == EDIT) ? getTypeForSelected() : selected));
			gr.drawImage(img, 0, 0, this);
			paintBTN.repaint();
			editBTN.repaint();
		}
	}
	
	private class MainCanvas extends Canvas {		
		public MainCanvas() {
			addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent arg0) {
					Point p = arg0.getPoint();
					int button = arg0.getButton();
					p.x /= ts;
					p.y /= ts;
					handleClick(p, button);
				}
			});
		}
		
		public void paint(Graphics gr) {
			int offset = 0;
			BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D gr_ = (Graphics2D) img.getGraphics();
			gr_.setColor(Color.black);
			gr_.fillRect(0, 0, getWidth(), getHeight());
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					Tile tile = level.getTile(x, y);
					if(tile != null) {
						gr_.setColor(Color.white);
						TileInfo info = tile.getInfo();
						if(info != null) {
//							gr_.drawImage(info.getImage(), x * ts, y * ts, Main.this);
						}
					}
					if(x != 0) {
						gr_.setColor(Color.white);
						gr_.drawLine(x * ts + offset, 0, x * ts + offset, getHeight());
					}
				}
				if(y != 0) {
					gr_.setColor(Color.white);
					gr_.drawLine(0, y * ts + offset, getWidth(), y * ts + offset);
				}
			}
			if(selected != null) {
				gr_.setColor(Color.red);
				gr_.setStroke(new BasicStroke(2f));
				gr_.drawRect(selected.x * ts + offset, selected.y * ts + offset, 33, 33);
			}
			gr.drawImage(img, 0, 0, Main.this);
		}
	}
	
	public static void main(String[] args) {
		new Main();		
	}
}
