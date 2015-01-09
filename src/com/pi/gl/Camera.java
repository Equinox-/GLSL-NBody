package com.pi.gl;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.pi.math.Matrix4;
import com.pi.math.Vector3;

public class Camera {
	public float offset;
	public Matrix4 pose;
	public static Camera curr;

	public Camera() {
		offset = -50.0f;
		pose = Matrix4.identity();
		curr = this;
	}

	private Vector3 prevCamPos;
	private Vector3 orbitStartPos;

	public void process() {
		final float deltaX = Mouse.getDX();
		final float deltaY = Mouse.getDY();
		offset += Mouse.getDWheel() / 25.0f;

		Vector3 camPos = new Vector3(Mouse.getX() / (float) Display.getWidth()
				- 0.5f, Mouse.getY() / (float) Display.getHeight() - 0.5f, 0);

		Vector3 orbitStartCache = orbitStartPos;
		orbitStartPos = null;

		if (Keyboard.isKeyDown(Keyboard.KEY_F4)
				|| (Mouse.isButtonDown(2) && Keyboard
						.isKeyDown(Keyboard.KEY_LSHIFT))
				|| Mouse.isButtonDown(1)) {
			if (orbitStartCache == null)
				orbitStartCache = camPos;
			orbitStartPos = orbitStartCache;
			if (Vector3.mag(orbitStartPos) > 0.4f) {
				float magA = Vector3.mag(camPos);
				float magB = Vector3.mag(prevCamPos);
				if (magA > 0.1 && magB > 0.1) {
					float dir = -Math.signum((camPos.x - prevCamPos.x)
							* camPos.y);
					float angle = (float) Math.acos(Vector3.dot(camPos,
							prevCamPos) / (magA * magB));
					// Rotating
					if (!Float.isNaN(angle) && !Float.isInfinite(angle))
						pose = Matrix4.multiply(pose, Matrix4.axis_angle(dir
								* angle, new Vector3(0, 0, 1)));
				}
			} else {
				// orbit
				Vector3 axis = new Vector3();
				axis.x = -deltaY;
				axis.y = deltaX;
				axis.z = 0;
				float mag = Vector3.mag(axis);
				axis = Vector3.normalize(axis);
				if (mag > 0)
					pose = Matrix4.multiply(pose,
							Matrix4.axis_angle(mag / 180.0f, axis));
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_F3)) {
			// fine zoom
			offset += deltaY / 25;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_F2) || Mouse.isButtonDown(2)) {
			// Move
			Vector3 trans = new Vector3();
			trans.x = deltaX / 100.0f * (Math.abs(offset) / 5.0f);
			trans.y = deltaY / 100.0f * (Math.abs(offset) / 5.0f);
			trans.z = 0;
			if (deltaX != 0 || deltaY != 0)
				pose = Matrix4.multiply(pose, Matrix4.translation(trans));
		}
		prevCamPos = camPos;
	}

	public void glApply() {
		GL11.glTranslatef(0, 0, offset);
		GL11.glMultMatrix(pose.data);
	}

	public void glRotateInverse() {
		GL11.glMultMatrix(Matrix4.transpose(Matrix4.mat3(pose)).data);
	}
}