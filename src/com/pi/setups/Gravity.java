package com.pi.setups;

import com.pi.InteractionScript;
import com.pi.Main;

public class Gravity extends InteractionScript {
	public Gravity() {
		super();
		super.particleParticleInteract = "\n"
				+ "float invRoot = inversesqrt(dot(meToIt, meToIt) + 0.001f);\n"
				+ "gl_FragColor.xyz += (it1[3] + ((it1[3] > 1.0e6f) ? (2.75E6f / invRoot) : 0)) * GRAV_CONST * deltaT * invRoot * invRoot* invRoot * meToIt;";
		super.particleInteract = "";
		super.constants.put("GRAV_CONST", (double) Main.GRAV_CONST);
	}
}
