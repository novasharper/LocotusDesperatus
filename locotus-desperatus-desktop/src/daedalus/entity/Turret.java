package daedalus.entity;

import daedalus.graphics.MeshHelper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import static daedalus.entity.Entity.colorSet;

import daedalus.Root;
import daedalus.ld.LDMain;
import daedalus.main.GameComponent;

public class Turret extends Entity {
	
	public Turret() {
		super(200, 0, true);
		colorIndex = 4;
	}
	
	public double getDrawX() {
		return (getLoc().x - LDMain.ldm.chief.getLoc().x) * GameComponent.tileSize + Gdx.graphics.getWidth() / 2;
	}
	
	public double getDrawY() {
		return (getLoc().y - LDMain.ldm.chief.getLoc().y) * GameComponent.tileSize + Gdx.graphics.getHeight() / 2;
	}
	private ShaderProgram meshShader;
	private void createShader() {
		// this shader tells opengl where to put things
		String vertexShader = "attribute vec4 a_position;    \n"
							+ "void main()                   \n"
							+ "{                             \n"
							+ "   gl_Position = a_position;  \n"
							+ "}                             \n";

		// this one tells it what goes in between the points (i.e
		// colour/texture)
		String fragmentShader = "#ifdef GL_ES                \n"
							  + "precision mediump float;    \n"
							  + "#endif                      \n"
							  + "void main()                 \n"
							  + "{                           \n"
							  + "  gl_FragColor = vec4(1.0,0.0,0.0,1.0);	\n"
							  + "}";

		// make an actual shader from our strings
		meshShader = new ShaderProgram(vertexShader, fragmentShader);

		// check there's no shader compile errors
		if (!meshShader.isCompiled())
			throw new IllegalStateException(meshShader.getLog());
	}
	
	private MeshHelper h = null;
	private void draw(SpriteBatch sb, ShapeRenderer sr, float scale, ShapeType type) {
		if(meshShader == null) createShader();
		float drawx = (float) getDrawX();
		float drawy = (float) getDrawY();
		float[] v = {
			drawx - scale / 3f, drawy + scale,
			drawx - scale / 2f, drawy + scale / 2f,
			
			drawx - scale, drawy - scale / 2f,
			drawx + scale, drawy - scale / 2f,
			
			drawx + scale / 2f, drawy + scale / 2f,
			drawx + scale / 3f, drawy + scale,
				
			drawx + scale / 6f, drawy + scale * 1.25f,
			drawx + scale / 6f, drawy + scale * 2.25f,
			drawx - scale / 6f, drawy + scale * 2.25f,
			drawx - scale / 6f, drawy + scale * 1.25f,
		};
		if(h == null) h = new MeshHelper();
		if(type == ShapeType.Line) {
			sr.begin(type);
			sr.polygon(v);
			sr.end();
		} else {
			for(int i = 0; i < v.length; i++) {
				if(i % 2 == 0) v[i] = v[i] * 2 / Gdx.graphics.getWidth() - 1f;
				else v[i] = v[i] * 2 / Gdx.graphics.getHeight()  - 1f;
			}
			h.createMesh(v);
			h.drawMesh();
		}
	}
	
	public void render(SpriteBatch sb, ShapeRenderer sr) {
		double drawx = getDrawX();
		double drawy = getDrawY();
		if(drawx < -40 || drawx > Gdx.graphics.getWidth() + 40) return;
		if(drawy < -40 || drawy > Gdx.graphics.getHeight() + 40) return;
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		float red = Math.min(1f, 2f * (1f - health / maxHealth));
		float green = Math.min(1f, 2f * (health / maxHealth));
		sr.setColor(red * 0.75f, green * 0.75f, 0, 0.9f);
		draw(sb, sr, 20, ShapeType.Filled);
		sr.setColor(red, green, 0, 1f);
		draw(sb, sr, 20, ShapeType.Line);
		sr.begin(ShapeType.Filled);
		sr.circle((float) getDrawX(), (float) getDrawY(), 2);
		sr.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		if(!arms.isEmpty() && arms.getFirst() != null)
			arms.getFirst().render(sb, sr);
//		if(isAI) {
//			float barWidth = 60;
//			sr.begin(ShapeType.Filled);
//			float red = Math.min(1f, 2f * (1f - health / maxHealth));
//			float green = Math.min(1f, 2f * (health / maxHealth));
//			sr.setColor(red, green, 0, 1f);
//			sr.rect((float) getDrawX() - barWidth / 2, (float) getDrawY() + 30, (float) (barWidth * health / maxHealth), 5);
//			sr.end();
//		}
	}
}
