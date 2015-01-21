package com.pi.setups;

import com.pi.InteractionScript;

public class GravityDarkMatter extends InteractionScript {
	public GravityDarkMatter(double gravConst) {
		super();
		super.particleParticleInteract = "\n"
				+ "float invRoot = inversesqrt(dot(meToIt, meToIt) + 0.001f);\n"
				+ "gl_FragColor.xyz += (it1[3] + ((it1[3] > 1.0e6f) ? (2.75E6f / invRoot) : 0)) * GRAV_CONST * deltaT * invRoot * invRoot* invRoot * meToIt;";
		super.particleInteract = "";
		super.constants.put("GRAV_CONST", gravConst);
	}
	
	/**
	 * Units are solar masses, light years, and seconds
	 */
	public static final double GRAVITY_GALACTIC = 1.567783995250E-28;
}
