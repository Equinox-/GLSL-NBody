package com.pi;

import com.pi.math.Vector3;

public interface PlacementScript {
	public void fill(float[] masses, Vector3[] pos, Vector3[] vel, float[] extra);
}
