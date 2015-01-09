package com.pi.gl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import com.pi.math.FastMath;
import com.pi.math.Vector3;

public class RenderTexture {
	private int texture;
	private int fbo;
	public final int particles, width, bitWidth;

	public static int perfectCount(int minimum) {
		int minSize = (int) Math.ceil(Math.sqrt(minimum << 1));
		int size2 = FastMath.nextPowerOf2(minSize);
		return (size2 * size2) >> 1;
	}

	public RenderTexture(int particles) {
		if (particles != perfectCount(particles))
			throw new IllegalArgumentException(particles
					+ " particles isn't perfect.  particles=2*4^n");
		this.particles = particles;
		int width = (int) Math.ceil(Math.sqrt(particles << 1));
		this.width = width;
		int bitWidth = 0;
		while ((width >>= 1) > 0)
			bitWidth++;
		this.bitWidth = bitWidth;
		System.out.println("Created with " + particles + " on a " + this.width
				+ "x" + this.width + " texture with a shift of " + bitWidth);
	}

	public void generate() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// Render Texture
		texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, width, width,
				0, GL11.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer) null);

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

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, width,
				GL11.GL_RGBA, GL11.GL_FLOAT, rawData);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void bindRender() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
		GL11.glViewport(0, 0, width, width);
	}

	public int getTexture() {
		return texture;
	}
}
