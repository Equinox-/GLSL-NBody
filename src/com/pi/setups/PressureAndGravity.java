package com.pi.setups;

import com.pi.InteractionScript;

public class PressureAndGravity extends InteractionScript {
	public PressureAndGravity() {
		super();
		super.particleParticleInteract = "\n"
				+ "float dist = length(meToIt) + .001f;\n"
				+ "float gravPower = pow(dist+1,-3);\n"
				+ "float esPower = exp(-dist) / (dist+1);\n"
				+ "gl_FragColor.xyz += deltaT * SUB_MULT * (gravPower - 100 * esPower) * meToIt;";
		
		
		super.particleInteract = "float dist = length(me1.xyz);\n"
				+ "float gravPower = pow(dist+1,-3);\n"
				+ "float esPower = exp(-dist) / (dist + 1);\n"
				+ "gl_FragColor.xyz += deltaT * (1e10f * esPower - 1 * gravPower) * MAIN_MULT * me1.xyz;";
		
		super.constants.put("SUB_MULT", 1E1);
		super.constants.put("MAIN_MULT", 1E9);
	}
}
