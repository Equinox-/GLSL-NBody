package com.pi;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;

import com.pi.gl.Camera;
import com.pi.gl.RenderTexture;
import com.pi.gl.Shaders;
import com.pi.math.Vector3;

public class Main {
	public static final File dataDir = new File("./data");

	private static double physDelta = 0;
	private static final int PERFECT_PARTICLES = RenderTexture.perfectCount(2048);
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
		Display.create(new PixelFormat(8, 8, 0, 8));

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
				* aspect * near, near, near + 10000);
	}

	private Camera camera;
	private RenderTexture[] buffers;
	private int dataBuffer;
	private IntBuffer drawQueue;
	final float radius = 30;

	private void load() throws IOException {
		camera = new Camera();
		camera.offset = -3;
	}

	private void init() {
		buffers = new RenderTexture[2];

		float[] masses = new float[PERFECT_PARTICLES];
		Vector3[] pos = new Vector3[PERFECT_PARTICLES];
		Vector3[] vel = new Vector3[PERFECT_PARTICLES];
		for (int i = 0; i < PERFECT_PARTICLES; i++) {
			pos[i] = new Vector3((float) Math.sin(i / 5f),
					(float) Math.cos(i / 5f), 0);
			vel[i] = new Vector3(0, 0, 0);
			masses[i] = 1;
		}
		for (int i = 0; i < 2; i++) {
			buffers[i] = new RenderTexture(PERFECT_PARTICLES);
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

			GL20.glUniform1f(Shaders.NBODY.uniform("deltaT"), 1);
			GL20.glUniform1f(Shaders.NBODY.uniform("gravConst"), 1E-5f);

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
	}

	private void run() {
		double lastLoop = getTime();
		long lastSecond = System.currentTimeMillis();
		int frames = 0;
		while (!Display.isCloseRequested()) {
			windowResized(Display.getWidth(), Display.getHeight(), 1);
			// GL11.glClearColor(1,1,1,1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			camera.glApply();
			GL11.glPushMatrix();
			GL11.glPointSize(5);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D,
					buffers[dataBuffer & 1].getTexture());
			Shaders.OUTPUT.use();
			GL11.glDrawArrays(GL11.GL_POINTS, 0, PERFECT_PARTICLES);
			Shaders.noProgram();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glPopMatrix();

			Display.update();

			physDelta = (getTime() - lastLoop) * 2;
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

	public void physics() {
		camera.process();

		// Run update.
		buffers[(dataBuffer ^ 1) & 1].bindRender();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
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

	public static void main(String[] args) throws LWJGLException, IOException {
		new Main();
	}
}
