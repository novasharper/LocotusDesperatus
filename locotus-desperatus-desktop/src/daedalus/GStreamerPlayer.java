/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith / Patrik Schulze
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */

package daedalus;

import java.io.File;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.gstreamer.Bus;
import org.gstreamer.GstObject;
import org.gstreamer.State;
import org.gstreamer.elements.BaseSrc;
import org.gstreamer.elements.PlayBin2;
import org.gstreamer.elements.RGBDataSink;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL12;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import static org.lwjgl.opengl.GL11.*;

public class GStreamerPlayer {

	private int videoWidth, videoHeight;
	private int texture;
	private IntBuffer buffer;
	private boolean done = false;
	PlayBin2 playbin;
	private boolean dirty;
	private final Object lock = new Object();

	public GStreamerPlayer(String strToPlay) {
		try {
			GStreamerLibrary.init();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
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

	public void updateAndRender(ShapeRenderer sr, SpriteBatch sb) {

		glClearColor(0, 0, 0, 1);
		glClear(GL_COLOR_BUFFER_BIT);

		if (videoWidth == 0 || videoHeight == 0) {
			return;
		}

		glEnable(GL_TEXTURE_2D);

		synchronized (lock) {
			if (texture == 0) {
				texture = glGenTextures();
				glBindTexture(GL20.GL_TEXTURE_2D, texture);
				glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER,
						GL20.GL_LINEAR);
				glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER,
						GL20.GL_LINEAR);
				glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S,
						GL20.GL_CLAMP_TO_EDGE);
				glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T,
						GL20.GL_CLAMP_TO_EDGE);
				buffer.rewind();
				glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, videoWidth,
						videoHeight, 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE,
						buffer);
			}
			if (dirty) {
				bBuffer.rewind();
				glTexSubImage2D(GL20.GL_TEXTURE_2D, 0, 0, 0, videoWidth,
						videoHeight, GL12.GL_BGRA,
						GL12.GL_UNSIGNED_INT_8_8_8_8_REV, bBuffer.asIntBuffer());
				dirty = false;
			}
		}

		float scaleFactor = 1.0f;
		if (Display.getWidth() >= Display.getHeight()) {
			scaleFactor = (float) Display.getHeight() / (float) videoHeight;
		} else {
			scaleFactor = (float) Display.getWidth() / (float) videoWidth;
		}

		glBindTexture(GL_TEXTURE_2D, texture);

		float x1, x2, y1, y2;
		y1 = (Display.getHeight() - (videoHeight * scaleFactor)) / 2.0f;
		y2 = y1 + videoHeight * scaleFactor;

		x1 = (Display.getWidth() - (videoWidth * scaleFactor)) / 2.0f;
		x2 = x1 + videoWidth * scaleFactor;

		glBegin(GL_QUADS);
		{
			glTexCoord2f(0, 0);
			glVertex2f(x1, y1);

			glTexCoord2f(1, 0);
			glVertex2f(x2, y1);

			glTexCoord2f(1, 1);
			glVertex2f(x2, y2);

			glTexCoord2f(0, 1);
			glVertex2f(x1, y2);
		}
		glEnd();

	}

	public void destroy() {
		playbin.setState(org.gstreamer.State.NULL);
		playbin.dispose();
	}
	
	private ByteBuffer bBuffer;
	private class RGBListener implements RGBDataSink.Listener {
		public void rgbFrame(boolean preroll, int width, int height,
				IntBuffer rgb) {
			synchronized (lock) {
				if (buffer == null || videoWidth != width
						|| videoHeight != height) {
					buffer = BufferUtils.newIntBuffer(height * width);
					bBuffer = BufferUtils.newByteBuffer(width * height * 4);
					videoWidth = width;
					videoHeight = height;
				}
				bBuffer.rewind();
				bBuffer.asIntBuffer().put(rgb);
				dirty = true;
			}

		}
	}
}
