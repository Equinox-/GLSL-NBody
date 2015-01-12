package com.pi.setups;

import com.pi.PlacementScript;
import com.pi.math.Vector3;

public class SphereLayout implements PlacementScript {
	private static final float radius = 30;

	@Override
	public void fill(float[] masses, Vector3[] pos, Vector3[] vel, float[] extra) {
		for (int i = 0; i < masses.length; i++) {
			float f = (float) (Math.random() * Math.PI - Math.PI / 2);
			float rad = (float) (radius * Math.cos(f));
			float z = (float) (radius * Math.sin(f));
			float r = (float) (Math.random() * Math.PI * 2);
			pos[i] = new Vector3((float) (rad * Math.cos(r)),
					(float) (rad * Math.sin(r)), z);
			vel[i] = new Vector3();
			masses[i] = 1;
		}
	}
}
