package com.pi.setups;

import com.pi.PlacementScript;
import com.pi.math.Vector3;

public class Galaxy implements PlacementScript {
	private static final float blackHoleMass = 2.6e6f;
	private static final float galacticRadius = 100e3f;
	private static final float diskRadius = 1e3f;
	private static final float galacticBuldge = 10e3f;

	private static float random(float randMin, float randMax) {
		float result;
		result = (float) Math.random();

		return ((1.0f - result) * randMin + result * randMax);
	}

	private static float randomGauss(float randMin, float randMax) {
		float center = (randMax + randMin) / 2.0f;
		float result = (float) (2.0f * Math.sqrt(Math.random())) - 1.0f;
		return center + (randMax - center) * result;
	}

	private static void generateGalaxy(float[] masses, Vector3[] pos,
			Vector3[] vel, int count, int offset, float branches) {

		for (int i = offset; i < offset + 10; i++) {
			pos[i] = new Vector3(0, 0, (i - 5) * 1E5f);
			vel[i] = new Vector3();
			masses[i] = blackHoleMass;
		}

		float totalMass = blackHoleMass;
		for (int i = offset + 10; i < offset + count; ++i) {
			float branch = (int) Math.floor(random(0, 1) * branches);
			float angle = 6.28f - (((float) (i - offset)) * 6.28f / count);
			float magnitude = (6.5f - angle) / 6.28f * galacticRadius;
			angle += (6.28f / branches) * branch;

			float normalMag = magnitude / galacticRadius;
			angle += random((float) Math.pow(normalMag, 5) - 1.25f,
					1.25f - (float) Math.pow(normalMag, 5));

			float mass = randomGauss(0.5f, (2.0f - normalMag) * 500.0f);

			float lpitch = random(-(float) Math.pow(1.0f - normalMag, 5.0f),
					(float) Math.pow(1.0f - normalMag, 5.0f));
			pos[i] = new Vector3(magnitude * (float) Math.cos(angle), magnitude
					* (float) Math.sin(angle), galacticBuldge * lpitch
					+ (diskRadius * randomGauss(-1.0f, 1.0f)) - 1E3f
					* (float) Math.pow(galacticRadius / magnitude, 2));

			float vMag = (float) Math.sqrt(1.567783995250E-28
					* (totalMass + (2.0E6 * magnitude)) / magnitude);
			vel[i] = new Vector3((float) Math.sin(angle) * -vMag,
					(float) Math.cos(angle) * vMag, pos[i].z / 1E15f);

			// Mass value
			masses[i] = mass;

			totalMass += mass;
		}
	}

	@Override
	public void fill(float[] masses, Vector3[] pos, Vector3[] vel, float[] extra) {
		generateGalaxy(masses, pos, vel, pos.length, 0, 3);
	}
}
