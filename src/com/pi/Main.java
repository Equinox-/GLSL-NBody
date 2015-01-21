package com.pi;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.PixelFormat;

import com.pi.gl.Camera;
import com.pi.gl.ParticleTexture;
import com.pi.gl.Shaders;
import com.pi.math.Vector3;
import com.pi.setups.Gravity;
import com.pi.setups.SpiralGalaxy;

public class Main {
	public static final float GRAV_CONST = 1.567783995250E-28f;

	public static final float TIME_DELTA = 60 * 60 * 24 * 365.25f * 1000f
			* 100f;

	public static final File dataDir = new File("./data");

	private static double physDelta = 0;
	private static final int PERFECT_PARTICLES = ParticleTexture
			.perfectCount(8192);
	private static final long initTime = System.currentTimeMillis();

	public static double getTime() {
		return (System.currentTimeMillis() - initTime) / 1000.0;
	}

	public static double getDelta() {
		return physDelta;
	}

	private static final float HORIZ_FOV = 120;

	public Main() throws LWJGLException, IOException {
		Display.setDisplayMode(new DisplayMode(1280, 720));
		Display.setTitle("Docking");
		PixelFormat px = new PixelFormat();
		Display.create(new PixelFormat().withAccumulationBitsPerPixel(8));

		load();
		init();
		run();
	}

	private void windowResized(int width, int height, float near) {
		GL11.glViewport(0, 0, width, height);
		final float tanV = (float) Math.tan(HORIZ_FOV * Math.PI / 360.0);
		final float aspect = height / (float) width;
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glFrustum(-tanV * near, tanV * near, -tanV * aspect * near, tanV
				* aspect * near, near, 1e27f);
	}

	private Camera camera;
	private ParticleTexture[] buffers;
	private int dataBuffer;
	private IntBuffer drawQueue;
	final float radius = 30;

	private void load() throws IOException {
		camera = new Camera();
	}

	float[] myMass;
	Vector3[] myPos;
	Vector3[] myVel;

	private void init() {
		Shaders.NBODY.setInjector(new Gravity());

		buffers = new ParticleTexture[2];

		float[] masses = myMass = new float[PERFECT_PARTICLES];
		Vector3[] pos = myPos = new Vector3[PERFECT_PARTICLES];
		Vector3[] vel = myVel = new Vector3[PERFECT_PARTICLES];
		new SpiralGalaxy().fill(masses, pos, vel, null);

		for (int i = 0; i < 2; i++) {
			buffers[i] = new ParticleTexture(PERFECT_PARTICLES);
			buffers[i].generate();
			buffers[i].setData(masses, null, pos, vel);
		}
		dataBuffer = 0;
		drawQueue = BufferUtils.createIntBuffer(PERFECT_PARTICLES);
		for (int i = 0; i < PERFECT_PARTICLES; i++)
			drawQueue.put(i);
		drawQueue.flip();

		{
			Shaders.NBODY.use();
			GL20.glUniform1i(Shaders.NBODY.uniform("sourceSize"),
					PERFECT_PARTICLES);
			GL20.glUniform1i(Shaders.NBODY.uniform("sourceWidth"),
					buffers[0].width);
			GL20.glUniform1i(Shaders.NBODY.uniform("sourceShift"),
					buffers[0].bitWidth);
			GL20.glUniform1i(Shaders.NBODY.uniform("sourceMask"),
					(1 << buffers[0].bitWidth) - 1);
			GL20.glUniform1i(Shaders.NBODY.uniform("velocityMask"),
					buffers[0].velMask);
			GL20.glUniform1f(Shaders.NBODY.uniform("deltaT"), TIME_DELTA);

			Shaders.OUTPUT.use();
			GL20.glUniform1i(Shaders.OUTPUT.uniform("sourceWidth"),
					buffers[0].width);
			GL20.glUniform1i(Shaders.OUTPUT.uniform("sourceShift"),
					buffers[0].bitWidth);
			GL20.glUniform1i(Shaders.OUTPUT.uniform("sourceMask"),
					(1 << buffers[0].bitWidth) - 1);
			Shaders.noProgram();
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD, GL14.GL_FUNC_ADD);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA,
				GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
	}

	private void run() {
		double lastLoop = getTime();
		long lastSecond = System.currentTimeMillis();
		int frames = 0;
		GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);
		boolean accumInit = false;
		while (!Display.isCloseRequested()) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			windowResized(Display.getWidth(), Display.getHeight(), 1);
			if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
				GL11.glClear(GL11.GL_ACCUM_BUFFER_BIT);
				accumInit = false;
			}
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			camera.glApply();
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D,
					buffers[dataBuffer & 1].getTexture());
			Shaders.OUTPUT.use();
			GL11.glDrawArrays(GL11.GL_POINTS, 0, PERFECT_PARTICLES);
			Shaders.noProgram();
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			// GL11.glBegin(GL11.GL_POINTS);
			// GL11.glColor3f(1, 0, 0);
			// for (int i = 0; i < myPos.length; i++) {
			// GL11.glVertex3f(myPos[i].x, myPos[i].y, myPos[i].z);
			// }
			// GL11.glEnd();

			GL11.glPopMatrix();

			if (accumInit)
				GL11.glAccum(GL11.GL_ACCUM, 1);
			else {
				GL11.glAccum(GL11.GL_ACCUM, 1);
				accumInit = true;
			}
			GL11.glAccum(GL11.GL_RETURN, 1.0f);
			GL11.glAccum(GL11.GL_LOAD, 0.975f);

			Display.update();

			physDelta = (getTime() - lastLoop);
			lastLoop = getTime();
			physics();
			// Display.sync(30);
			frames++;
			if (lastSecond + 1000 < System.currentTimeMillis()) {
				Display.setTitle((frames * 1000 / (System.currentTimeMillis() - lastSecond))
						+ " FPS");
				lastSecond = System.currentTimeMillis();
				frames = 0;
			}
		}
	}

	boolean running = false, pressFlag = false;

	public void physics() {
		camera.process();

		// Run update.
		if (running) {
			buffers[(dataBuffer ^ 1) & 1].bindRender();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D,
					buffers[dataBuffer & 1].getTexture());
			Shaders.NBODY.use();
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
			Shaders.noProgram();
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			dataBuffer = (dataBuffer ^ 1) & 1;

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			if (!pressFlag)
				running = !running;
			pressFlag = true;
		} else
			pressFlag = false;

		// float dt = 1;
		// MANUAL UPDATE
		// for (int i = 0; i < myMass.length; i++) {
		// Vector3.addto(myPos[i], myVel[i], dt);
		// for (int j = 0; j < myMass.length; j++) {
		// if (i != j) {
		// Vector3 meToIt = Vector3.lincom(myPos[j], 1, myPos[i], -1);
		// float distLen = Vector3.mag(meToIt);
		// Vector3.addto(myVel[i], meToIt, myMass[j] * GRAV_CONST*dt
		// * (float) Math.pow(distLen + .01f, -3));
		// }
		// }
		// }
	}

	public static void main(String[] args) throws LWJGLException, IOException {
		new Main();
	}
}
