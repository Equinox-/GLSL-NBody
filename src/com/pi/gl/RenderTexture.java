package com.pi.gl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.glu.GLU;

import com.pi.math.FastMath;
import com.pi.math.Vector3;

public class RenderTexture {
	private int texture;
	private int fbo;
	public final int particles, width;

	public static int perfectCount(int minimum) {
		return FastMath.nextPowerOf2(minimum);
	}

	public RenderTexture(int particles) {
		if (particles != perfectCount(particles))
			throw new IllegalArgumentException(particles
					+ " particles isn't perfect.  particles=2^n");
		this.particles = particles;
		System.out.println("Created with " + particles);

		this.width = particles << 1;
	}

	public void generate() {
		GL11.glEnable(GL11.GL_TEXTURE_1D);
		// Render Texture
		texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_1D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameterf(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_S,
				GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexImage1D(GL11.GL_TEXTURE_1D, 0, GL30.GL_RGBA32F, width, 0,
				GL11.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer) null);

		fbo = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
		GL30.glFramebufferTexture1D(GL30.GL_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_1D, texture, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	public void setData(float[] mass, float[] extra, Vector3[] pos,
			Vector3[] vel) {
		if (mass.length != pos.length
				|| (extra != null && extra.length != pos.length)
				|| (vel != null && pos.length != vel.length)
				|| pos.length != particles)
			throw new IllegalArgumentException("Input arrays must have length "
					+ particles + ", not (" + mass.length + ","
					+ (extra == null ? "null" : extra.length) + ","
					+ pos.length + "," + (vel == null ? "null" : vel.length)
					+ ")");

		FloatBuffer rawData = BufferUtils.createFloatBuffer(particles * 8);
		for (int i = 0; i < particles; i++) {
			rawData.put(pos[i].x);
			rawData.put(pos[i].y);
			rawData.put(pos[i].z);
			rawData.put(mass[i]);
			if (vel != null) {
				rawData.put(vel[i].x);
				rawData.put(vel[i].y);
				rawData.put(vel[i].z);
			} else
				rawData.put(new float[] { 0, 0, 0 });
			if (extra != null)
				rawData.put(extra[i]);
			else
				rawData.put(0);
		}
		rawData.flip();

		GL11.glBindTexture(GL11.GL_TEXTURE_1D, texture);
		GL11.glTexSubImage1D(GL11.GL_TEXTURE_1D, 0, 0, width, GL11.GL_RGBA,
				GL11.GL_FLOAT, rawData);
		GL11.glBindTexture(GL11.GL_TEXTURE_1D, 0);
	}

	public void bindRender() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
		GL11.glViewport(0, 0, width, 1);
	}

	public int getTexture() {
		return texture;
	}
}
