package daedalus.graphics;

import java.io.File;
import java.nio.IntBuffer;

import org.gstreamer.Bus;
import org.gstreamer.GstObject;
import org.gstreamer.State;
import org.gstreamer.elements.PlayBin2;
import org.gstreamer.elements.RGBDataSink;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL12;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.BufferUtils;

import daedalus.GStreamerLibrary;
import daedalus.Root;
import daedalus.main.GameComponent;
import daedalus.main.GameContext;

public class Cutscene extends GameContext {
	private int videoWidth, videoHeight;
	private IntBuffer buffer;
	private boolean done = false;
	PlayBin2 playbin;
	private boolean dirty;
	private final Object lock = new Object();

	public Cutscene(String strToPlay) {
		try {
			GStreamerLibrary.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		playbin = new PlayBin2("VideoPlayer");

		RGBListener listener = new RGBListener();
		playbin.setInputFile(new File(strToPlay));

		RGBDataSink sink = new RGBDataSink(("sink"), listener);
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

	static {
		try {
			GStreamerLibrary.init();
		} catch (Exception e) {
		}
	}
	private Texture tex;

	public void init() {
		tex = new Texture(videoWidth, videoHeight, Format.RGBA8888);
	}

	public void tick() {
		// TODO Auto-generated method stub

	}

	public boolean isTransparent() {
		return false;
	}

	public void render(SpriteBatch sb, ShapeRenderer sr) {
		if(isDone()) Gdx.app.exit();
		
		if (videoWidth == 0 || videoHeight == 0) {
			return;
		}

		synchronized (lock) {
			if (dirty) {
				buffer.rewind();
				tex.bind();
				Gdx.gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, videoWidth,
						videoHeight, GL12.GL_BGRA,
						GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
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
		sb.draw(tex, 0, 0, videoWidth * scaleFactor, videoHeight * scaleFactor);
		sb.end();
	}

	public static void main(String[] args) {
		args = new String[] { Root.class.getResource("/data/sample_h264.mp4")
				.getFile().toString() };
		Cutscene player = new Cutscene(args[0]);
		player.preroll();
		GameComponent.create("Locotus Desperatus", player.getWidth(),
				player.getHeight(), false, false);
		GameComponent.getGame().pushContext(player);
		GameComponent.getGame().start();
		GameComponent.getGame().setPaused(false);
		player.play();
	}
}
