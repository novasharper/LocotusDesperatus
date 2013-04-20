package daedalus;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import daedalus.util.ScreenshotSaver;

public class LocotusDesperatus extends InputAdapter implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;
	private ShapeRenderer sRender;
	
	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera(1, h/w);
		batch = new SpriteBatch();
		sRender = new ShapeRenderer();
		Gdx.input.setInputProcessor(this);
	}
	public void dispose() {
		batch.dispose();
	}
	public void render() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
	}
	public void resize(int width, int height) {
		camera = new OrthographicCamera(1, (float) height / width);
		batch = new SpriteBatch();
		sRender = new ShapeRenderer();
		Root.reloadFonts();
	}
	public void pause() {
	}
	public void resume() {
	}
	public boolean keyUp(int keycode) {
		if(keycode == Keys.ESCAPE) Gdx.app.exit();
		else if(keycode == Keys.F2) {
			try {
				ScreenshotSaver.saveScreenshot(new File("scrn.png"));
			} catch (IOException e) {
			}
		}
		else if(keycode == Keys.F11) {
			DisplayMode dm = Gdx.graphics.getDesktopDisplayMode();
			Gdx.graphics.setDisplayMode(dm.width, dm.height, true);
		}
		return true;
	}
}
