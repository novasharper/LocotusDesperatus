package daedalus.graphics;

import java.io.File;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

import org.gstreamer.Bus;
import org.gstreamer.ClockTime;
import org.gstreamer.GstObject;
import org.gstreamer.State;
import org.gstreamer.elements.PlayBin2;
import org.gstreamer.elements.RGBDataSink;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL12;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.BufferUtils;

import daedalus.main.GameComponent;
import daedalus.main.GameContext;
import daedalus.util.Util;

public class Cutscene extends GameContext {
	public static final int[] sizes = { 720, 768, 1080 };
	
	private int videoWidth, videoHeight;
	private IntBuffer buffer;
	private boolean done = false;
	PlayBin2 playbin;
	private boolean dirty;
	private final Object lock = new Object();

	public Cutscene(String cName) {
		try {
			GStreamerLibrary.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		playbin = new PlayBin2("VideoPlayer");
		
		RGBListener listener = new RGBListener();
		int size = 720;
		int Size = GameComponent.getSize();
		for(int size_ : sizes) {
			size = size_;
			if(Size <= size_) {
				break;
			}
		}
		File movie = new File(Util.getWorkingDir(), "resources/movie/" + cName + "/" + cName + "_" + size + ".mp4");
		playbin.setInputFile(movie);

		RGBDataSink sink = new RGBDataSink("sink", listener);
		sink.setPassDirectBuffer(true);

		playbin.setVideoSink(sink);

		playbin.setState(State.PAUSED);
		playbin.getState();

		playbin.getBus().connect(new Bus.ERROR() {
			@Override
			public void errorMessage(GstObject arg0, int arg1, String arg2) {
				// PANIC !
				done = true;
			}
		});

		playbin.getBus().connect(new Bus.EOS() {
			@Override
			public void endOfStream(GstObject arg0) {
				done = true;
//				playbin.seek(ClockTime.ZERO);
			}
		});
	}

	public void preroll() {
		playbin.pause();
		playbin.getState();
	}

	public void play() {
		playbin.play();
	}

	public int getWidth() {
		return videoWidth;
	}

	public int getHeight() {
		return videoHeight;
	}

	public PlayBin2 getPlayBin() {
		return playbin;
	}

	public boolean isDone() {
		return done;
	}

	public void destroy() {
		playbin.setState(org.gstreamer.State.NULL);
		playbin.dispose();
	}

	private class RGBListener implements RGBDataSink.Listener {
		public void rgbFrame(boolean preroll, int width, int height,
				IntBuffer rgb) {
			synchronized (lock) {
				if (buffer == null || videoWidth != width
						|| videoHeight != height) {
					buffer = BufferUtils.newIntBuffer(width * height);
					videoWidth = width;
					videoHeight = height;
				}
				buffer.rewind();
				buffer.put(rgb);
				dirty = true;
			}
		}
	}
	
	private Texture tex;

	public void init() {
		tex = new Texture(videoWidth, videoHeight, Format.RGBA8888);
	}

	public void tick() {
	}

	public boolean isTransparent() {
		return false;
	}
	
	int endTimer = -1;
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		sr.begin(ShapeType.Filled);
		sr.setColor(0, 0, 0, 1);
		sr.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		sr.end();
		
		
		if(isDone()) {
			if(endTimer < 0) endTimer = (int) (GameComponent.framerate);
			else if(endTimer > 0) endTimer--;
			else {
				destroy();
				GameComponent.getGame().popContext();
			}
			return;
		}
		else if(!playbin.isPlaying()) playbin.play();
		
		if (videoWidth == 0 || videoHeight == 0) {
			return;
		}

		synchronized (lock) {
			if (dirty) {
				if(tex == null) {
					try {
						tex = new Texture(videoWidth, videoHeight, Format.RGBA8888);
					} catch(Exception ex) {
						return;
					}
				}
				buffer.rewind();
				tex.bind();
				Gdx.gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
				dirty = false;
			}
		}

		float scaleFactor = 1.0f;
		if (Display.getWidth() >= Display.getHeight()) {
			scaleFactor = (float) Display.getHeight() / (float) videoHeight;
		} else {
			scaleFactor = (float) Display.getWidth() / (float) videoWidth;
		}
		sb.begin();
		tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		sb.draw(tex, (Gdx.graphics.getWidth() - videoWidth * scaleFactor) / 2,
				(Gdx.graphics.getHeight() - videoHeight * scaleFactor) / 2, videoWidth * scaleFactor, videoHeight * scaleFactor);
		sb.end();
	}
}
