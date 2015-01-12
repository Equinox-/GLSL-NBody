package com.pi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.pi.gl.Shaders.ShaderInjector;

public abstract class InteractionScript implements ShaderInjector {
	protected final Map<String, Double> constants = new HashMap<String, Double>();
	protected String particleParticleInteract;
	protected String particleInteract;

	@Override
	public String vertShaderCallback(String src) {
		return src;
	}

	@Override
	public String fragShaderCallback(String src) {
		src = src.replace("__PARTICLE_PARTICLE_FORCES__",
				particleParticleInteract);
		src = src.replace("__PARTICLE_FORCES__", particleInteract);

		StringBuilder defines = new StringBuilder();
		for (Entry<String, Double> cs : constants.entrySet())
			defines.append("#define " + cs.getKey() + " (" + cs.getValue()
					+ ")\n");
		src = src.replace("__DEFINES__", defines.toString());
		
		System.out.println(src);
		return src;
	}
}
